package com.example.data.api

import com.example.model.Track
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SearchResponse(
    val status: Boolean? = null,
    val result: List<SpotifySearchItem>? = null
)

@JsonClass(generateAdapter = true)
data class SpotifySearchItem(
    val title: String? = null,
    val artist: String? = null,
    val thumbnail: String? = null,
    val url: String? = null,
    val duration: String? = null
) {
    fun toTrack(): Track {
        val safeTitle = title ?: "Unknown Title"
        val safeArtist = artist ?: "Unknown Artist"
        val safeUrl = url ?: ""
        // Use track URL or a stable hash as unique id
        val trackId = if (safeUrl.isNotEmpty()) safeUrl else "${safeTitle}_$safeArtist"
        return Track(
            id = trackId,
            title = safeTitle,
            artist = safeArtist,
            thumbnail = thumbnail ?: "",
            url = safeUrl,
            duration = duration,
            isLocal = false
        )
    }
}

@JsonClass(generateAdapter = true)
data class DownloaderResponse(
    val status: Boolean? = null,
    val result: DownloaderResult? = null
)

@JsonClass(generateAdapter = true)
data class DownloaderResult(
    val url: String? = null,
    @Json(name = "download_url") val downloadUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyPlayResponse(
    val status: Boolean? = null,
    val result: SpotifyPlayResult? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyPlayResult(
    val url: String? = null,
    @Json(name = "download_url") val downloadUrl: String? = null
)

interface SpotificApiService {
    @GET("search/spotify")
    suspend fun searchSpotify(@Query("q") query: String): SearchResponse

    @GET("downloader/spotify")
    suspend fun getStreamUrl(@Query("url") url: String): DownloaderResponse

    @GET("downloader/spotifyplay")
    suspend fun getFallbackStreamUrl(@Query("q") query: String): SpotifyPlayResponse

    companion object {
        private const val BASE_URL = "https://api.nexray.eu.cc/"

        fun create(): SpotificApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SpotificApiService::class.java)
        }
    }
}
