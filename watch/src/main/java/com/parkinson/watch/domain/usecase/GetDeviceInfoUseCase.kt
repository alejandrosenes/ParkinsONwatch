package com.parkinson.watch.domain.usecase

import com.parkinson.watch.data.repository.DeviceRepository
import com.parkinson.watch.domain.model.DeviceInfo
import javax.inject.Inject

class GetDeviceInfoUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(): DeviceInfo = deviceRepository.getDeviceInfo()
}
