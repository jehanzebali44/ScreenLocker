package com.example.screen.locker.domain.repository

import kotlinx.coroutines.flow.Flow

enum class LockType {
    NONE, PIN, PATTERN, ZIPPER, ZIP_PIN, ZIP_PATTERN
}

enum class ZipperStyle {
    GOLD, SILVER, BLACK, RAINBOW
}

enum class HookStyle {
    CLASSIC, ROUND, SQUARE, HEART
}

interface LockRepository {
    fun getPin(): Flow<String?>
    suspend fun savePin(pin: String?)
    fun getPattern(): Flow<List<Int>?>
    suspend fun savePattern(pattern: List<Int>?)
    fun getLockType(): Flow<LockType>
    suspend fun setLockType(type: LockType)
    
    fun getZipperStyle(): Flow<ZipperStyle>
    suspend fun setZipperStyle(style: ZipperStyle)
    fun getHookStyle(): Flow<HookStyle>
    suspend fun setHookStyle(style: HookStyle)
    
    fun getCustomWallpaper(): Flow<String?>
    suspend fun setCustomWallpaper(url: String?)
}
