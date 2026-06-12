package com.woowacourse.runpamine.data.run.repository

import com.woowacourse.runpamine.data.run.local.RunLocalDataSource
import com.woowacourse.runpamine.data.run.remote.RunRemoteDataSource
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.run.RunResult
import com.woowacourse.runpamine.domain.run.RunSyncRepository
import com.woowacourse.runpamine.domain.run.RunSyncStatus

class DefaultRunSyncRepository(
    private val authRepository: AuthRepository,
    private val localDataSource: RunLocalDataSource,
    private val remoteDataSource: RunRemoteDataSource,
) : RunSyncRepository {
    override suspend fun syncRun(runSessionId: String): Result<RunResult> {
        val session =
            localDataSource.findSession(runSessionId)
                ?: return Result.failure(IllegalArgumentException("Run session not found: $runSessionId"))
        val points = RunPointSimplifier.simplify(localDataSource.findPoints(runSessionId))

        return runCatching {
            localDataSource.updateSyncStatus(runSessionId, RunSyncStatus.SYNCING)
            remoteDataSource.createRun(
                accessToken = requireAccessToken(),
                session = session,
                points = points,
            )
        }.onSuccess {
            localDataSource.updateSyncStatus(runSessionId, RunSyncStatus.SYNCED)
        }.onFailure {
            localDataSource.updateSyncStatus(runSessionId, RunSyncStatus.FAILED)
        }
    }

    override suspend fun syncPendingRuns(): List<Result<RunResult>> =
        localDataSource.findPendingSessions().map { session ->
            syncRun(session.id)
        }

    private suspend fun requireAccessToken(): String =
        requireNotNull(authRepository.getCurrentSession()?.accessToken) {
            "로그인이 필요해요."
        }
}
