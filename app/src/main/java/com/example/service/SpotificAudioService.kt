package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState as MediaPlaybackState
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpotificAudioService : Service(), AudioManager.OnAudioFocusChangeListener {

    companion object {
        private const val TAG = "SpotificAudioService"
        const val CHANNEL_ID = "spotific_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.spotific.action.PLAY"
        const val ACTION_PAUSE = "com.spotific.action.PAUSE"
        const val ACTION_PLAY_PAUSE = "com.spotific.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.spotific.action.NEXT"
        const val ACTION_PREVIOUS = "com.spotific.action.PREVIOUS"
        const val ACTION_STOP = "com.spotific.action.STOP"
        const val ACTION_SEEK = "com.spotific.action.SEEK"

        const val EXTRA_TRACK_ID = "extra_track_id"
        const val EXTRA_TRACK_TITLE = "extra_track_title"
        const val EXTRA_TRACK_ARTIST = "extra_track_artist"
        const val EXTRA_TRACK_THUMBNAIL = "extra_track_thumbnail"
        const val EXTRA_TRACK_URL = "extra_track_url"
        const val EXTRA_TRACK_LOCAL_PATH = "extra_track_local_path"
        const val EXTRA_TRACK_IS_LOCAL = "extra_track_is_local"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSession? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    private var currentTitle: String = "Spotific"
    private var currentArtist: String = "Now Playing"
    private var currentThumbnail: String = ""
    private var albumArtBitmap: Bitmap? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val playerManager by lazy { SpotificPlayerManager.getInstance() }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SpotificAudioService onCreate")
        playerManager.registerService(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        createNotificationChannel()
        setupMediaSession()
        setupMediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand action=$action")

        when (action) {
            ACTION_PLAY -> {
                currentTitle = intent.getStringExtra(EXTRA_TRACK_TITLE) ?: "Spotific"
                currentArtist = intent.getStringExtra(EXTRA_TRACK_ARTIST) ?: "Music"
                currentThumbnail = intent.getStringExtra(EXTRA_TRACK_THUMBNAIL) ?: ""
                loadAlbumArt(currentThumbnail)
                updateNotification(isPlaying = false)
            }
            ACTION_PLAY_PAUSE -> {
                if (isPlaying()) pause() else resume()
            }
            ACTION_PAUSE -> pause()
            ACTION_NEXT -> playerManager.skipNext(this)
            ACTION_PREVIOUS -> playerManager.skipPrevious(this)
            ACTION_STOP -> {
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun setupMediaSession() {
        mediaSession = MediaSession(this, "SpotificMediaSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    resume()
                }

                override fun onPause() {
                    pause()
                }

                override fun onSkipToNext() {
                    playerManager.skipNext(this@SpotificAudioService)
                }

                override fun onSkipToPrevious() {
                    playerManager.skipPrevious(this@SpotificAudioService)
                }

                override fun onSeekTo(pos: Long) {
                    seekTo(pos.toInt())
                }

                override fun onStop() {
                    stopPlayback()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            isActive = true
        }
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            setOnPreparedListener { mp ->
                Log.d(TAG, "MediaPlayer prepared, starting playback. Duration=${mp.duration}")
                requestAudioFocus()
                mp.start()
                updateMediaSessionPlaybackState(MediaPlaybackState.STATE_PLAYING, mp.currentPosition.toLong())
                updateNotification(isPlaying = true)
                playerManager.onPlaybackStarted(mp.duration.toLong())
            }

            setOnCompletionListener {
                Log.d(TAG, "MediaPlayer track completed")
                updateMediaSessionPlaybackState(MediaPlaybackState.STATE_PAUSED, 0L)
                updateNotification(isPlaying = false)
                playerManager.onPlaybackCompleted(this@SpotificAudioService)
            }

            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                playerManager.onPlaybackError("Playback error: $what")
                false
            }

            setOnBufferingUpdateListener { _, percent ->
                Log.d(TAG, "MediaPlayer buffering: $percent%")
            }
        }
    }

    fun loadAndPlay(streamUrl: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                mediaPlayer?.let { player ->
                    player.reset()
                    player.setDataSource(streamUrl)
                    playerManager.onPlaybackBuffering()
                    updateNotification(isPlaying = false)
                    player.prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting data source: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    playerManager.onPlaybackError("Cannot load stream: ${e.message}")
                }
            }
        }
    }

    fun resume() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                requestAudioFocus()
                player.start()
                updateMediaSessionPlaybackState(MediaPlaybackState.STATE_PLAYING, player.currentPosition.toLong())
                updateNotification(isPlaying = true)
                playerManager.onPlaybackStarted(player.duration.toLong())
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                updateMediaSessionPlaybackState(MediaPlaybackState.STATE_PAUSED, player.currentPosition.toLong())
                updateNotification(isPlaying = false)
                playerManager.onPlaybackPaused()
            }
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        updateMediaSessionPlaybackState(
            if (isPlaying()) MediaPlaybackState.STATE_PLAYING else MediaPlaybackState.STATE_PAUSED,
            positionMs.toLong()
        )
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun getCurrentPosition(): Int = try {
        mediaPlayer?.currentPosition ?: 0
    } catch (e: Exception) { 0 }

    fun getDuration(): Int = try {
        mediaPlayer?.duration ?: 0
    } catch (e: Exception) { 0 }

    private fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        }
        abandonAudioFocus()
        playerManager.onPlaybackPaused()
    }

    private fun updateMediaSessionPlaybackState(state: Int, position: Long) {
        val playbackSpeed = if (state == MediaPlaybackState.STATE_PLAYING) 1.0f else 0.0f
        val stateBuilder = MediaPlaybackState.Builder()
            .setActions(
                MediaPlaybackState.ACTION_PLAY or
                MediaPlaybackState.ACTION_PAUSE or
                MediaPlaybackState.ACTION_PLAY_PAUSE or
                MediaPlaybackState.ACTION_SKIP_TO_NEXT or
                MediaPlaybackState.ACTION_SKIP_TO_PREVIOUS or
                MediaPlaybackState.ACTION_SEEK_TO or
                MediaPlaybackState.ACTION_STOP
            )
            .setState(state, position, playbackSpeed)
        mediaSession?.setPlaybackState(stateBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Spotific Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls background audio playback and media session"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun loadAlbumArt(url: String) {
        if (url.isEmpty()) {
            albumArtBitmap = null
            return
        }
        serviceScope.launch(Dispatchers.IO) {
            try {
                val loader = ImageLoader(this@SpotificAudioService)
                val request = ImageRequest.Builder(this@SpotificAudioService)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    albumArtBitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    withContext(Dispatchers.Main) {
                        updateNotification(isPlaying())
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load album art for notification: ${e.message}")
            }
        }
    }

    private fun updateNotification(isPlaying: Boolean) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPendingIntent = PendingIntent.getService(
            this, 1,
            Intent(this, SpotificAudioService::class.java).apply { action = ACTION_PREVIOUS },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPausePendingIntent = PendingIntent.getService(
            this, 2,
            Intent(this, SpotificAudioService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            this, 3,
            Intent(this, SpotificAudioService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            this, 4,
            Intent(this, SpotificAudioService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseText = if (isPlaying) "Pause" else "Play"

        val mediaStyle = Notification.MediaStyle()
            .setMediaSession(mediaSession?.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        builder
            .setContentTitle(currentTitle)
            .setContentText(currentArtist)
            .setSmallIcon(R.drawable.ic_spotific_logo)
            .setContentIntent(contentPendingIntent)
            .setStyle(mediaStyle)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseText, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPendingIntent)

        albumArtBitmap?.let { bitmap ->
            builder.setLargeIcon(bitmap)
        }

        val notification = builder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()

            focusRequest = request
            audioManager?.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(this)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaPlayer?.setVolume(0.2f, 0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                resume()
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Critical requirement:
        // Do NOT stop audio or foreground service when user closes the app from recents!
        // Audio continues in background with persistent notification.
        Log.d(TAG, "onTaskRemoved: App task closed, keeping audio playback active in foreground service")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        Log.d(TAG, "SpotificAudioService onDestroy")
        playerManager.unregisterService(this)
        serviceScope.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession?.release()
        mediaSession = null
        abandonAudioFocus()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
