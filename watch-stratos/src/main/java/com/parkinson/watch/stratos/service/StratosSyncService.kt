package com.parkinson.watch.stratos.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class StratosSyncService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Servicio de sincronización optimizado para Amazfit Stratos
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
