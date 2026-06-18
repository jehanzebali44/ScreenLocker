package com.example.screen.locker.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screen.locker.domain.repository.HookStyle
import com.example.screen.locker.domain.repository.LockRepository
import com.example.screen.locker.domain.repository.LockType
import com.example.screen.locker.domain.repository.ZipperStyle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: LockRepository) : ViewModel() {

    val lockType: StateFlow<LockType> = repository.getLockType()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LockType.NONE)

    val zipperStyle: StateFlow<ZipperStyle> = repository.getZipperStyle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ZipperStyle.SILVER)

    val hookStyle: StateFlow<HookStyle> = repository.getHookStyle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HookStyle.CLASSIC)

    fun setLockType(type: LockType) {
        viewModelScope.launch {
            repository.setLockType(type)
        }
    }
    
    fun disableLock() {
        viewModelScope.launch {
            repository.setLockType(LockType.NONE)
        }
    }

    fun setZipperStyle(style: ZipperStyle) {
        viewModelScope.launch {
            repository.setZipperStyle(style)
        }
    }

    fun setHookStyle(style: HookStyle) {
        viewModelScope.launch {
            repository.setHookStyle(style)
        }
    }
}
