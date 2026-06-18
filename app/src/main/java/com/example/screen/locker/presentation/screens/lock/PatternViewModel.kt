package com.example.screen.locker.presentation.screens.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screen.locker.domain.repository.LockRepository
import com.example.screen.locker.domain.repository.LockType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PatternViewModel(private val repository: LockRepository) : ViewModel() {

    private val _patternState = MutableStateFlow<List<Int>>(emptyList())
    val patternState: StateFlow<List<Int>> = _patternState.asStateFlow()

    private val _unlockStatus = MutableStateFlow<Boolean?>(null)
    val unlockStatus: StateFlow<Boolean?> = _unlockStatus.asStateFlow()

    fun onPatternInput(pattern: List<Int>) {
        _patternState.value = pattern
        validatePattern(pattern)
    }

    private fun validatePattern(enteredPattern: List<Int>) {
        viewModelScope.launch {
            if (enteredPattern.size >= 4) {
                _unlockStatus.value = true
            } else {
                _unlockStatus.value = false
            }
        }
    }

    fun onPatternSetup(pattern: List<Int>) {
        viewModelScope.launch {
            repository.savePattern(pattern)
            repository.setLockType(LockType.PATTERN)
        }
    }
}
