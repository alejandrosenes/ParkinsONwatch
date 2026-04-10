package com.parkinson.watch.data.repository

import com.parkinson.watch.data.local.SleepDao
import com.parkinson.watch.data.local.entity.SleepSessionEntity
import com.parkinson.watch.domain.model.SleepPhase
import com.parkinson.watch.domain.model.SleepSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    private val sleepDao: SleepDao
) {
    fun getSleepForDate(date: LocalDate): Flow<SleepSession?> =
        sleepDao.getSleepForDate(date.toEpochDay()).map { entity ->
            entity?.let {
                SleepSession(
                    startTime = java.time.LocalDateTime.ofEpochSecond(
                        it.startTimestamp,
                        0,
                        java.time.ZoneOffset.UTC
                    ),
                    endTime = java.time.LocalDateTime.ofEpochSecond(
                        it.endTimestamp,
                        0,
                        java.time.ZoneOffset.UTC
                    ),
                    efficiencyPercent = it.efficiencyPercent,
                    rbdProxyScore = it.rbdProxyScore,
                    phases = mapOf(
                        SleepPhase.REM to it.remMinutes,
                        SleepPhase.N1 to it.n1Minutes,
                        SleepPhase.N2 to it.n2Minutes,
                        SleepPhase.N3 to it.n3Minutes,
                        SleepPhase.AWAKE to it.awakeMinutes
                    ),
                    awakenings = it.awakenings,
                    latencyMinutes = it.latencyMinutes
                )
            }
        }

    suspend fun saveSleepSession(session: SleepSession) {
        sleepDao.insertSleepSession(
            SleepSessionEntity(
                id = 0,
                date = session.startTime.toLocalDate().toEpochDay(),
                startTimestamp = session.startTime.toEpochSecond(java.time.ZoneOffset.UTC),
                endTimestamp = session.endTime.toEpochSecond(java.time.ZoneOffset.UTC),
                efficiencyPercent = session.efficiencyPercent,
                rbdProxyScore = session.rbdProxyScore,
                remMinutes = session.phases[SleepPhase.REM] ?: 0,
                n1Minutes = session.phases[SleepPhase.N1] ?: 0,
                n2Minutes = session.phases[SleepPhase.N2] ?: 0,
                n3Minutes = session.phases[SleepPhase.N3] ?: 0,
                awakeMinutes = session.phases[SleepPhase.AWAKE] ?: 0,
                awakenings = session.awakenings,
                latencyMinutes = session.latencyMinutes,
                synced = false
            )
        )
    }
}
