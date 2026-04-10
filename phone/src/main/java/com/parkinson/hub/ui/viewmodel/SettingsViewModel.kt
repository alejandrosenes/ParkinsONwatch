package com.parkinson.hub.ui.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.hub.data.repository.WatchSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class SettingsUiState(
    val appVersion: String = "1.0.0",
    val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val isWatchConnected: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTime: String = "",
    val syncProgress: Int = 0,
    val totalRecords: Int = 156,
    val lastExport: String = "Nunca",
    val stravaConnected: Boolean = false,
    val stravaEmail: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val watchSyncRepository: WatchSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        checkWatchConnection()
    }

    private fun checkWatchConnection() {
        viewModelScope.launch {
            try {
                val isConnected = watchSyncRepository.isWatchConnected()
                _uiState.update { it.copy(isWatchConnected = isConnected) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isWatchConnected = false) }
            }
        }
    }

    fun syncWithWatch() {
        if (_uiState.value.isSyncing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncProgress = 0) }

            try {
                for (progress in listOf(10, 30, 50, 70, 90)) {
                    _uiState.update { it.copy(syncProgress = progress) }
                    delay(500)
                }

                val syncResult = watchSyncRepository.syncWithWatch()

                if (syncResult.isSuccess) {
                    val now = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncProgress = 100,
                            lastSyncTime = now.format(formatter)
                        )
                    }
                } else {
                    _uiState.update { it.copy(isSyncing = false, syncProgress = 0) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncing = false, syncProgress = 0) }
            }
        }
    }

    fun refreshWatchConnection() {
        checkWatchConnection()
    }

    fun connectStrava(email: String) {
        _uiState.update { it.copy(stravaConnected = true, stravaEmail = email) }
    }

    fun disconnectStrava() {
        _uiState.update { it.copy(stravaConnected = false, stravaEmail = "") }
    }

    fun exportData() {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            _uiState.update { it.copy(lastExport = now.format(formatter)) }
        }
    }
}
