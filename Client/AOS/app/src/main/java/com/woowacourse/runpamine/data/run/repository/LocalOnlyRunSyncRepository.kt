package com.woowacourse.runpamine.data.run.repository

import com.woowacourse.runpamine.data.run.local.RunLocalDataSource
import com.woowacourse.runpamine.domain.run.RunResult
import com.woowacourse.runpamine.domain.run.RunSyncRepository

class LocalOnlyRunSyncRepository(
    private val localDataSource: RunLocalDataSource,
) : RunSyncRepository {
    override suspend fun syncRun(runSessionId: String): Result<RunResult> {
        val session =
            localDataSource.findSession(runSessionId)
                ?: return Result.failure(IllegalArgumentException("Run session not found: $runSessionId"))

        return Result.success(
            RunResult(
                id = session.id,
                distanceMeters = session.distanceMeters,
                durationSeconds = session.durationSeconds,
                averagePaceSecondsPerKm =
                    averagePaceSecondsPerKm(
                        distanceMeters = session.distanceMeters,
                        durationSeconds = session.durationSeconds,
                    ),
                calories = session.calories,
            ),
        )
    }

    override suspend fun syncPendingRuns(): List<Result<RunResult>> =
        localDataSource.findPendingSessions().map { session ->
            syncRun(session.id)
        }

    private fun averagePaceSecondsPerKm(
        distanceMeters: Int,
        durationSeconds: Long,
    ): Int {
        if (distanceMeters <= 0) return 0
        return (durationSeconds / (distanceMeters / METERS_PER_KILOMETER)).toInt()
    }

    private companion object {
        const val METERS_PER_KILOMETER = 1_000.0
    }
}
