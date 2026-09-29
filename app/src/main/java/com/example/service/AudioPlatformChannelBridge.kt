package com.example.service

import android.content.Context
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Handles communication between Flutter UI and Kotlin Audio Foreground Service
 * via Flutter Platform Channel architecture.
 */
class AudioPlatformChannelBridge(
    private val context: Context,
    private val playerManager: SpotificPlayerManager
) {
    interface EventSink {
        fun success(event: Any?)
        fun error(errorCode: String, errorMessage: String?, errorDetails: Any?)
    }

    interface ResultSink {
        fun success(result: Any?)
        fun error(errorCode: String, errorMessage: String?, errorDetails: Any?)
        fun notImplemented()
    }

    private var eventJob: Job? = null
    private var eventSink: EventSink? = null

    companion object {
        const val METHOD_CHANNEL_NAME = "com.spotific.audio/methods"
        const val EVENT_CHANNEL_NAME = "com.spotific.audio/events"
    }

    fun handleMethodCall(
        method: String,
        arguments: Map<String, Any?>?,
        result: ResultSink,
        coroutineScope: CoroutineScope
    ) {
        when (method) {
            "getState" -> {
                result.success(playerManager.getStateMap())
            }
            "play" -> {
                val title = arguments?.get("title") as? String ?: "Unknown"
                val artist = arguments?.get("artist") as? String ?: "Unknown"
                val thumbnail = arguments?.get("thumbnail") as? String ?: ""
                val url = arguments?.get("url") as? String ?: ""
                val id = arguments?.get("id") as? String ?: url
                val duration = arguments?.get("duration") as? String
                val isLocal = arguments?.get("isLocal") as? Boolean ?: false
                val localFilePath = arguments?.get("localFilePath") as? String

                val track = Track(
                    id = id,
                    title = title,
                    artist = artist,
                    thumbnail = thumbnail,
                    url = url,
                    duration = duration,
                    isLocal = isLocal,
                    localFilePath = localFilePath
                )

                playerManager.playTrack(context, track)
                result.success(true)
            }
            "pause" -> {
                playerManager.pause()
                result.success(true)
            }
            "resume" -> {
                playerManager.resume(context)
                result.success(true)
            }
            "togglePlayPause" -> {
                playerManager.togglePlayPause(context)
                result.success(true)
            }
            "seek" -> {
                val positionMs = (arguments?.get("positionMs") as? Number)?.toLong() ?: 0L
                playerManager.seekTo(positionMs)
                result.success(true)
            }
            "skipNext" -> {
                playerManager.skipNext(context)
                result.success(true)
            }
            "skipPrevious" -> {
                playerManager.skipPrevious(context)
                result.success(true)
            }
            else -> result.notImplemented()
        }
    }

    fun onListen(sink: EventSink, scope: CoroutineScope) {
        eventSink = sink
        eventJob?.cancel()
        eventJob = playerManager.playbackState
            .onEach {
                sink.success(playerManager.getStateMap())
            }
            .launchIn(scope)
    }

    fun onCancel() {
        eventJob?.cancel()
        eventJob = null
        eventSink = null
    }
}
