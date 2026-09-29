package com.example.data.api

import android.util.Log
import com.example.model.Track
import kotlinx.coroutines.delay

class StreamResolver(private val apiService: SpotificApiService) {

    companion object {
        private const val TAG = "StreamResolver"
        private const val MAX_PRIMARY_RETRIES = 2
    }

    suspend fun resolveStreamUrl(track: Track): String? {
        // If track is already local, return its file path
        if (track.isLocal && !track.localFilePath.isNullOrEmpty()) {
            return track.localFilePath
        }

        // 1. Try Primary endpoint with retries
        if (track.url.isNotEmpty()) {
            var attempt = 0
            while (attempt <= MAX_PRIMARY_RETRIES) {
                try {
                    Log.d(TAG, "Trying primary endpoint for url=${track.url} (attempt $attempt)")
                    val response = apiService.getStreamUrl(track.url)
                    val streamUrl = response.result?.url ?: response.result?.downloadUrl
                    if (response.status == true && !streamUrl.isNullOrEmpty()) {
                        Log.d(TAG, "Primary endpoint succeeded: $streamUrl")
                        return streamUrl
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Primary endpoint attempt $attempt failed: ${e.message}")
                }
                attempt++
                if (attempt <= MAX_PRIMARY_RETRIES) {
                    delay(500L * attempt)
                }
            }
        }

        // 2. Fallback endpoint: /downloader/spotifyplay?q={title} {artist}
        return try {
            val query = "${track.title} ${track.artist}".trim()
            Log.d(TAG, "Falling back to spotifyplay endpoint with query='$query'")
            val fallbackResponse = apiService.getFallbackStreamUrl(query)
            val fallbackUrl = fallbackResponse.result?.downloadUrl ?: fallbackResponse.result?.url
            if (!fallbackUrl.isNullOrEmpty()) {
                Log.d(TAG, "Fallback endpoint succeeded: $fallbackUrl")
                fallbackUrl
            } else {
                Log.e(TAG, "Fallback endpoint returned empty download URL")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback endpoint failed: ${e.message}", e)
            null
        }
    }
}
