package com.example.data.downloader

import android.content.Context
import android.util.Log
import com.example.data.api.StreamResolver
import com.example.data.local.DownloadedDao
import com.example.data.local.DownloadedTrackEntity
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadManager(
    private val context: Context,
    private val downloadedDao: DownloadedDao,
    private val streamResolver: StreamResolver,
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    companion object {
        private const val TAG = "DownloadManager"
    }

    private val _downloadingTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingTrackIds: StateFlow<Set<String>> = _downloadingTrackIds.asStateFlow()

    suspend fun downloadTrack(track: Track): Boolean = withContext(Dispatchers.IO) {
        if (_downloadingTrackIds.value.contains(track.id)) {
            return@withContext false
        }

        // Check if already downloaded
        if (downloadedDao.isDownloadedDirect(track.id)) {
            return@withContext true
        }

        _downloadingTrackIds.value = _downloadingTrackIds.value + track.id

        try {
            // Resolve direct stream URL
            val streamUrl = streamResolver.resolveStreamUrl(track)
            if (streamUrl.isNullOrEmpty()) {
                Log.e(TAG, "Cannot download track ${track.title}: failed to resolve stream URL")
                return@withContext false
            }

            // Create downloads directory
            val downloadsDir = File(context.filesDir, "downloads").apply {
                if (!exists()) mkdirs()
            }

            // Safe filename based on track ID or hash
            val safeId = track.id.replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(50)
            val outputFile = File(downloadsDir, "${safeId}_${System.currentTimeMillis()}.mp3")

            // Download bytes
            val request = Request.Builder().url(streamUrl).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful || response.body == null) {
                Log.e(TAG, "Download failed with response code ${response.code}")
                return@withContext false
            }

            response.body!!.byteStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Save to database
            val entity = DownloadedTrackEntity.fromTrack(track, outputFile.absolutePath)
            downloadedDao.insertDownload(entity)
            Log.d(TAG, "Successfully downloaded ${track.title} to ${outputFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading ${track.title}: ${e.message}", e)
            false
        } finally {
            _downloadingTrackIds.value = _downloadingTrackIds.value - track.id
        }
    }

    suspend fun deleteDownload(trackId: String) = withContext(Dispatchers.IO) {
        val entity = downloadedDao.getDownloadById(trackId)
        if (entity != null) {
            try {
                val file = File(entity.localFilePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete file: ${e.message}")
            }
            downloadedDao.deleteDownloadById(trackId)
        }
    }
}
