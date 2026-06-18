package com.example.screen.locker.presentation.screens.wallpaper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screen.locker.data.network.PexelsPhoto
import com.example.screen.locker.domain.repository.LockRepository
import com.example.screen.locker.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WallpaperViewModel(
    private val wallpaperRepository: WallpaperRepository,
    private val lockRepository: LockRepository
) : ViewModel() {

    private val _wallpapers = MutableStateFlow<List<PexelsPhoto>>(emptyList())
    val wallpapers: StateFlow<List<PexelsPhoto>> = _wallpapers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Nature")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    private val allCategories = listOf("Nature", "Abstract", "Space", "Dark", "Minimal", "Architecture", "Animals")

    init {
        checkAllCategories()
        fetchWallpapers(_selectedCategory.value)
    }

    private fun checkAllCategories() {
        viewModelScope.launch {
            val verified = mutableListOf<String>()
            // This is a bit heavy, but satisfies the requirement to only show categories with content.
            // In a real production app, we might do this differently or just assume these major categories work.
            for (category in allCategories) {
                val photos = wallpaperRepository.getWallpapers(category)
                if (photos.isNotEmpty()) {
                    verified.add(category)
                }
            }
            _availableCategories.value = verified
            if (_availableCategories.value.isNotEmpty() && !verified.contains(_selectedCategory.value)) {
                onCategorySelected(verified.first())
            }
        }
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        fetchWallpapers(category)
    }

    private fun fetchWallpapers(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _wallpapers.value = wallpaperRepository.getWallpapers(category)
            _isLoading.value = false
        }
    }

    fun setCustomWallpaper(url: String?) {
        viewModelScope.launch {
            lockRepository.setCustomWallpaper(url)
        }
    }
}
