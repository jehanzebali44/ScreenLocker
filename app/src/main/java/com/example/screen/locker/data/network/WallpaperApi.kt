package com.example.screen.locker.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class WallpaperResponse(
    val photos: List<PexelsPhoto>
)

data class PexelsPhoto(
    val id: Int,
    val src: PexelsSrc,
    val alt: String
)

data class PexelsSrc(
    val large2x: String,
    val portrait: String
)

interface WallpaperApi {
    @GET("search")
    suspend fun getWallpapers(
        @Header("Authorization") apiKey: String,
        @Query("query") category: String,
        @Query("per_page") perPage: Int = 30
    ): WallpaperResponse
}
