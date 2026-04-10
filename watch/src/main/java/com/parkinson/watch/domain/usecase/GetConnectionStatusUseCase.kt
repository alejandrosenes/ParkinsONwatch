package com.parkinson.watch.domain.usecase

import com.parkinson.watch.data.repository.DeviceRepository
import com.parkinson.watch.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConnectionStatusUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Flow<ConnectionStatus> = deviceRepository.getConnectionStatus()
}
