package com.parkinson.watch.domain.usecase

import com.parkinson.watch.data.repository.SensorRepository
import javax.inject.Inject

class StartMonitoringUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    suspend operator fun invoke() {
        sensorRepository.startHighFrequencyMonitoring()
    }
}
