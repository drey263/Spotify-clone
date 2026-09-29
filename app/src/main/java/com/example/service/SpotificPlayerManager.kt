package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.api.StreamResolver
import com.example.model.PlaybackState
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SpotificPlayerManager private constructor() {

    companion object {
        private const val TAG = "SpotificPlayerManager"

        @Volatile
        private var INSTANCE: SpotificPlayerManager? = null

        fun getInstance(): SpotificPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SpotificPlayerManager().also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tickerJob: Job? = null
    private var streamResolver: StreamResolver? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var activeService: SpotificAudioService? = null

    fun initialize(resolver: StreamResolver) {
        this.streamResolver = resolver
    }

    fun registerService(service: SpotificAudioService) {
        this.activeService = service
        Log.d(TAG, "AudioService registered with PlayerManager")
    }

    fun unregisterService(service: SpotificAudioService) {
        if (this.activeService == service) {
            this.activeService = null
            Log.d(TAG, "AudioService unregistered from PlayerManager")
        }
    }

    fun playTrack(context: Context, track: Track, queue: List<Track> = emptyList()) {
        val fullQueue = if (queue.isNotEmpty()) queue else listOf(track)
        val index = fullQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        _playbackState.update {
            it.copy(
                currentTrack = track,
                queue = fullQueue,
                queueIndex = index,
                isPlaying = false,
                isBuffering = true,
                currentPositionMs = 0L,
                durationMs = 0L,
                error = null
            )
        }

        // Start Foreground Service
        val serviceIntent = Intent(context, SpotificAudioService::class.java).apply {
            action = SpotificAudioService.ACTION_PLAY
            putExtra(SpotificAudioService.EXTRA_TRACK_ID, track.id)
            putExtra(SpotificAudioService.EXTRA_TRACK_TITLE, track.title)
            putExtra(SpotificAudioService.EXTRA_TRACK_ARTIST, track.artist)
            putExtra(SpotificAudioService.EXTRA_TRACK_THUMBNAIL, track.thumbnail)
            putExtra(SpotificAudioService.EXTRA_TRACK_URL, track.url)
            putExtra(SpotificAudioService.EXTRA_TRACK_LOCAL_PATH, track.localFilePath)
            putExtra(SpotificAudioService.EXTRA_TRACK_IS_LOCAL, track.isLocal)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Resolve stream URL asynchronously if not already local
        scope.launch(Dispatchers.IO) {
            val resolvedUrl = if (track.isLocal && !track.localFilePath.isNullOrEmpty()) {
                track.localFilePath
            } else {
                streamResolver?.resolveStreamUrl(track)
            }

            if (!resolvedUrl.isNullOrEmpty()) {
                activeService?.loadAndPlay(resolvedUrl)
            } else {
                _playbackState.update {
                    it.copy(isBuffering = false, error = "Failed to load audio stream")
                }
            }
        }
    }

    fun togglePlayPause(context: Context) {
        val current = _playbackState.value
        if (current.currentTrack == null) return

        if (current.isPlaying) {
            pause()
        } else {
            resume(context)
        }
    }

    fun resume(context: Context) {
        activeService?.resume() ?: run {
            // Restart service if it was stopped
            _playbackState.value.currentTrack?.let { track ->
                playTrack(context, track, _playbackState.value.queue)
            }
        }
    }

    fun pause() {
        activeService?.pause()
    }

    fun seekTo(positionMs: Long) {
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
        activeService?.seekTo(positionMs.toInt())
    }

    fun skipNext(context: Context) {
        val state = _playbackState.value
        val queue = state.queue
        if (queue.isEmpty()) return

        val nextIndex = (state.queueIndex + 1) % queue.size
        playTrack(context, queue[nextIndex], queue)
    }

    fun skipPrevious(context: Context) {
        val state = _playbackState.value
        // If > 3 seconds in, seek to start of current song
        if (state.currentPositionMs > 3000L) {
            seekTo(0L)
            return
        }

        val queue = state.queue
        if (queue.isEmpty()) return

        val prevIndex = if (state.queueIndex - 1 < 0) queue.size - 1 else state.queueIndex - 1
        playTrack(context, queue[prevIndex], queue)
    }

    fun updatePlaybackProgress(positionMs: Long, durationMs: Long) {
        _playbackState.update {
            it.copy(
                currentPositionMs = positionMs,
                durationMs = if (durationMs > 0) durationMs else it.durationMs
            )
        }
    }

    fun onPlaybackStarted(durationMs: Long) {
        _playbackState.update {
            it.copy(
                isPlaying = true,
                isBuffering = false,
                durationMs = durationMs
            )
        }
        startProgressTicker()
    }

    fun onPlaybackPaused() {
        _playbackState.update { it.copy(isPlaying = false) }
        stopProgressTicker()
    }

    fun onPlaybackBuffering() {
        _playbackState.update { it.copy(isBuffering = true) }
    }

    fun onPlaybackCompleted(context: Context) {
        stopProgressTicker()
        skipNext(context)
    }

    fun onPlaybackError(message: String) {
        _playbackState.update {
            it.copy(isPlaying = false, isBuffering = false, error = message)
        }
        stopProgressTicker()
    }

    private fun startProgressTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(500)
                activeService?.let { service ->
                    if (service.isPlaying()) {
                        val pos = service.getCurrentPosition().toLong()
                        val dur = service.getDuration().toLong()
                        updatePlaybackProgress(pos, dur)
                    }
                }
            }
        }
    }

    private fun stopProgressTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun getStateMap(): Map<String, Any?> {
        val state = _playbackState.value
        return mapOf(
            "isPlaying" to state.isPlaying,
            "isBuffering" to state.isBuffering,
            "currentPositionMs" to state.currentPositionMs,
            "durationMs" to state.durationMs,
            "title" to (state.currentTrack?.title ?: ""),
            "artist" to (state.currentTrack?.artist ?: ""),
            "thumbnail" to (state.currentTrack?.thumbnail ?: ""),
            "url" to (state.currentTrack?.url ?: ""),
            "trackId" to (state.currentTrack?.id ?: "")
        )
    }
}
