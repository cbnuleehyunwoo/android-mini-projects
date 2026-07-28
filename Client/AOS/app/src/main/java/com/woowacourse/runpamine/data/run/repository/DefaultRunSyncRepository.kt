package com.woowacourse.runpamine.data.run.repository

import com.woowacourse.runpamine.data.run.local.RunLocalDataSource
import com.woowacourse.runpamine.data.run.remote.RunRemoteDataSource
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.run.RunResult
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunSyncRepository
import com.woowacourse.runpamine.domain.run.RunSyncStatus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultRunSyncRepository(
    private val authRepository: AuthRepository,
    private val localDataSource: RunLocalDataSource,
    private val remoteDataSource: RunRemoteDataSource,
) : RunSyncRepository {
    private val syncMutex = Mutex()

    override suspend fun syncRun(runSessionId: String): Result<RunResult> =
        syncMutex.withLock {
            syncRunLocked(runSessionId)
        }

    private suspend fun syncRunLocked(runSessionId: String): Result<RunResult> {
        val session =
            localDataSource.findSession(runSessionId)
                ?: return Result.failure(IllegalArgumentException("Run session not found: $runSessionId"))

        val currentSession =
            authRepository.getCurrentSession()
                ?: return Result.failure(IllegalStateException("로그인이 필요해요."))

        if (session.accountUserId == null) {
            return Result.failure(IllegalStateException("소유자를 확인할 수 없는 러닝 기록은 업로드하지 않아요."))
        }

        if (session.accountUserId != currentSession.user.id) {
            return Result.failure(IllegalStateException("다른 계정의 러닝 기록은 업로드하지 않아요."))
        }

        if (session.syncStatus == RunSyncStatus.SYNCED) {
            return Result.success(session.toRunResult())
        }

        if (session.distanceMeters == 0) {
            localDataSource.updateSyncStatus(runSessionId, RunSyncStatus.SYNCED)
            return Result.success(session.toRunResult())
        }

        val points = RunPointSimplifier.simplify(localDataSource.findPoints(runSessionId))

        return runCatching {
            localDataSource.updateSyncStatus(runSessionId, RunSyncStatus.SYNCING)
            remoteDataSource.createRun(
                accessToken = currentSession.accessToken,
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
}

private fun RunSession.toRunResult(): RunResult =
    RunResult(
        id = id,
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        averagePaceSecondsPerKm = averagePaceSecondsPerKm,
        calories = calories,
    )
