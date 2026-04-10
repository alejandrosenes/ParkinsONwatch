package com.parkinson.watch.data.repository

import com.parkinson.watch.data.local.HealthDao
import com.parkinson.watch.data.local.entity.MedicationEntryEntity
import com.parkinson.watch.data.local.entity.MedicationScheduleEntity
import com.parkinson.watch.data.local.entity.WellbeingEntity
import com.parkinson.watch.domain.model.Alert
import com.parkinson.watch.domain.model.DailySummary
import com.parkinson.watch.domain.model.HourlySummary
import com.parkinson.watch.domain.model.MedicationDose
import com.parkinson.watch.domain.model.MedicationEntry
import com.parkinson.watch.domain.model.MedicationSchedule
import com.parkinson.watch.domain.model.WellbeingEntry
import com.parkinson.watch.domain.model.WellbeingLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepository @Inject constructor(
    private val healthDao: HealthDao
) {
    fun getMedicationSchedules(): Flow<List<MedicationSchedule>> = 
        healthDao.getAllMedicationSchedules().map { entities ->
            entities.map { entity ->
                MedicationSchedule(
                    id = entity.id,
                    name = entity.name,
                    dose = entity.dose,
                    scheduleTimes = entity.scheduleTimes.split(",").map { 
                        LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                    },
                    lastTaken = entity.lastTaken?.let { 
                        LocalDateTime.of(LocalDate.now(), LocalTime.parse(it))
                    }
                )
            }
        }

    fun getMedicationLogForDate(date: LocalDate): Flow<List<MedicationEntry>> =
        healthDao.getMedicationLogForDate(
            date.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC),
            date.plusDays(1).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC)
        ).map { entities ->
            entities.map { entity ->
                MedicationEntry(
                    id = entity.id,
                    medicationId = entity.medicationId,
                    medicationName = entity.medicationName,
                    dose = entity.dose,
                    time = LocalDateTime.ofEpochSecond(
                        entity.timestamp,
                        0,
                        java.time.ZoneOffset.UTC
                    ),
                    withFood = entity.withFood
                )
            }
        }

    suspend fun logWellbeing(wellbeing: String) {
        val level = when (wellbeing) {
            "good" -> WellbeingLevel.GOOD
            "fair" -> WellbeingLevel.FAIR
            else -> WellbeingLevel.POOR
        }
        healthDao.insertWellbeing(
            WellbeingEntity(
                timestamp = System.currentTimeMillis(),
                level = level.name,
                note = null
            )
        )
    }

    suspend fun logMedicationTaken() {
        // Implementation would get next scheduled medication and log it
        healthDao.insertMedicationLog(
            MedicationEntryEntity(
                timestamp = System.currentTimeMillis(),
                medicationId = 1,
                medicationName = "Levodopa",
                dose = "100mg",
                withFood = false
            )
        )
    }

    fun getDailySummary(date: LocalDate): Flow<DailySummary> {
        val startOfDay = date.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC)
        val endOfDay = date.plusDays(1).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC)

        return healthDao.getDailySummary(startOfDay, endOfDay).map { summary ->
            val hourlyData = (0..23).map { hour ->
                HourlySummary(
                    hour = String.format("%02d:00", hour),
                    tremorLevel = summary.tremorByHour?.get(hour) ?: 0f,
                    heartRate = summary.hrByHour?.get(hour) ?: 0,
                    medicationTaken = summary.medsByHour?.contains(hour) == true
                )
            }

            DailySummary(
                avgTremor = summary.avgTremor,
                avgHeartRate = summary.avgHeartRate,
                sleepEfficiency = summary.sleepEfficiency,
                lastMedicationTime = summary.lastMedicationTime ?: "--:--",
                hourlySummary = hourlyData,
                alerts = emptyList()
            )
        }
    }
}
