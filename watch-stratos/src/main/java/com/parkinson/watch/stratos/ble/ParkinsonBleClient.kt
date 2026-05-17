package com.parkinson.watch.stratos.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import com.parkinson.shared.models.MedicalEvent
import com.parkinson.shared.models.EventType
import java.util.*

/**
 * Cliente BLE en el reloj que se conecta al teléfono y envía datos.
 */
class ParkinsonBleClient(private val context: Context) {

    companion object {
        // Mismos UUIDs que el servidor
        val SERVICE_UUID: UUID = UUID.fromString("B4E6A8C0-1234-5678-9ABC-DEF012345678")
        val COMMAND_CHAR_UUID: UUID = UUID.fromString("B4E6A8C1-1234-5678-9ABC-DEF012345678")
        val MEDICAL_DATA_CHAR_UUID: UUID = UUID.fromString("B4E6A8C2-1234-5678-9ABC-DEF012345678")
        val STRAVA_ACTIVITY_CHAR_UUID: UUID = UUID.fromString("B4E6A8C3-1234-5678-9ABC-DEF012345678")
        val CONFIG_CHAR_UUID: UUID = UUID.fromString("B4E6A8C4-1234-5678-9ABC-DEF012345678")
        
        private const val TAG = "BLE_CLIENT"
    }

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var gattClient: BluetoothGatt? = null
    private var connectedDevice: BluetoothDevice? = null
    
    private var medicalDataCharacteristic: BluetoothGattCharacteristic? = null
    private var stravaActivityCharacteristic: BluetoothGattCharacteristic? = null
    
    private var isConnected = false
    private var connectionCallback: ((Boolean) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String, onConnectionChange: (Boolean) -> Unit): Boolean {
        connectionCallback = onConnectionChange
        
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
        
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "Bluetooth no disponible")
            return false
        }

        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        if (device == null) {
            Log.e(TAG, "Dispositivo no encontrado: $deviceAddress")
            return false
        }

        connectedDevice = device
        
        gattClient = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, Bluetooth.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }

        return true
    }

    @SuppressLint("MissingPermission")
    fun scanAndConnect(onConnectionChange: (Boolean) -> Unit): Boolean {
        connectionCallback = onConnectionChange
        
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
        
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "Bluetooth no disponible")
            return false
        }

        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: run {
            Log.e(TAG, "Scanner BLE no disponible")
            return false
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                
                // Buscar dispositivo que anuncie nuestro servicio
                val serviceUuids = result.scanRecord?.serviceUuids
                if (serviceUuids?.any { it.uuid == SERVICE_UUID } == true) {
                    Log.i(TAG, "Dispositivo encontrado: ${result.device.name}")
                    scanner.stopScan(this)
                    connectToDevice(result.device.address, onConnectionChange)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "Error en escaneo: $errorCode")
                onConnectionChange(false)
            }
        }

        // Iniciar escaneo con filtro por UUID de servicio
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()
        )

        try {
            scanner.startScan(filters, settings, scanCallback)
            
            // Timeout de 30 segundos
            bluetoothAdapter?.handler?.postDelayed({
                try {
                    scanner.stopScan(scanCallback)
                    if (!isConnected) {
                        Log.w(TAG, "Timeout de escaneo sin encontrar dispositivo")
                        onConnectionChange(false)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al detener escaneo", e)
                }
            }, 30000)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permiso de escaneo denegado", e)
            return false
        }

        return true
    }

    fun disconnect() {
        gattClient?.close()
        gattClient = null
        connectedDevice = null
        isConnected = false
        connectionCallback?.invoke(false)
        Log.i(TAG, "Desconectado")
    }

    @SuppressLint("MissingPermission")
    fun sendMedicalEvent(event: MedicalEvent): Boolean {
        if (!isConnected || medicalDataCharacteristic == null) {
            Log.w(TAG, "No conectado o característica no disponible")
            return false
        }

        try {
            val data = serializeMedicalEvent(event)
            medicalDataCharacteristic?.value = data
            
            val success = gattClient?.writeCharacteristic(medicalDataCharacteristic) == true
            Log.d(TAG, "Evento médico enviado: ${event.type}, éxito: $success")
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar evento médico", e)
            return false
        }
    }

    @SuppressLint("MissingPermission")
    fun sendStravaActivity(activityJson: String): Boolean {
        if (!isConnected || stravaActivityCharacteristic == null) {
            Log.w(TAG, "No conectado o característica no disponible")
            return false
        }

        try {
            // Dividir en paquetes si es muy largo (MTU típico ~20 bytes sin negociación)
            val data = activityJson.toByteArray(Charsets.UTF_8)
            val maxPacketSize = 20 // MTU mínimo seguro
            
            var offset = 0
            while (offset < data.size) {
                val length = minOf(maxPacketSize, data.size - offset)
                val packet = data.sliceArray(offset until offset + length)
                
                stravaActivityCharacteristic?.value = packet
                gattClient?.writeCharacteristic(stravaActivityCharacteristic)
                
                offset += length
                
                // Pequeña pausa entre paquetes
                Thread.sleep(10)
            }
            
            Log.d(TAG, "Actividad Strava enviada (${data.size} bytes)")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar actividad Strava", e)
            return false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d(TAG, "Estado de conexión: $newState")
            
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Conectado al teléfono")
                    isConnected = true
                    // Descubrir servicios
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Desconectado del teléfono")
                    isConnected = false
                    connectedDevice = null
                    connectionCallback?.invoke(false)
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Error al descubrir servicios: $status")
                connectionCallback?.invoke(false)
                return
            }

            val service = gatt.getService(SERVICE_UUID)
            if (service == null) {
                Log.e(TAG, "Servicio Parkinson no encontrado")
                connectionCallback?.invoke(false)
                return
            }

            medicalDataCharacteristic = service.getCharacteristic(MEDICAL_DATA_CHAR_UUID)
            stravaActivityCharacteristic = service.getCharacteristic(STRAVA_ACTIVITY_CHAR_UUID)
            
            if (medicalDataCharacteristic == null || stravaActivityCharacteristic == null) {
                Log.e(TAG, "Características no encontradas")
                connectionCallback?.invoke(false)
                return
            }

            Log.i(TAG, "Servicios y características descubiertos correctamente")
            isConnected = true
            connectionCallback?.invoke(true)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (characteristic.uuid) {
                MEDICAL_DATA_CHAR_UUID -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "Evento médico confirmado por servidor")
                    } else {
                        Log.e(TAG, "Error al escribir evento médico: $status")
                    }
                }
                STRAVA_ACTIVITY_CHAR_UUID -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "Paquete de actividad confirmado")
                    } else {
                        Log.e(TAG, "Error al escribir actividad: $status")
                    }
                }
            }
        }
    }

    private fun serializeMedicalEvent(event: MedicalEvent): ByteArray {
        // Formato binario compacto:
        // [0]: Tipo de evento (byte)
        // [1-4]: Timestamp (int, segundos desde epoch)
        // [5-8]: Severity (float)
        // [9-12]: Duration (int)
        // [13]: HeartRate (byte)
        // [14]: Activity level string length
        // [15...]: Activity level string bytes
        
        val activityBytes = event.context.activityLevel.toByteArray(Charsets.UTF_8)
        val totalSize = 15 + activityBytes.size
        
        return ByteArray(totalSize).apply {
            var index = 0
            
            // Tipo
            this[index++] = event.type.ordinal.toByte()
            
            // Timestamp (convertir a segundos para ahorrar espacio)
            val timestampSec = (event.timestamp / 1000).toInt()
            this[index++] = ((timestampSec ushr 24) and 0xFF).toByte()
            this[index++] = ((timestampSec ushr 16) and 0xFF).toByte()
            this[index++] = ((timestampSec ushr 8) and 0xFF).toByte()
            this[index++] = (timestampSec and 0xFF).toByte()
            
            // Severity (float a bits)
            val severityBits = java.lang.Float.floatToIntBits(event.severity)
            this[index++] = ((severityBits ushr 24) and 0xFF).toByte()
            this[index++] = ((severityBits ushr 16) and 0xFF).toByte()
            this[index++] = ((severityBits ushr 8) and 0xFF).toByte()
            this[index++] = (severityBits and 0xFF).toByte()
            
            // Duration
            this[index++] = ((event.durationSeconds ushr 24) and 0xFF).toByte()
            this[index++] = ((event.durationSeconds ushr 16) and 0xFF).toByte()
            this[index++] = ((event.durationSeconds ushr 8) and 0xFF).toByte()
            this[index++] = (event.durationSeconds and 0xFF).toByte()
            
            // HeartRate
            this[index++] = event.context.avgHeartRate.toByte()
            
            // Activity level string
            this[index++] = activityBytes.size.toByte()
            System.arraycopy(activityBytes, 0, this, index, activityBytes.size)
        }
    }
}
