package com.example.screen.locker.domain.repository

import com.example.screen.locker.data.network.PexelsPhoto

interface WallpaperRepository {
    suspend fun getWallpapers(category: String): List<PexelsPhoto>
}
