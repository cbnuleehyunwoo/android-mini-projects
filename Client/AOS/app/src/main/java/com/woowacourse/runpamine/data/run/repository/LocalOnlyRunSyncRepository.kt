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
                averagePaceSecondsPerKm = session.averagePaceSecondsPerKm,
                calories = session.calories,
            ),
        )
    }

    override suspend fun syncPendingRuns(): List<Result<RunResult>> =
        localDataSource.findPendingSessions().map { session ->
            syncRun(session.id)
        }
}
