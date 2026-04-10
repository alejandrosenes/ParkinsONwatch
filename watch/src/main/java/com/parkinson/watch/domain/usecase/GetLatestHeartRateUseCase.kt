package com.parkinson.watch.domain.usecase

import com.parkinson.shared.domain.model.HeartRateReading
import com.parkinson.watch.data.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestHeartRateUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(): Flow<HeartRateReading?> = sensorRepository.getLatestHeartRateReading()
}
