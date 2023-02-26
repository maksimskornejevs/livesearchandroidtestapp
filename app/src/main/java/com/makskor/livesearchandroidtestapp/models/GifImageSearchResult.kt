package com.makskor.livesearchandroidtestapp.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GifImageSearchResult (
    @Json(name = "data")
    val results: List<GifImageSearchResultItem>,
    @Json(name = "pagination")
    val pagination: GifImagePagination
)