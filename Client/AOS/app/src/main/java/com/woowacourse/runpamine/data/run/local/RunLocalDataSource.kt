package com.woowacourse.runpamine.data.run.local

import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunSession
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
    )

    suspend fun findSession(sessionId: String): RunSession?

    suspend fun findActiveSession(): RunSession?

    fun observeActiveSession(): Flow<RunSession?>

    suspend fun findPoints(sessionId: String): List<RunPoint>

    suspend fun findLastPoint(sessionId: String): RunPoint?

    suspend fun countPoints(sessionId: String): Int

    suspend fun finishSession(
        session: RunSession,
        status: RunSyncStatus = RunSyncStatus.LOCAL_ONLY,
    )

    suspend fun updateSyncStatus(
        sessionId: String,
        status: RunSyncStatus,
    )

    suspend fun findPendingSessions(): List<RunSession>
}
