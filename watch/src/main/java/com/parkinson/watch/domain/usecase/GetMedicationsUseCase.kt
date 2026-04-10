package com.parkinson.watch.domain.usecase

import com.parkinson.watch.data.repository.HealthRepository
import com.parkinson.watch.domain.model.MedicationSchedule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMedicationsUseCase @Inject constructor(
    private val healthRepository: HealthRepository
) {
    operator fun invoke(): Flow<List<MedicationSchedule>> = healthRepository.getMedicationSchedules()
}
