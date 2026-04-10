package com.parkinson.hub.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class LogEntryUiState(
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LogEntryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LogEntryUiState())
    val uiState: StateFlow<LogEntryUiState> = _uiState.asStateFlow()

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
}
