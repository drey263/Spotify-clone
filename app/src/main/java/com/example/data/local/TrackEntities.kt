package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.model.Track
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorite_tracks")
data class FavoriteTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String,
    val url: String,
    val duration: String? = null,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toTrack(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        thumbnail = thumbnail,
        url = url,
        duration = duration,
        isLocal = false
    )

    companion object {
        fun fromTrack(track: Track): FavoriteTrackEntity = FavoriteTrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            thumbnail = track.thumbnail,
            url = track.url,
            duration = track.duration
        )
    }
}

@Entity(tableName = "downloaded_tracks")
data class DownloadedTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String,
    val url: String,
    val localFilePath: String,
    val duration: String? = null,
    val downloadedAt: Long = System.currentTimeMillis()
) {
    fun toTrack(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        thumbnail = thumbnail,
        url = url,
        duration = duration,
        isLocal = true,
        localFilePath = localFilePath
    )

    companion object {
        fun fromTrack(track: Track, filePath: String): DownloadedTrackEntity = DownloadedTrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            thumbnail = track.thumbnail,
            url = track.url,
            localFilePath = filePath,
            duration = track.duration
        )
    }
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_tracks ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteTrackEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE id = :trackId)")
    fun isFavorite(trackId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE id = :trackId)")
    suspend fun isFavoriteDirect(trackId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteTrackEntity)

    @Delete
    suspend fun deleteFavorite(entity: FavoriteTrackEntity)

    @Query("DELETE FROM favorite_tracks WHERE id = :trackId")
    suspend fun deleteFavoriteById(trackId: String)
}

@Dao
interface DownloadedDao {
    @Query("SELECT * FROM downloaded_tracks ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadedTrackEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_tracks WHERE id = :trackId)")
    fun isDownloaded(trackId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_tracks WHERE id = :trackId)")
    suspend fun isDownloadedDirect(trackId: String): Boolean

    @Query("SELECT * FROM downloaded_tracks WHERE id = :trackId LIMIT 1")
    suspend fun getDownloadById(trackId: String): DownloadedTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(entity: DownloadedTrackEntity)

    @Delete
    suspend fun deleteDownload(entity: DownloadedTrackEntity)

    @Query("DELETE FROM downloaded_tracks WHERE id = :trackId")
    suspend fun deleteDownloadById(trackId: String)
}
