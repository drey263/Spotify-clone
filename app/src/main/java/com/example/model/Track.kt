package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String,
    val url: String,
    val duration: String? = null,
    val isLocal: Boolean = false,
    val localFilePath: String? = null
)
