package com.woowacourse.runpamine.data.run.repository

import com.woowacourse.runpamine.data.run.local.RunLocalDataSource
import com.woowacourse.runpamine.data.run.remote.RunRemoteDataSource
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.auth.AuthSession
import com.woowacourse.runpamine.domain.auth.AuthUser
import com.woowacourse.runpamine.domain.run.RunDaySummary
import com.woowacourse.runpamine.domain.run.RunPeriodSummary
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunResult
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunSyncStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

class DefaultRunSyncRepositoryTest {
    @Test
    fun `0미터 기록은 서버로 전송하지 않는다`() =
        runBlocking {
            val localDataSource = FakeRunLocalDataSource(runSession(distanceMeters = 0))
            val remoteDataSource = FakeRunRemoteDataSource()
            val repository = repository(localDataSource, remoteDataSource)

            val result = repository.syncRun(RUN_ID)

            assertTrue(result.isSuccess)
            assertFalse(remoteDataSource.createRunCalled)
            assertEquals(RunSyncStatus.SYNCED, localDataSource.syncStatus)
        }

    @Test
    fun `1미터 기록은 서버로 전송한다`() =
        runBlocking {
            val localDataSource = FakeRunLocalDataSource(runSession(distanceMeters = 1))
            val remoteDataSource = FakeRunRemoteDataSource()
            val repository = repository(localDataSource, remoteDataSource)

            val result = repository.syncRun(RUN_ID)

            assertTrue(result.isSuccess)
            assertTrue(remoteDataSource.createRunCalled)
            assertEquals(RunSyncStatus.SYNCED, localDataSource.syncStatus)
        }

    @Test
    fun `업로드 실패 기록은 FAILED로 남고 pending 재시도 대상이 된다`() =
        runBlocking {
            val localDataSource = FakeRunLocalDataSource(runSession(distanceMeters = 1))
            val remoteDataSource =
                FakeRunRemoteDataSource().apply {
                    failure = IllegalStateException("network error")
                }
            val repository = repository(localDataSource, remoteDataSource)

            val failedResult = repository.syncRun(RUN_ID)

            assertTrue(failedResult.isFailure)
            assertEquals(RunSyncStatus.FAILED, localDataSource.syncStatus)

            remoteDataSource.failure = null
            val retryResults = repository.syncPendingRuns()

            assertEquals(1, retryResults.size)
            assertTrue(retryResults.single().isSuccess)
            assertEquals(RunSyncStatus.SYNCED, localDataSource.syncStatus)
            assertEquals(2, remoteDataSource.createRunCallCount)
        }

    @Test
    fun `동일 러닝 동시 업로드 요청은 한 번만 서버로 전송한다`() =
        runBlocking {
            val localDataSource = FakeRunLocalDataSource(runSession(distanceMeters = 1))
            val remoteDataSource =
                FakeRunRemoteDataSource().apply {
                    createRunDelayMillis = 50
                }
            val repository = repository(localDataSource, remoteDataSource)

            val first = async { repository.syncRun(RUN_ID) }
            val second = async { repository.syncRun(RUN_ID) }

            assertTrue(first.await().isSuccess)
            assertTrue(second.await().isSuccess)
            assertEquals(1, remoteDataSource.createRunCallCount)
            assertEquals(RunSyncStatus.SYNCED, localDataSource.syncStatus)
        }

    @Test
    fun `다른 계정의 로컬 러닝은 서버로 전송하지 않는다`() =
        runBlocking {
            val localDataSource =
                FakeRunLocalDataSource(
                    runSession(distanceMeters = 1).copy(accountUserId = "other-user-id"),
                )
            val remoteDataSource = FakeRunRemoteDataSource()
            val repository = repository(localDataSource, remoteDataSource)

            val result = repository.syncRun(RUN_ID)

            assertTrue(result.isFailure)
            assertFalse(remoteDataSource.createRunCalled)
            assertEquals(RunSyncStatus.LOCAL_ONLY, localDataSource.syncStatus)
        }

    @Test
    fun `소유자가 없는 로컬 러닝은 서버로 전송하지 않는다`() =
        runBlocking {
            val localDataSource =
                FakeRunLocalDataSource(
                    runSession(distanceMeters = 1).copy(accountUserId = null),
                )
            val remoteDataSource = FakeRunRemoteDataSource()
            val repository = repository(localDataSource, remoteDataSource)

            val result = repository.syncRun(RUN_ID)

            assertTrue(result.isFailure)
            assertFalse(remoteDataSource.createRunCalled)
            assertEquals(RunSyncStatus.LOCAL_ONLY, localDataSource.syncStatus)
        }

    private fun repository(
        localDataSource: RunLocalDataSource,
        remoteDataSource: RunRemoteDataSource,
    ) = DefaultRunSyncRepository(
        authRepository = FakeAuthRepository(),
        localDataSource = localDataSource,
        remoteDataSource = remoteDataSource,
    )

    private fun runSession(distanceMeters: Int) =
        RunSession(
            id = RUN_ID,
            startedAt = Instant.parse("2026-06-15T00:00:00Z"),
            endedAt = Instant.parse("2026-06-15T00:01:00Z"),
            distanceMeters = distanceMeters,
            durationSeconds = 60,
            accountUserId = "user-id",
        )

    private class FakeAuthRepository : AuthRepository {
        private val session = AuthSession("access-token", "refresh-token", AuthUser("user-id", null))

        override fun observeSession(): Flow<AuthSession?> = emptyFlow()

        override suspend fun loadSessionFromStorage(): AuthSession = session

        override suspend fun getCurrentSession(): AuthSession = session

        override suspend fun signInWithGoogleIdToken(
            idToken: String,
            nonce: String?,
        ): AuthSession = session

        override suspend fun signOut() = Unit

        override suspend fun deleteAccount() = Unit
    }

    private class FakeRunLocalDataSource(
        session: RunSession,
    ) : RunLocalDataSource {
        private var session: RunSession? = session
        var syncStatus: RunSyncStatus = session.syncStatus
            private set

        override suspend fun findSession(sessionId: String): RunSession? =
            session?.copy(syncStatus = syncStatus)?.takeIf { it.id == sessionId }

        override suspend fun findPoints(sessionId: String): List<RunPoint> = emptyList()

        override suspend fun updateSyncStatus(
            sessionId: String,
            status: RunSyncStatus,
        ) {
            syncStatus = status
            session = session?.copy(syncStatus = status)
        }

        override suspend fun saveSession(session: RunSession) = Unit

        override suspend fun savePointAndMetrics(
            point: RunPoint,
            distanceMeters: Int,
            durationSeconds: Long,
            averagePaceSecondsPerKm: Int,
            calories: Int,
        ) = Unit

        override suspend fun updateRunningMetrics(
            sessionId: String,
            distanceMeters: Int,
            durationSeconds: Long,
            averagePaceSecondsPerKm: Int,
            calories: Int,
        ) = Unit

        override suspend fun findActiveSession(): RunSession? = null

        override fun observeActiveSession(): Flow<RunSession?> = emptyFlow()

        override fun observePoints(sessionId: String): Flow<List<RunPoint>> = emptyFlow()

        override suspend fun findLastPoint(sessionId: String): RunPoint? = null

        override suspend fun countPoints(sessionId: String): Int = 0

        override suspend fun finishSession(
            session: RunSession,
            status: RunSyncStatus,
        ) = Unit

        override suspend fun deleteActiveSession() = Unit

        override suspend fun findPendingSessions(): List<RunSession> =
            listOfNotNull(
                session
                    ?.copy(syncStatus = syncStatus)
                    ?.takeIf {
                        it.endedAt != null && it.syncStatus.isPending()
                    },
            )
    }

    private class FakeRunRemoteDataSource : RunRemoteDataSource {
        var createRunCalled = false
            private set
        var createRunCallCount = 0
            private set
        var createRunDelayMillis = 0L
        var failure: Throwable? = null

        override suspend fun createRun(
            accessToken: String,
            session: RunSession,
            points: List<RunPoint>,
        ): RunResult {
            createRunCalled = true
            createRunCallCount += 1
            if (createRunDelayMillis > 0) delay(createRunDelayMillis)
            failure?.let { throw it }
            return RunResult(session.id, session.distanceMeters, session.durationSeconds, session.averagePaceSecondsPerKm, session.calories)
        }

        override suspend fun getWeeklyRuns(
            accessToken: String,
            anchorDate: LocalDate,
        ): RunPeriodSummary = RunPeriodSummary(0, emptyList<RunDaySummary>(), emptyList())

        override suspend fun getMonthlyRuns(
            accessToken: String,
            yearMonth: YearMonth,
        ): RunPeriodSummary = RunPeriodSummary(0, emptyList<RunDaySummary>(), emptyList())

        override suspend fun getRunDetail(
            accessToken: String,
            runId: String,
        ): RunSession = error("Not used")
    }

    private companion object {
        const val RUN_ID = "run-id"
    }
}

private fun RunSyncStatus.isPending(): Boolean =
    when (this) {
        RunSyncStatus.LOCAL_ONLY,
        RunSyncStatus.SYNCING,
        RunSyncStatus.FAILED,
        -> true
        RunSyncStatus.SYNCED -> false
    }
