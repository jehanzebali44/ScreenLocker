package com.example.screen.locker.data.repository

import com.example.screen.locker.data.network.PexelsPhoto
import com.example.screen.locker.data.network.WallpaperApi
import com.example.screen.locker.domain.repository.WallpaperRepository

class WallpaperRepositoryImpl(private val api: WallpaperApi) : WallpaperRepository {
    
    // Replace with your actual Pexels API Key - Go Pexels.com/api
    private val apiKey = "YOUR_PEXELS_API_KEY"

    override suspend fun getWallpapers(category: String): List<PexelsPhoto> {
        return try {
            api.getWallpapers(apiKey, category).photos
        } catch (e: Exception) {
            emptyList()
        }
    }
}
