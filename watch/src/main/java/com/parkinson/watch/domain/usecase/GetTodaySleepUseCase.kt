package com.parkinson.watch.domain.usecase

import com.parkinson.watch.data.repository.SleepRepository
import com.parkinson.watch.domain.model.SleepSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTodaySleepUseCase @Inject constructor(
    private val sleepRepository: SleepRepository
) {
    operator fun invoke(): Flow<SleepSession?> = sleepRepository.getSleepForDate(LocalDate.now())
}
