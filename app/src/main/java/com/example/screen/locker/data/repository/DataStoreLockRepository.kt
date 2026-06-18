package com.example.screen.locker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.screen.locker.domain.repository.HookStyle
import com.example.screen.locker.domain.repository.LockRepository
import com.example.screen.locker.domain.repository.LockType
import com.example.screen.locker.domain.repository.ZipperStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lock_settings")

class DataStoreLockRepository(private val context: Context) : LockRepository {

    private val pinKey = stringPreferencesKey("lock_pin")
    private val patternKey = stringPreferencesKey("lock_pattern")
    private val lockTypeKey = stringPreferencesKey("lock_type")
    private val zipperStyleKey = stringPreferencesKey("zipper_style")
    private val hookStyleKey = stringPreferencesKey("hook_style")
    private val customWallpaperKey = stringPreferencesKey("custom_wallpaper")

    override fun getPin(): Flow<String?> = context.dataStore.data.map { it[pinKey] }

    override suspend fun savePin(pin: String?) {
        context.dataStore.edit { 
            if (pin == null) it.remove(pinKey) else it[pinKey] = pin 
        }
    }

    override fun getPattern(): Flow<List<Int>?> = context.dataStore.data.map { 
        it[patternKey]?.split(",")?.filter { s -> s.isNotEmpty() }?.map { s -> s.toInt() }
    }

    override suspend fun savePattern(pattern: List<Int>?) {
        context.dataStore.edit {
            if (pattern == null) it.remove(patternKey) else it[patternKey] = pattern.joinToString(",")
        }
    }

    override fun getLockType(): Flow<LockType> = context.dataStore.data.map { 
        val name = it[lockTypeKey] ?: LockType.NONE.name
        try { LockType.valueOf(name) } catch (e: Exception) { LockType.NONE }
    }

    override suspend fun setLockType(type: LockType) {
        context.dataStore.edit { it[lockTypeKey] = type.name }
    }

    override fun getZipperStyle(): Flow<ZipperStyle> = context.dataStore.data.map { 
        val name = it[zipperStyleKey] ?: ZipperStyle.SILVER.name
        try { ZipperStyle.valueOf(name) } catch (e: Exception) { ZipperStyle.SILVER }
    }

    override suspend fun setZipperStyle(style: ZipperStyle) {
        context.dataStore.edit { it[zipperStyleKey] = style.name }
    }

    override fun getHookStyle(): Flow<HookStyle> = context.dataStore.data.map { 
        val name = it[hookStyleKey] ?: HookStyle.CLASSIC.name
        try { HookStyle.valueOf(name) } catch (e: Exception) { HookStyle.CLASSIC }
    }

    override suspend fun setHookStyle(style: HookStyle) {
        context.dataStore.edit { it[hookStyleKey] = style.name }
    }

    override fun getCustomWallpaper(): Flow<String?> = context.dataStore.data.map { it[customWallpaperKey] }

    override suspend fun setCustomWallpaper(url: String?) {
        context.dataStore.edit {
            if (url == null) it.remove(customWallpaperKey) else it[customWallpaperKey] = url
        }
    }
}
