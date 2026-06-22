package com.woowacourse.runpamine.data.run.local

import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunSyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRunLocalDataSource(
    private val runDao: RunDao,
) : RunLocalDataSource {
    override suspend fun saveSession(session: RunSession) {
        runDao.insertSession(session.toEntity())
    }

    override suspend fun savePointAndMetrics(
        point: RunPoint,
        distanceMeters: Int,
        durationSeconds: Long,
        averagePaceSecondsPerKm: Int,
        calories: Int,
    ) {
        runDao.insertPointAndMetrics(
            point = point.toEntity(),
            distanceMeters = distanceMeters,
            durationSeconds = durationSeconds,
            averagePaceSecondsPerKm = averagePaceSecondsPerKm,
            calories = calories,
        )
    }

    override suspend fun updateRunningMetrics(
        sessionId: String,
        distanceMeters: Int,
        durationSeconds: Long,
        averagePaceSecondsPerKm: Int,
        calories: Int,
    ) {
        runDao.updateRunningMetrics(
            sessionId = sessionId,
            distanceMeters = distanceMeters,
            durationSeconds = durationSeconds,
            averagePaceSecondsPerKm = averagePaceSecondsPerKm,
            calories = calories,
        )
    }

    override suspend fun findSession(sessionId: String): RunSession? = runDao.getSession(sessionId)?.toDomain()

    override suspend fun findActiveSession(): RunSession? = runDao.getActiveSession()?.toDomain()

    override fun observeActiveSession(): Flow<RunSession?> = runDao.observeActiveSession().map { it?.toDomain() }

    override fun observePoints(sessionId: String): Flow<List<RunPoint>> =
        runDao.observePoints(sessionId).map { points -> points.map { it.toDomain() } }

    override suspend fun findPoints(sessionId: String): List<RunPoint> = runDao.getPoints(sessionId).map { it.toDomain() }

    override suspend fun findLastPoint(sessionId: String): RunPoint? = runDao.getLastPoint(sessionId)?.toDomain()

    override suspend fun countPoints(sessionId: String): Int = runDao.getPointCount(sessionId)

    override suspend fun finishSession(
        session: RunSession,
        status: RunSyncStatus,
    ) {
        runDao.finishSession(
            sessionId = session.id,
            endedAtEpochMillis = requireNotNull(session.endedAt).toEpochMilli(),
            distanceMeters = session.distanceMeters,
            durationSeconds = session.durationSeconds,
            averagePaceSecondsPerKm = session.averagePaceSecondsPerKm,
            calories = session.calories,
            syncStatus = status,
        )
    }

    override suspend fun deleteActiveSession() {
        runDao.deleteActiveSessions()
    }

    override suspend fun updateSyncStatus(
        sessionId: String,
        status: RunSyncStatus,
    ) {
        runDao.updateSyncStatus(sessionId, status)
    }

    override suspend fun findPendingSessions(): List<RunSession> =
        runDao
            .getSessionsByStatuses(listOf(RunSyncStatus.LOCAL_ONLY, RunSyncStatus.FAILED))
            .map { it.toDomain() }
}
