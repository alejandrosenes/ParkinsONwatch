package com.parkinson.watch.data.repository

import android.content.Context
import android.os.Build
import com.parkinson.watch.domain.model.ConnectionStatus
import com.parkinson.watch.domain.model.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _connectionStatus = MutableStateFlow(
        ConnectionStatus(
            bleConnected = false,
            wifiConnected = false,
            lastSync = null,
            pendingRecords = 0
        )
    )

    fun getConnectionStatus(): Flow<ConnectionStatus> = _connectionStatus.asStateFlow()

    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            manufacturer = Build.MANUFACTURER,
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            appVersion = getAppVersion(),
            hasGyroscope = context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_SENSOR_GYROSCOPE),
            hasHeartRate = context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_SENSOR_HEART_RATE),
            hasSpO2 = false,
            hasGps = context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LOCATION_GPS)
        )
    }

    private fun getAppVersion(): String {
        return try {
            @Suppress("DEPRECATION")
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}
