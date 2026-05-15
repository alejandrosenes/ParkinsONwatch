package com.parkinson.hub.data.repository

import android.content.Context
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private var watchIpAddress: String = "192.168.1.100" // Default watch IP
    private val syncPort = 7878

    suspend fun setWatchIpAddress(ip: String) {
        watchIpAddress = ip
    }

    suspend fun isWatchConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectionInfo = wifiManager.connectionInfo
            
            // Check if we're connected to a WiFi network
            if (connectionInfo.networkId == -1) {
                return@withContext false
            }

            // Try to ping the watch
            val address = InetAddress.getByName(watchIpAddress)
            return@withContext address.isReachable(3000)
        } catch (e: Exception) {
            return@withContext false
        }
    }

    suspend fun syncWithWatch(): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            // Try to connect to the watch sync server
            val url = "http://$watchIpAddress:$syncPort/sync"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val jsonResponse = JSONObject(body)
                
                val tremorCount = jsonResponse.optInt("tremorCount", 0)
                val heartRateCount = jsonResponse.optInt("heartRateCount", 0)
                val totalDataCount = tremorCount + heartRateCount
                
                return@withContext Result.success(
                    SyncResult(
                        success = true,
                        message = "Sincronización completada",
                        dataCount = totalDataCount,
                        tremorReadings = tremorCount,
                        heartRateReadings = heartRateCount
                    )
                )
            } else {
                return@withContext Result.failure(Exception("Error de conexión: ${response.code}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Error de sincronización: ${e.message}"))
        }
    }

    suspend fun getWatchStatus(): WatchStatus = withContext(Dispatchers.IO) {
        try {
            val url = "http://$watchIpAddress:$syncPort/status"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val jsonStatus = JSONObject(body)
                
                WatchStatus(
                    isConnected = true,
                    batteryLevel = jsonStatus.optInt("batteryLevel", 0),
                    lastSyncTime = System.currentTimeMillis(),
                    storageUsed = jsonStatus.optLong("storageUsed", 0),
                    isCollecting = jsonStatus.optBoolean("isCollecting", false)
                )
            } else {
                WatchStatus(
                    isConnected = false,
                    batteryLevel = 0,
                    lastSyncTime = 0
                )
            }
        } catch (e: Exception) {
            WatchStatus(
                isConnected = false,
                batteryLevel = 0,
                lastSyncTime = 0
            )
        }
    }

    suspend fun getTremorData(): Result<List<TremorReading>> = withContext(Dispatchers.IO) {
        try {
            val url = "http://$watchIpAddress:$syncPort/tremor"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val jsonResponse = JSONObject(body)
                val dataArray = jsonResponse.getJSONArray("data")
                
                val readings = mutableListOf<TremorReading>()
                for (i in 0 until dataArray.length()) {
                    val jsonReading = dataArray.getJSONObject(i)
                    readings.add(TremorReading(
                        timestamp = jsonReading.getLong("timestamp"),
                        tpiScore = jsonReading.getFloat("tpiScore"),
                        dominantFrequency = jsonReading.getFloat("dominantFrequency"),
                        bandPower3_7 = jsonReading.getFloat("bandPower3_7"),
                        bandPower8_12 = jsonReading.getFloat("bandPower8_12"),
                        rmsAmplitude = jsonReading.getFloat("rmsAmplitude")
                    ))
                }
                
                Result.success(readings)
            } else {
                Result.failure(Exception("Error al obtener datos de temblor"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    suspend fun getHeartRateData(): Result<List<HeartRateReading>> = withContext(Dispatchers.IO) {
        try {
            val url = "http://$watchIpAddress:$syncPort/heartrate"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val jsonResponse = JSONObject(body)
                val dataArray = jsonResponse.getJSONArray("data")
                
                val readings = mutableListOf<HeartRateReading>()
                for (i in 0 until dataArray.length()) {
                    val jsonReading = dataArray.getJSONObject(i)
                    readings.add(HeartRateReading(
                        timestamp = jsonReading.getLong("timestamp"),
                        bpm = jsonReading.getInt("bpm"),
                        rmssd = jsonReading.getFloat("rmssd"),
                        sdnn = jsonReading.getFloat("sdnn"),
                        lfHfRatio = jsonReading.getFloat("lfHfRatio")
                    ))
                }
                
                Result.success(readings)
            } else {
                Result.failure(Exception("Error al obtener datos de frecuencia cardíaca"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }
}

data class SyncResult(
    val success: Boolean,
    val message: String,
    val dataCount: Int,
    val tremorReadings: Int = 0,
    val heartRateReadings: Int = 0
)

data class WatchStatus(
    val isConnected: Boolean,
    val batteryLevel: Int,
    val lastSyncTime: Long,
    val storageUsed: Long = 0,
    val isCollecting: Boolean = false
)

data class TremorReading(
    val timestamp: Long,
    val tpiScore: Float,
    val dominantFrequency: Float,
    val bandPower3_7: Float,
    val bandPower8_12: Float,
    val rmsAmplitude: Float
)

data class HeartRateReading(
    val timestamp: Long,
    val bpm: Int,
    val rmssd: Float,
    val sdnn: Float,
    val lfHfRatio: Float
)
