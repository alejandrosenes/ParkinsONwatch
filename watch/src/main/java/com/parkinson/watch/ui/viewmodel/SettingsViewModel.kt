package com.parkinson.watch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.watch.domain.usecase.GetConnectionStatusUseCase
import com.parkinson.watch.domain.usecase.GetDeviceInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class SettingsUiState(
    val bleConnected: Boolean = false,
    val wifiConnected: Boolean = false,
    val lastSyncTime: String = "--:--",
    val pendingRecords: Int = 0,
    val appVersion: String = "1.0.0",
    val deviceModel: String = "AOSP Watch"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getConnectionStatusUseCase: GetConnectionStatusUseCase,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadDeviceInfo()
        loadConnectionStatus()
    }

    private fun loadDeviceInfo() {
        viewModelScope.launch {
            try {
                val deviceInfo = getDeviceInfoUseCase()
                _uiState.update {
                    it.copy(
                        appVersion = deviceInfo.appVersion,
                        deviceModel = deviceInfo.model
                    )
                }
            } catch (e: Exception) {
                // Use defaults
            }
        }
    }

    private fun loadConnectionStatus() {
        viewModelScope.launch {
            try {
                getConnectionStatusUseCase().collect { status ->
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    _uiState.update {
                        it.copy(
                            bleConnected = status.bleConnected,
                            wifiConnected = status.wifiConnected,
                            lastSyncTime = status.lastSync?.format(timeFormatter) ?: "--:--",
                            pendingRecords = status.pendingRecords
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }
}
