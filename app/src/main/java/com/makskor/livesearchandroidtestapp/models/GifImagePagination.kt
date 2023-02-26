package com.makskor.livesearchandroidtestapp.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GifImagePagination(
    @Json(name = "total_count")
    val totalCount: Int,
    @Json(name = "offset")
    val offset: Int,
)