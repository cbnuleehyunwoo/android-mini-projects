package com.woowacourse.runpamine.data.run.local

import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunSplit
import com.woowacourse.runpamine.domain.run.RunSyncStatus
import kotlinx.coroutines.flow.Flow

interface RunLocalDataSource {
    suspend fun saveSession(session: RunSession)

    suspend fun savePointAndMetrics(
        point: RunPoint,
        distanceMeters: Int,
        durationSeconds: Long,
        averagePaceSecondsPerKm: Int,
        calories: Int,
        splits: List<RunSplit>,
    )

    suspend fun updateRunningMetrics(
        sessionId: String,
        distanceMeters: Int,
        durationSeconds: Long,
        averagePaceSecondsPerKm: Int,
        calories: Int,
        splits: List<RunSplit>,
    )

    suspend fun findSession(sessionId: String): RunSession?

    suspend fun findActiveSession(): RunSession?

    fun observeActiveSession(): Flow<RunSession?>

    fun observePoints(sessionId: String): Flow<List<RunPoint>>

    suspend fun findPoints(sessionId: String): List<RunPoint>

    suspend fun findLastPoint(sessionId: String): RunPoint?

    suspend fun countPoints(sessionId: String): Int

    suspend fun finishSession(
        session: RunSession,
        status: RunSyncStatus = RunSyncStatus.LOCAL_ONLY,
    )

    suspend fun deleteActiveSession()

    suspend fun updateSyncStatus(
        sessionId: String,
        status: RunSyncStatus,
    )

    suspend fun findPendingSessions(): List<RunSession>
}
