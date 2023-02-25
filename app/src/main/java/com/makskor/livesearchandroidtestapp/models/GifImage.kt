package com.makskor.livesearchandroidtestapp.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GifImage (
    @Json(name = "url")
    val url: String,
)