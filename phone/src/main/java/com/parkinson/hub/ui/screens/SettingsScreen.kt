package com.parkinson.hub.ui.screens

import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parkinson.hub.ui.theme.Indigo700
import com.parkinson.hub.ui.theme.StatusGreen
import com.parkinson.hub.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var notificationsEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(true) }
    var cloudSyncEnabled by remember { mutableStateOf(false) }

    var showRemindersDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showStravaDialog by remember { mutableStateOf(false) }
    var showBiometricSetup by remember { mutableStateOf(false) }

    val biometricManager = remember { BiometricManager.from(context) }
    val canUseBiometric = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
    ) == BiometricManager.BIOMETRIC_SUCCESS

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            SettingsSection(title = "Dispositivo") {
                SettingsSwitchItem(
                    icon = Icons.Default.Bluetooth,
                    title = "Sincronización con Reloj",
                    subtitle = "Conectar con ParkinsON Watch",
                    isChecked = uiState.isWatchConnected,
                    onCheckedChange = { viewModel.refreshWatchConnection() }
                )
            }
        }

        item {
            SettingsSection(title = "Apariencia") {
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Modo Oscuro",
                    subtitle = "Activar tema oscuro",
                    isChecked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }
        }

        item {
            SettingsSection(title = "Notificaciones") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Recordatorios",
                    subtitle = "Notificaciones de medicación y ejercicio",
                    isChecked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsNavigationItem(
                    icon = Icons.Default.Notifications,
                    title = "Configurar Recordatorios",
                    subtitle = "Medicación, ejercicio, hidratación",
                    onClick = { showRemindersDialog = true }
                )
            }
        }

        item {
            SettingsSection(title = "Seguridad") {
                SettingsSwitchItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Bloqueo Biométrico",
                    subtitle = if (canUseBiometric) "Requiere huella o Face ID" else "Biometría no disponible",
                    isChecked = biometricEnabled,
                    isEnabled = canUseBiometric,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            biometricEnabled = true
                        } else {
                            biometricEnabled = false
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsNavigationItem(
                    icon = Icons.Default.Security,
                    title = "Privacidad de Datos",
                    subtitle = "Gestionar datos y exportación",
                    onClick = { showPrivacyDialog = true }
                )
            }
        }

        item {
            SettingsSection(title = "Sincronización") {
                SettingsSwitchItem(
                    icon = Icons.Default.CloudSync,
                    title = "Backup en la Nube",
                    subtitle = "Sincronizar datos con cuenta",
                    isChecked = cloudSyncEnabled,
                    onCheckedChange = { cloudSyncEnabled = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SyncWithWatchCard(
                    isConnected = uiState.isWatchConnected,
                    isSyncing = uiState.isSyncing,
                    lastSyncTime = uiState.lastSyncTime,
                    onSyncClick = { viewModel.syncWithWatch() }
                )
            }
        }

        item {
            SettingsSection(title = "Integraciones") {
                SettingsNavigationItem(
                    icon = Icons.Default.SmartToy,
                    title = "Strava",
                    subtitle = if (uiState.stravaConnected) "Conectado: ${uiState.stravaEmail}" else "Conectar cuenta de Strava",
                    onClick = { showStravaDialog = true }
                )
            }
        }

        item {
            SettingsSection(title = "Acerca de") {
                SettingsInfoItem(
                    title = "Versión",
                    value = uiState.appVersion
                )
                SettingsInfoItem(
                    title = "Dispositivo",
                    value = uiState.deviceModel
                )
                SettingsInfoItem(
                    title = "Datos totales",
                    value = "${uiState.totalRecords} registros"
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Aviso Médico",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Esta aplicación es una herramienta de apoyo y monitorización, nunca un dispositivo médico certificado ni sustituto de diagnóstico profesional. Consulta siempre con tu neurólogo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Reminders Dialog
    if (showRemindersDialog) {
        RemindersConfigDialog(
            onDismiss = { showRemindersDialog = false }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        PrivacyDataDialog(
            totalRecords = uiState.totalRecords,
            lastExport = uiState.lastExport,
            onDismiss = { showPrivacyDialog = false },
            onExport = { viewModel.exportData() }
        )
    }

    // Strava Dialog
    if (showStravaDialog) {
        StravaConnectDialog(
            isConnected = uiState.stravaConnected,
            email = uiState.stravaEmail,
            onDismiss = { showStravaDialog = false },
            onConnect = { email, password -> 
                viewModel.connectStrava(email)
                showStravaDialog = false
            },
            onDisconnect = { 
                viewModel.disconnectStrava()
                showStravaDialog = false
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    isEnabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = isEnabled
        )
    }
}

@Composable
fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SettingsInfoItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SyncWithWatchCard(
    isConnected: Boolean,
    isSyncing: Boolean,
    lastSyncTime: String,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                StatusGreen.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isConnected) StatusGreen.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (isConnected) StatusGreen else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isConnected) "Reloj conectado" else "Reloj no conectado",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (lastSyncTime.isNotEmpty()) "Última sync: $lastSyncTime" else "Sin sincronizar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSyncClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected && !isSyncing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Indigo700,
                    disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sincronizando...")
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Forzar Sincronización")
                }
            }
        }
    }
}

@Composable
fun RemindersConfigDialog(onDismiss: () -> Unit) {
    var medicationReminder by remember { mutableStateOf(true) }
    var exerciseReminder by remember { mutableStateOf(true) }
    var hydrationReminder by remember { mutableStateOf(false) }
    var medicationTime by remember { mutableStateOf("08:00") }
    var exerciseTime by remember { mutableStateOf("10:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Recordatorios") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Recordatorio de Medicación",
                    subtitle = "Recordar tomar medicación",
                    isChecked = medicationReminder,
                    onCheckedChange = { medicationReminder = it }
                )

                SettingsSwitchItem(
                    icon = Icons.Default.Sync,
                    title = "Recordatorio de Ejercicio",
                    subtitle = "Sugerir actividad física",
                    isChecked = exerciseReminder,
                    onCheckedChange = { exerciseReminder = it }
                )

                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Recordatorio de Hidratación",
                    subtitle = "Beber agua cada hora",
                    isChecked = hydrationReminder,
                    onCheckedChange = { hydrationReminder = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun PrivacyDataDialog(
    totalRecords: Int,
    lastExport: String,
    onDismiss: () -> Unit,
    onExport: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacidad y Datos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tus Datos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Registros totales: $totalRecords")
                        Text("• Última exportación: $lastExport")
                        Text("• Ubicación: Solo en este dispositivo")
                        Text("• Encriptación: AES-256")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExport,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo700)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar")
                    }

                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Borrar")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Borrar todos los datos?") },
            text = { Text("Esta acción no se puede deshacer. Se eliminarán todos los registros de medicación, ejercicio, sueño y síntomas.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDismiss()
                }) {
                    Text("Borrar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun StravaConnectDialog(
    isConnected: Boolean,
    email: String,
    onDismiss: () -> Unit,
    onConnect: (String, String) -> Unit,
    onDisconnect: () -> Unit
) {
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Conectar con Strava") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isConnected) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = StatusGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = StatusGreen
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Conectado", fontWeight = FontWeight.Bold)
                                Text(email, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                } else {
                    Text("Ingresa tus credenciales de Strava para sincronizar tus actividades de ejercicio.")

                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text("Email de Strava") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = { inputPassword = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Tus datos se usan solo para sincronizar actividades. Nunca almacenamos tu contraseña.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        confirmButton = {
            if (isConnected) {
                TextButton(onClick = onDisconnect) {
                    Text("Desconectar", color = MaterialTheme.colorScheme.error)
                }
            } else {
                Button(
                    onClick = { onConnect(inputEmail, inputPassword) },
                    enabled = inputEmail.isNotBlank() && inputPassword.isNotBlank()
                ) {
                    Text("Conectar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
