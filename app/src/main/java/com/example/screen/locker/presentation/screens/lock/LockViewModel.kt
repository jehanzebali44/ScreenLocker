package com.example.screen.locker.presentation.screens.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screen.locker.domain.repository.HookStyle
import com.example.screen.locker.domain.repository.LockRepository
import com.example.screen.locker.domain.repository.LockType
import com.example.screen.locker.domain.repository.ZipperStyle
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LockViewModel(
    private val repository: LockRepository
) : ViewModel() {

    private val _pinState = MutableStateFlow("")
    val pinState: StateFlow<String> = _pinState.asStateFlow()

    private val _unlockStatus = MutableStateFlow<Boolean?>(null)
    val unlockStatus: StateFlow<Boolean?> = _unlockStatus.asStateFlow()

    private var failedAttempts = 0

    val lockType: StateFlow<LockType> = repository.getLockType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, LockType.NONE)

    val zipperStyle: StateFlow<ZipperStyle> = repository.getZipperStyle()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ZipperStyle.SILVER)

    val hookStyle: StateFlow<HookStyle> = repository.getHookStyle()
        .stateIn(viewModelScope, SharingStarted.Eagerly, HookStyle.CLASSIC)

    val customWallpaper: StateFlow<String?> = repository.getCustomWallpaper()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _zipperUnlocked = MutableStateFlow(false)
    val zipperUnlocked: StateFlow<Boolean> = _zipperUnlocked.asStateFlow()

    fun onZipperComplete() {
        viewModelScope.launch {
            val type = lockType.value
            if (type == LockType.ZIPPER) {
                _unlockStatus.value = true
            } else {
                _zipperUnlocked.value = true
            }
        }
    }

    fun onPinInput(digit: String) {
        if (_pinState.value.length < 4) {
            _pinState.value += digit
        }
        if (_pinState.value.length == 4) {
            validatePin(_pinState.value)
        }
    }

    fun clearPin() {
        _pinState.value = ""
    }

    private fun validatePin(enteredPin: String) {
        viewModelScope.launch {
            val savedPin = repository.getPin().first()
            if (savedPin != null) {
                if (enteredPin == savedPin) {
                    _unlockStatus.value = true
                    failedAttempts = 0
                } else {
                    handleFailure()
                }
            } else {
                _unlockStatus.value = true
            }
        }
    }

    fun validatePattern(enteredPattern: List<Int>) {
        viewModelScope.launch {
            val savedPattern = repository.getPattern().first()
            if (savedPattern != null) {
                if (enteredPattern == savedPattern) {
                    _unlockStatus.value = true
                    failedAttempts = 0
                } else {
                    handleFailure()
                }
            } else {
                _unlockStatus.value = true
            }
        }
    }

    private fun handleFailure() {
        _unlockStatus.value = false
        _pinState.value = ""
        failedAttempts++
    }

    fun onPinSetup(pin: String) {
        viewModelScope.launch {
            repository.savePin(pin)
            repository.setLockType(LockType.PIN)
        }
    }
}
