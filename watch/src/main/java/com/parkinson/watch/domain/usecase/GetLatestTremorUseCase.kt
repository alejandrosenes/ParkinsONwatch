package com.parkinson.watch.domain.usecase

import com.parkinson.shared.domain.model.TremorReading
import com.parkinson.watch.data.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestTremorUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(): Flow<TremorReading?> = sensorRepository.getLatestTremorReading()
}
