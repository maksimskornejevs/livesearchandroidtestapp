package com.makskor.livesearchandroidtestapp.api

import com.makskor.livesearchandroidtestapp.models.GifImageSearchResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GifImageSearchService {
    @GET("gifs/search")
    fun getResponse(
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int,
        @Query("q") searchTerm: String
    ): Call<GifImageSearchResult>
}