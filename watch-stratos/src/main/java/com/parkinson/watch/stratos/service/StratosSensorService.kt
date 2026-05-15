package com.parkinson.watch.stratos.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class StratosSensorService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Servicio de sensores optimizado para Amazfit Stratos
        // Sin dependencias de Google Play Services
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
