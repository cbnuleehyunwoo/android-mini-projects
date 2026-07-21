package com.woowacourse.runpamine.domain.run

interface RunSyncRepository {
    suspend fun syncRun(runSessionId: String): Result<RunResult>

    suspend fun syncPendingRuns(): List<Result<RunResult>>
}
