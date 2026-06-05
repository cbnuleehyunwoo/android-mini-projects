package com.woowacourse.runpamine.domain.run

import kotlinx.coroutines.flow.Flow

interface RunTrackingRepository {
    suspend fun startRun(): RunSession

    suspend fun stopRun(): RunSession?

    fun observeCurrentRun(): Flow<RunSession?>
}
