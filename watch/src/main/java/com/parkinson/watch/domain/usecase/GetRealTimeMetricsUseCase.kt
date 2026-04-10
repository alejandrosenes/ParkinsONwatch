package com.parkinson.watch.domain.usecase

import com.parkinson.watch.data.repository.SensorRepository
import com.parkinson.watch.domain.model.RealTimeMetrics
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRealTimeMetricsUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(): Flow<RealTimeMetrics> = sensorRepository.getRealTimeMetrics()
}
