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
        private val session: RunSession,
    ) : RunLocalDataSource {
        var syncStatus: RunSyncStatus = session.syncStatus
            private set

        override suspend fun findSession(sessionId: String): RunSession? = session.takeIf { it.id == sessionId }

        override suspend fun findPoints(sessionId: String): List<RunPoint> = emptyList()

        override suspend fun updateSyncStatus(
            sessionId: String,
            status: RunSyncStatus,
        ) {
            syncStatus = status
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

        override suspend fun findPendingSessions(): List<RunSession> = emptyList()
    }

    private class FakeRunRemoteDataSource : RunRemoteDataSource {
        var createRunCalled = false
            private set

        override suspend fun createRun(
            accessToken: String,
            session: RunSession,
            points: List<RunPoint>,
        ): RunResult {
            createRunCalled = true
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
