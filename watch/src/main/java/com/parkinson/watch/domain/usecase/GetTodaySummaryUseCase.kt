package com.parkinson.watch.domain.usecase

import com.parkinson.watch.data.repository.HealthRepository
import com.parkinson.watch.domain.model.Alert
import com.parkinson.watch.domain.model.DailySummary
import com.parkinson.watch.domain.model.HourlySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetTodaySummaryUseCase @Inject constructor(
    private val healthRepository: HealthRepository
) {
    operator fun invoke(): Flow<DailySummary> = healthRepository.getDailySummary(LocalDate.now())
}
