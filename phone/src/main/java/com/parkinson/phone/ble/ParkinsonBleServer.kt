package com.parkinson.phone.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.parkinson.shared.models.MedicalEvent
import com.parkinson.shared.models.EventType
import java.util.*
import kotlin.concurrent.schedule

/**
 * Servidor BLE en el teléfono que recibe datos del reloj.
 * UUIDs personalizados para el servicio de Parkinson.
 */
class ParkinsonBleServer(private val context: Context) {

    companion object {
        // UUID base único para la aplicación
        private val BASE_UUID = UUID.fromString("0000110A-0000-1000-8000-00805F9B34FB")
        
        // UUIDs específicos para cada característica
        val SERVICE_UUID: UUID = UUID.fromString("B4E6A8C0-1234-5678-9ABC-DEF012345678")
        val COMMAND_CHAR_UUID: UUID = UUID.fromString("B4E6A8C1-1234-5678-9ABC-DEF012345678")
        val MEDICAL_DATA_CHAR_UUID: UUID = UUID.fromString("B4E6A8C2-1234-5678-9ABC-DEF012345678")
        val STRAVA_ACTIVITY_CHAR_UUID: UUID = UUID.fromString("B4E6A8C3-1234-5678-9ABC-DEF012345678")
        val CONFIG_CHAR_UUID: UUID = UUID.fromString("B4E6A8C4-1234-5678-9ABC-DEF012345678")
    }

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var gattServer: BluetoothGattServer? = null
    private var connectedDevice: BluetoothDevice? = null
    
    private var medicalDataCallback: ((MedicalEvent) -> Unit)? = null
    private var stravaActivityCallback: ((String) -> Unit)? = null

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            Log.d("BLE_SERVER", "Connection state changed: ${device.address} -> $newState")
            
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device
                Log.i("BLE_SERVER", "Dispositivo conectado: ${device.name}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevice = null
                Log.i("BLE_SERVER", "Dispositivo desconectado")
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.d("BLE_SERVER", "Read request for: ${characteristic.uuid}")
            
            when (characteristic.uuid) {
                COMMAND_CHAR_UUID -> {
                    // Responder con estado actual del servidor
                    gattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        offset,
                        "READY".toByteArray(Charsets.UTF_8)
                    )
                }
                else -> {
                    gattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        offset,
                        null
                    )
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            Log.d("BLE_SERVER", "Write request: ${characteristic.uuid}, size: ${value.size}")
            
            when (characteristic.uuid) {
                MEDICAL_DATA_CHAR_UUID -> {
                    // Recibir evento médico desde el reloj
                    try {
                        val event = parseMedicalEvent(value)
                        Log.i("BLE_SERVER", "Evento médico recibido: ${event.type}")
                        medicalDataCallback?.invoke(event)
                        
                        if (responseNeeded) {
                            gattServer?.sendResponse(
                                device,
                                requestId,
                                BluetoothGatt.GATT_SUCCESS,
                                offset,
                                value
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("BLE_SERVER", "Error al parsear evento médico", e)
                        if (responseNeeded) {
                            gattServer?.sendResponse(
                                device,
                                requestId,
                                BluetoothGatt.GATT_FAILURE,
                                offset,
                                null
                            )
                        }
                    }
                }
                
                STRAVA_ACTIVITY_CHAR_UUID -> {
                    // Recibir actividad de Strava (JSON)
                    try {
                        val activityJson = String(value, Charsets.UTF_8)
                        Log.i("BLE_SERVER", "Actividad Strava recibida: ${activityJson.take(50)}...")
                        stravaActivityCallback?.invoke(activityJson)
                        
                        if (responseNeeded) {
                            gattServer?.sendResponse(
                                device,
                                requestId,
                                BluetoothGatt.GATT_SUCCESS,
                                offset,
                                "OK".toByteArray(Charsets.UTF_8)
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("BLE_SERVER", "Error al procesar actividad Strava", e)
                        if (responseNeeded) {
                            gattServer?.sendResponse(
                                device,
                                requestId,
                                BluetoothGatt.GATT_FAILURE,
                                offset,
                                null
                            )
                        }
                    }
                }
                
                else -> {
                    if (responseNeeded) {
                        gattServer?.sendResponse(
                            device,
                            requestId,
                            BluetoothGatt.GATT_FAILURE,
                            offset,
                            null
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startServer(
        onMedicalData: (MedicalEvent) -> Unit,
        onStravaActivity: (String) -> Unit
    ): Boolean {
        medicalDataCallback = onMedicalData
        stravaActivityCallback = onStravaActivity
        
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
        
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e("BLE_SERVER", "Bluetooth no disponible o desactivado")
            return false
        }

        gattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
        
        if (gattServer == null) {
            Log.e("BLE_SERVER", "No se pudo crear el GATT Server")
            return false
        }

        // Crear servicio personalizado
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        
        // Característica de comandos (lectura/escritura)
        val commandChar = BluetoothGattCharacteristic(
            COMMAND_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or 
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or 
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        
        // Característica de datos médicos (solo escritura desde el reloj)
        val medicalDataChar = BluetoothGattCharacteristic(
            MEDICAL_DATA_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        
        // Característica de actividades Strava (solo escritura)
        val stravaActivityChar = BluetoothGattCharacteristic(
            STRAVA_ACTIVITY_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        
        // Característica de configuración (lectura/escritura)
        val configChar = BluetoothGattCharacteristic(
            CONFIG_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or 
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or 
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        service.addCharacteristic(commandChar)
        service.addCharacteristic(medicalDataChar)
        service.addCharacteristic(stravaActivityChar)
        service.addCharacteristic(configChar)
        
        gattServer?.addService(service)
        
        Log.i("BLE_SERVER", "Servidor BLE iniciado correctamente")
        return true
    }

    fun stopServer() {
        gattServer?.close()
        gattServer = null
        connectedDevice = null
        Log.i("BLE_SERVER", "Servidor BLE detenido")
    }

    @SuppressLint("MissingPermission")
    fun notifyConnectedDevices(message: String) {
        val characteristic = gattServer?.getService(SERVICE_UUID)
            ?.getCharacteristic(COMMAND_CHAR_UUID) ?: return
            
        connectedDevice?.let { device ->
            characteristic.value = message.toByteArray(Charsets.UTF_8)
            gattServer?.notifyCharacteristicChanged(device, characteristic, false)
        }
    }

    private fun parseMedicalEvent(data: ByteArray): MedicalEvent {
        // Formato binario simplificado:
        // [0]: Tipo de evento (byte)
        // [1-4]: Timestamp (long)
        // [5-8]: Severity (float)
        // [9-12]: Duration (int)
        // [13]: HeartRate (byte)
        // [14-...]: Activity level string length + bytes
        
        if (data.size < 14) throw IllegalArgumentException("Datos insuficientes")
        
        val typeIndex = data[0].toInt()
        val eventType = EventType.values().getOrElse(typeIndex) { EventType.OFF_STATE }
        
        val timestamp = with(ByteBuffer.wrap(data, 1, 4)) {
            int.toLong() and 0xFFFFFFFFL
        }
        
        val severity = with(ByteBuffer.wrap(data, 5, 4)) {
            float
        }
        
        val duration = with(ByteBuffer.wrap(data, 9, 4)) {
            int
        }
        
        val heartRate = data[13].toInt() and 0xFF
        
        val activityLevel = if (data.size > 14) {
            val length = data[14].toInt()
            if (data.size > 14 + length) {
                String(data, 15, length.coerceAtMost(data.size - 15), Charsets.UTF_8)
            } else "UNKNOWN"
        } else "UNKNOWN"
        
        return MedicalEvent(
            timestamp = timestamp,
            type = eventType,
            severity = severity,
            durationSeconds = duration,
            context = com.parkinson.shared.models.SensorContext(
                avgHeartRate = heartRate,
                activityLevel = activityLevel,
                sleepQualityScore = null,
                lastMealTime = null,
                lastMedicationTime = null
            )
        )
    }
}

// Helper para ByteBuffer (si no está disponible en la versión target)
private class ByteBuffer private constructor(private val buffer: ByteArray, private var position: Int = 0) {
    
    companion object {
        fun wrap(data: ByteArray, offset: Int, length: Int): ByteBuffer {
            return ByteBuffer(data.sliceArray(offset until offset + length), 0)
        }
    }
    
    val int: Int
        get() {
            val b0 = buffer[position++].toInt() and 0xFF
            val b1 = buffer[position++].toInt() and 0xFF
            val b2 = buffer[position++].toInt() and 0xFF
            val b3 = buffer[position++].toInt() and 0xFF
            return (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
        }
    
    val float: Float
        get() {
            return java.lang.Float.intBitsToFloat(int)
        }
}
