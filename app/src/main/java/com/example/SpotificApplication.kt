package com.example

import android.app.Application
import com.example.data.api.SpotificApiService
import com.example.data.api.StreamResolver
import com.example.data.downloader.DownloadManager
import com.example.data.local.SpotificDatabase
import com.example.data.repository.TrackRepository
import com.example.service.SpotificPlayerManager

class SpotificApplication : Application() {

    lateinit var database: SpotificDatabase
        private set

    lateinit var apiService: SpotificApiService
        private set

    lateinit var streamResolver: StreamResolver
        private set

    lateinit var downloadManager: DownloadManager
        private set

    lateinit var trackRepository: TrackRepository
        private set

    lateinit var playerManager: SpotificPlayerManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = SpotificDatabase.getDatabase(this)
        apiService = SpotificApiService.create()
        streamResolver = StreamResolver(apiService)
        downloadManager = DownloadManager(this, database.downloadedDao(), streamResolver)
        trackRepository = TrackRepository(apiService, database.favoriteDao(), database.downloadedDao())

        playerManager = SpotificPlayerManager.getInstance()
        playerManager.initialize(streamResolver)
    }

    companion object {
        lateinit var instance: SpotificApplication
            private set
    }
}
