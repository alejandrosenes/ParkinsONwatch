package com.parkinson.watch.domain.usecase

import com.parkinson.watch.data.repository.HealthRepository
import com.parkinson.watch.domain.model.MedicationEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTodayMedicationLogUseCase @Inject constructor(
    private val healthRepository: HealthRepository
) {
    operator fun invoke(): Flow<List<MedicationEntry>> = 
        healthRepository.getMedicationLogForDate(LocalDate.now())
}
