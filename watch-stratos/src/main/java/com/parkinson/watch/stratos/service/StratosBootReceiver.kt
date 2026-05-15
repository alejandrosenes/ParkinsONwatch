package com.parkinson.watch.stratos.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StratosBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Iniciar servicios después del reinicio del dispositivo
        }
    }
}
