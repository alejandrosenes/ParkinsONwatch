package com.parkinson.watch.domain.usecase

import com.parkinson.watch.data.repository.HealthRepository
import javax.inject.Inject

class LogMedicationUseCase @Inject constructor(
    private val healthRepository: HealthRepository
) {
    suspend operator fun invoke() {
        healthRepository.logMedicationTaken()
    }
}
