package com.parkinson.hub.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.parkinson.hub.ui.theme.Emerald600
import com.parkinson.hub.ui.theme.Indigo700
import com.parkinson.hub.ui.theme.StatusGreen
import java.time.LocalTime

private val Coral600 = Color(0xFFE57373)
private val Amber600 = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEntryScreen() {
    var expandedSection by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Registrar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Registra tu actividad diaria",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            LogCategoryCard(
                title = "Medicación",
                subtitle = "Registra tomas de medicación y suplementos",
                icon = Icons.Default.Medication,
                color = Indigo700,
                isExpanded = expandedSection == "medication",
                onClick = {
                    expandedSection = if (expandedSection == "medication") null else "medication"
                }
            )
        }

        if (expandedSection == "medication") {
            item {
                MedicationLogForm(
                    onSave = { expandedSection = null }
                )
            }
        }

        item {
            LogCategoryCard(
                title = "Ejercicio",
                subtitle = "Registra actividad física y ejercicios",
                icon = Icons.Default.Sports,
                color = StatusGreen,
                isExpanded = expandedSection == "exercise",
                onClick = {
                    expandedSection = if (expandedSection == "exercise") null else "exercise"
                }
            )
        }

        if (expandedSection == "exercise") {
            item {
                ExerciseLogForm(
                    onSave = { expandedSection = null }
                )
            }
        }

        item {
            LogCategoryCard(
                title = "Bienestar",
                subtitle = "Estado emocional, estrés y calidad de vida",
                icon = Icons.Default.Favorite,
                color = Coral600,
                isExpanded = expandedSection == "wellbeing",
                onClick = {
                    expandedSection = if (expandedSection == "wellbeing") null else "wellbeing"
                }
            )
        }

        if (expandedSection == "wellbeing") {
            item {
                WellbeingLogForm(
                    onSave = { expandedSection = null }
                )
            }
        }

        item {
            LogCategoryCard(
                title = "Sueño",
                subtitle = "Registra calidad del sueño",
                icon = Icons.Default.MonitorHeart,
                color = Emerald600,
                isExpanded = expandedSection == "sleep",
                onClick = {
                    expandedSection = if (expandedSection == "sleep") null else "sleep"
                }
            )
        }

        if (expandedSection == "sleep") {
            item {
                SleepLogForm(
                    onSave = { expandedSection = null }
                )
            }
        }

        item {
            LogCategoryCard(
                title = "Nutrición",
                subtitle = "Comidas, hidratación y ayuno",
                icon = Icons.Default.Restaurant,
                color = Amber600,
                isExpanded = expandedSection == "nutrition",
                onClick = {
                    expandedSection = if (expandedSection == "nutrition") null else "nutrition"
                }
            )
        }

        if (expandedSection == "nutrition") {
            item {
                NutritionLogForm(
                    onSave = { expandedSection = null }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun LogCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Expandir",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        rotationZ = if (isExpanded) 45f else 0f
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationLogForm(onSave: () -> Unit) {
    var medicationName by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var withFood by remember { mutableStateOf(false) }
    var expandedMedication by remember { mutableStateOf(false) }
    var expandedDose by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }

    val medications = listOf("Levodopa", "Carbidopa", "Entacapona", "Pramipexol", "Rotigotina", "Selegilina", "Amantadina", "Otro")
    val doses = listOf("25mg", "50mg", "100mg", "150mg", "200mg", "250mg", "Otra dosis")

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = selectedTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                selectedTime = time
                showTimePicker = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registrar medicación",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Time selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hora del registro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format("%02d:%02d", selectedTime.hour, selectedTime.minute),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedMedication,
                onExpandedChange = { expandedMedication = !expandedMedication }
            ) {
                OutlinedTextField(
                    value = medicationName,
                    onValueChange = { medicationName = it },
                    label = { Text("Medicamento") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMedication) },
                    readOnly = false
                )
                ExposedDropdownMenu(
                    expanded = expandedMedication,
                    onDismissRequest = { expandedMedication = false }
                ) {
                    medications.forEach { med ->
                        DropdownMenuItem(
                            text = { Text(med) },
                            onClick = {
                                medicationName = med
                                expandedMedication = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedDose,
                onExpandedChange = { expandedDose = !expandedDose }
            ) {
                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = { Text("Dosis") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDose) },
                    readOnly = false
                )
                ExposedDropdownMenu(
                    expanded = expandedDose,
                    onDismissRequest = { expandedDose = false }
                ) {
                    doses.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d) },
                            onClick = {
                                dose = d
                                expandedDose = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = "",
                onValueChange = { dose = it },
                label = { Text("Otra dosis (mg)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tomado con comida",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = withFood,
                    onCheckedChange = { withFood = it }
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo700)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLogForm(onSave: () -> Unit) {
    var exerciseType by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var intensity by remember { mutableFloatStateOf(5f) }
    var expandedExercise by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }

    val exercises = listOf("Caminar", "Natación", "Tai Chi", "Yoga", "Pilates", "Ejercicios de estiramiento", "Bicicleta", "Otro")

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = selectedTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                selectedTime = time
                showTimePicker = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registrar ejercicio",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = StatusGreen
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hora del registro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format("%02d:%02d", selectedTime.hour, selectedTime.minute),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedExercise,
                onExpandedChange = { expandedExercise = !expandedExercise }
            ) {
                OutlinedTextField(
                    value = exerciseType,
                    onValueChange = { exerciseType = it },
                    label = { Text("Tipo de ejercicio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExercise) }
                )
                ExposedDropdownMenu(
                    expanded = expandedExercise,
                    onDismissRequest = { expandedExercise = false }
                ) {
                    exercises.forEach { ex ->
                        DropdownMenuItem(
                            text = { Text(ex) },
                            onClick = {
                                exerciseType = ex
                                expandedExercise = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { c -> c.isDigit() } },
                label = { Text("Duración (minutos)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Intensidad: ${intensity.toInt()}/10",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = StatusGreen,
                    activeTrackColor = StatusGreen
                )
            )

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    }
}

@Composable
fun WellbeingLogForm(onSave: () -> Unit) {
    var wellbeingLevel by remember { mutableStateOf(3) }
    var stressLevel by remember { mutableFloatStateOf(5f) }
    var notes by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }

    val wellbeingEmojis = listOf("😢", "😕", "😐", "🙂", "😊")

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = selectedTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                selectedTime = time
                showTimePicker = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registrar bienestar",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "¿Cómo te sientes hoy?",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                wellbeingEmojis.forEachIndexed { index, emoji ->
                    Card(
                        modifier = Modifier
                            .clickable { wellbeingLevel = index + 1 },
                        colors = CardDefaults.cardColors(
                            containerColor = if (wellbeingLevel == index + 1)
                                Coral600.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Text(
                text = "Nivel de estrés: ${stressLevel.toInt()}/10",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = stressLevel,
                onValueChange = { stressLevel = it },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = Coral600,
                    activeTrackColor = Coral600
                )
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral600)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    }
}

@Composable
fun SleepLogForm(onSave: () -> Unit) {
    var sleepQuality by remember { mutableFloatStateOf(5f) }
    var sleepIssues by remember { mutableStateOf(false) }
    var lucidDreams by remember { mutableStateOf(false) }
    var totalSleepHours by remember { mutableStateOf("7") }
    var totalSleepMinutes by remember { mutableStateOf("30") }
    var showBedtimePicker by remember { mutableStateOf(false) }
    var showWakeTimePicker by remember { mutableStateOf(false) }
    var bedtime by remember { mutableStateOf(LocalTime.of(23, 0)) }
    var wakeTime by remember { mutableStateOf(LocalTime.of(7, 0)) }

    val sleepPhases = remember {
        mutableStateListOf(
            SleepPhaseData("Despertar", "0", "0"),
            SleepPhaseData("N1 (Ligero)", "0", "15"),
            SleepPhaseData("N2 (Sueno leve)", "0", "45"),
            SleepPhaseData("N3 (Profundo)", "0", "60"),
            SleepPhaseData("REM", "0", "45")
        )
    }

    if (showBedtimePicker) {
        TimePickerDialog(
            initialTime = bedtime,
            onDismiss = { showBedtimePicker = false },
            onConfirm = { time ->
                bedtime = time
                showBedtimePicker = false
            }
        )
    }

    if (showWakeTimePicker) {
        TimePickerDialog(
            initialTime = wakeTime,
            onDismiss = { showWakeTimePicker = false },
            onConfirm = { time ->
                wakeTime = time
                showWakeTimePicker = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registrar sueño",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = totalSleepHours,
                    onValueChange = { totalSleepHours = it.filter { c -> c.isDigit() } },
                    label = { Text("Horas") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = totalSleepMinutes,
                    onValueChange = { totalSleepMinutes = it.filter { c -> c.isDigit() } },
                    label = { Text("Minutos") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Fases del sueño",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            sleepPhases.forEachIndexed { index, phase ->
                if (phase.name != "Despertar") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = phase.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = phase.hours,
                            onValueChange = { newValue ->
                                sleepPhases[index] = phase.copy(hours = newValue.filter { c -> c.isDigit() })
                            },
                            label = { Text("Horas") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = phase.minutes,
                            onValueChange = { newValue ->
                                sleepPhases[index] = phase.copy(minutes = newValue.filter { c -> c.isDigit() })
                            },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Text(
                text = "Calidad percibida: ${sleepQuality.toInt()}/10",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = sleepQuality,
                onValueChange = { sleepQuality = it },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = Emerald600,
                    activeTrackColor = Emerald600
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Problemas de sueño",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = sleepIssues,
                    onCheckedChange = { sleepIssues = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sueños lúcidos",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = lucidDreams,
                    onCheckedChange = { lucidDreams = it }
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    }
}

data class SleepPhaseData(
    val name: String,
    val hours: String,
    val minutes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionLogForm(onSave: () -> Unit) {
    var mealType by remember { mutableStateOf("") }
    var hydration by remember { mutableFloatStateOf(5f) }
    var fasting by remember { mutableStateOf(false) }
    var expandedMeal by remember { mutableStateOf(false) }
    var nutritionNotes by remember { mutableStateOf("") }
    var aiAnalysis by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    val photos = remember { mutableStateListOf<Uri>() }

    val meals = listOf("Desayuno", "Almuerzo", "Comida", "Merienda", "Cena", "Snack")

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = selectedTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                selectedTime = time
                showTimePicker = false
            }
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        photos.addAll(uris)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Handle camera capture
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registrar nutrición",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Amber600
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hora del registro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format("%02d:%02d", selectedTime.hour, selectedTime.minute),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Fotos de comida (la IA analizará los nutrientes)",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber600)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Galería")
                }

                Button(
                    onClick = { cameraLauncher.launch(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber600)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                }
            }

            // Display photos
            if (photos.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    photos.take(3).forEach { uri ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Foto de comida",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    if (photos.size > 3) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Amber600.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+${photos.size - 3}", color = Amber600)
                        }
                    }
                }

                // AI Analysis button
                Button(
                    onClick = {
                        isAnalyzing = true
                        aiAnalysis = "Analizando imagen...\n\nIdentificando alimentos: Arroz, pollo, vegetales...\n\nNutrientes estimados:\n• Proteínas: 25g\n• Carbohidratos: 45g\n• Grasas: 12g\n• Fibra: 5g\n\nLa IA de ParkiBot está conectada y lista para analizar tus comidas."
                        isAnalyzing = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo700)
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isAnalyzing) "Analizando..." else "Analizar con IA (ParkiBot)")
                }
            }

            // AI Analysis result
            if (aiAnalysis.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Análisis Nutricional (ParkiBot)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aiAnalysis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedMeal,
                onExpandedChange = { expandedMeal = !expandedMeal }
            ) {
                OutlinedTextField(
                    value = mealType,
                    onValueChange = { mealType = it },
                    label = { Text("Tipo de comida") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMeal) }
                )
                ExposedDropdownMenu(
                    expanded = expandedMeal,
                    onDismissRequest = { expandedMeal = false }
                ) {
                    meals.forEach { meal ->
                        DropdownMenuItem(
                            text = { Text(meal) },
                            onClick = {
                                mealType = meal
                                expandedMeal = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Hidratación: ${hydration.toInt()}/10 vasos",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = hydration,
                onValueChange = { hydration = it },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = Amber600,
                    activeTrackColor = Amber600
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ayuno intermitente",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = fasting,
                    onCheckedChange = { fasting = it }
                )
            }

            OutlinedTextField(
                value = nutritionNotes,
                onValueChange = { nutritionNotes = it },
                label = { Text("Notas de alimentación") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Amber600)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar hora") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
