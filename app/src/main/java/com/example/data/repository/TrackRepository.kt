package com.example.data.repository

import com.example.data.api.SpotificApiService
import com.example.data.local.DownloadedDao
import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteTrackEntity
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class Artist(
    val name: String,
    val imageUrl: String
)

class TrackRepository(
    private val apiService: SpotificApiService,
    private val favoriteDao: FavoriteDao,
    private val downloadedDao: DownloadedDao
) {
    val favorites: Flow<List<Track>> = favoriteDao.getAllFavorites().map { list ->
        list.map { it.toTrack() }
    }

    val downloads: Flow<List<Track>> = downloadedDao.getAllDownloads().map { list ->
        list.map { it.toTrack() }
    }

    fun isFavorite(trackId: String): Flow<Boolean> = favoriteDao.isFavorite(trackId)
    fun isDownloaded(trackId: String): Flow<Boolean> = downloadedDao.isDownloaded(trackId)

    suspend fun toggleFavorite(track: Track) = withContext(Dispatchers.IO) {
        val isFav = favoriteDao.isFavoriteDirect(track.id)
        if (isFav) {
            favoriteDao.deleteFavoriteById(track.id)
        } else {
            favoriteDao.insertFavorite(FavoriteTrackEntity.fromTrack(track))
        }
    }

    suspend fun search(query: String): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchSpotify(query)
            val items = response.result?.map { it.toTrack() } ?: emptyList()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrendingTracks(): Result<List<Track>> = search("Trending Hits")

    suspend fun getPopularTracks(): Result<List<Track>> = search("Today Top Hits")

    suspend fun getGenreTracks(genre: String): Result<List<Track>> = search(genre)

    fun getTopArtists(): List<Artist> = listOf(
        Artist("The Weeknd", "https://i.scdn.co/image/ab6761610000e5eb214f3cf1cbe7139c1e26ffbb"),
        Artist("Taylor Swift", "https://i.scdn.co/image/ab6761610000e5eb5a00969a4698c3132a15fbb0"),
        Artist("Drake", "https://i.scdn.co/image/ab6761610000e5eb4293385d324e238817b44bd5"),
        Artist("Billie Eilish", "https://i.scdn.co/image/ab6761610000e5ebd8b9980db67272cb4d2c3daf"),
        Artist("Dua Lipa", "https://i.scdn.co/image/ab6761610000e5ebd42a27db3286b58553da8858"),
        Artist("Ed Sheeran", "https://i.scdn.co/image/ab6761610000e5eb12a2efab7b8c3d98eb4b2ec7"),
        Artist("Post Malone", "https://i.scdn.co/image/ab6761610000e5ebb5473722956cf5759fe2d486"),
        Artist("Ariana Grande", "https://i.scdn.co/image/ab6761610000e5ebcdce7620dc940db0718684d8")
    )
}
