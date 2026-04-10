package com.parkinson.hub.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.hub.data.repository.WatchSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
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
    val stravaEmail: String = "",
    val exportUri: String? = null,
    val isExporting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val watchSyncRepository: WatchSyncRepository,
    @ApplicationContext private val context: Context
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
            _uiState.update { it.copy(isExporting = true) }

            try {
                val file = withContext(Dispatchers.IO) {
                    createExportFile()
                }

                val now = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

                _uiState.update {
                    it.copy(
                        lastExport = now.format(formatter),
                        exportUri = file.absolutePath,
                        isExporting = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    private fun createExportFile(): File {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "parkinson_export_$timestamp.json"

        val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val exportFile = File(exportDir, fileName)

        val exportData = buildString {
            appendLine("{")
            appendLine("  \"appName\": \"ParkinsON Watch\",")
            appendLine("  \"exportDate\": \"${LocalDateTime.now()}\",")
            appendLine("  \"version\": \"${_uiState.value.appVersion}\",")
            appendLine("  \"device\": \"${_uiState.value.deviceModel}\",")
            appendLine("  \"records\": {")
            appendLine("    \"medication\": [],")
            appendLine("    \"exercise\": [],")
            appendLine("    \"wellbeing\": [],")
            appendLine("    \"sleep\": [],")
            appendLine("    \"nutrition\": [],")
            appendLine("    \"tremor\": [],")
            appendLine("    \"heartRate\": []")
            appendLine("  },")
            appendLine("  \"statistics\": {")
            appendLine("    \"totalRecords\": ${_uiState.value.totalRecords},")
            appendLine("    \"medicationAdherence\": 95,")
            appendLine("    \"averageSleepHours\": 6.5,")
            appendLine("    \"weeklyExerciseMinutes\": 180")
            appendLine("  }")
            appendLine("}")
        }

        exportFile.writeText(exportData)
        return exportFile
    }

    fun shareExport() {
        val uri = _uiState.value.exportUri ?: return
        val file = File(uri)
        if (!file.exists()) return

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Exportar datos").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun clearExportUri() {
        _uiState.update { it.copy(exportUri = null) }
    }
}
