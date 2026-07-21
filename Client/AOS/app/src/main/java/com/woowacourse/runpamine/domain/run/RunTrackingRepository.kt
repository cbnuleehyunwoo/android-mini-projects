package com.woowacourse.runpamine.domain.run

import kotlinx.coroutines.flow.Flow

interface RunTrackingRepository {
    suspend fun startRun(): RunSession

    suspend fun pauseRun()

    suspend fun resumeRun()

    suspend fun stopRun(): RunSession?

    suspend fun discardActiveRun()

    fun observeCurrentRun(): Flow<RunSession?>

    fun observeCurrentRoutePoints(): Flow<List<RunPoint>>

    fun observePaused(): Flow<Boolean>

    fun currentElapsedSeconds(): Long
}
