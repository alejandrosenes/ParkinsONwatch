package com.parkinson.hub.domain.usecase

import com.parkinson.hub.data.repository.HealthRepository
import com.parkinson.hub.domain.model.AlertData
import com.parkinson.hub.domain.model.DailyData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class GetDailyDataUseCase @Inject constructor(
    private val healthRepository: HealthRepository
) {
    operator fun invoke(): Flow<DailyData> = healthRepository.getTodayData()
}
