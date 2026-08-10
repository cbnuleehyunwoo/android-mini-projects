package com.woowacourse.runpamine.data.run.repository

import com.woowacourse.runpamine.data.run.local.RunLocalDataSource
import com.woowacourse.runpamine.domain.run.LocationTracker
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunSplit
import com.woowacourse.runpamine.domain.run.RunSyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class DefaultRunTrackingRepositoryTest {
    @Test
    fun `10초 동안 움직이지 않아도 사용자가 일시정지하지 않으면 러닝을 계속한다`() =
        runBlocking {
            val clock = AtomicReference(STARTED_AT)
            val localDataSource = FakeRunLocalDataSource()
            val locationTracker = FakeLocationTracker()
            val repository =
                repository(
                    localDataSource = localDataSource,
                    locationTracker = locationTracker,
                    clock = clock,
                )

            repository.startRun()
            awaitCondition { locationTracker.requestCount == 1 }
            locationTracker.emit(point(longitude = 127.0, secondsAfterStart = 0))
            awaitCondition { localDataSource.points.size == 1 }

            clock.set(STARTED_AT.plusSeconds(10))
            delay(10)
            assertFalse(repository.observePaused().latestValue())
            assertEquals(1, locationTracker.requestCount)
            assertEquals(1, locationTracker.activeSubscriptions)
            assertEquals(10L, repository.currentElapsedSeconds())

            repository.discardActiveRun()
        }

    @Test
    fun `일시정지와 재개는 사용자가 명시적으로 요청할 때만 상태를 전환한다`() =
        runBlocking {
            val clock = AtomicReference(STARTED_AT)
            val localDataSource = FakeRunLocalDataSource()
            val locationTracker = FakeLocationTracker()
            val repository =
                repository(
                    localDataSource = localDataSource,
                    locationTracker = locationTracker,
                    clock = clock,
                )

            repository.startRun()
            awaitCondition { locationTracker.activeSubscriptions == 1 }

            repository.pauseRun()
            awaitCondition { locationTracker.activeSubscriptions == 0 }
            assertTrue(repository.observePaused().latestValue())
            assertEquals(1, locationTracker.requestCount)

            clock.set(STARTED_AT.plusSeconds(30))
            delay(10)
            assertTrue(repository.observePaused().latestValue())
            assertEquals(1, locationTracker.requestCount)

            repository.resumeRun()
            awaitCondition { locationTracker.activeSubscriptions == 1 }
            assertFalse(repository.observePaused().latestValue())
            assertEquals(2, locationTracker.requestCount)

            repository.discardActiveRun()
        }

    @Test
    fun `러닝 시작 시 현재 사용자 식별자를 로컬 세션에 저장한다`() =
        runBlocking {
            val localDataSource = FakeRunLocalDataSource()
            val locationTracker = FakeLocationTracker()
            val repository =
                DefaultRunTrackingRepository(
                    localDataSource = localDataSource,
                    locationTracker = locationTracker,
                    dispatcher = Dispatchers.Default,
                    now = { STARTED_AT },
                    currentUserId = { "user-id" },
                )

            val session = repository.startRun()

            assertEquals("user-id", session.accountUserId)
            assertEquals("user-id", localDataSource.session.value?.accountUserId)

            repository.discardActiveRun()
        }

    private fun repository(
        localDataSource: RunLocalDataSource,
        locationTracker: LocationTracker,
        clock: AtomicReference<Instant>,
    ) = DefaultRunTrackingRepository(
        localDataSource = localDataSource,
        locationTracker = locationTracker,
        dispatcher = Dispatchers.Default,
        now = clock::get,
    )

    private suspend fun awaitCondition(condition: suspend () -> Boolean) {
        withTimeout(1_000) {
            while (!condition()) {
                delay(1)
            }
        }
    }

    private suspend fun Flow<Boolean>.latestValue(): Boolean = first()

    private fun point(
        longitude: Double,
        secondsAfterStart: Long,
    ) = RunPoint(
        latitude = 37.0,
        longitude = longitude,
        recordedAt = STARTED_AT.plusSeconds(secondsAfterStart),
        horizontalAccuracyMeters = 5f,
    )

    private class FakeLocationTracker : LocationTracker {
        private val stream = MutableSharedFlow<RunPoint>(extraBufferCapacity = 8)
        private val requests = AtomicInteger(0)
        val requestCount: Int
            get() = requests.get()
        val activeSubscriptions: Int
            get() = stream.subscriptionCount.value

        override fun observeLocation(): Flow<RunPoint> =
            flow {
                requests.incrementAndGet()
                stream.collect { emit(it) }
            }

        suspend fun emit(point: RunPoint) {
            withTimeout(1_000) {
                while (stream.subscriptionCount.value == 0) {
                    delay(1)
                }
            }
            stream.emit(point)
        }
    }

    private class FakeRunLocalDataSource : RunLocalDataSource {
        val session = MutableStateFlow<RunSession?>(null)
        val points = CopyOnWriteArrayList<RunPoint>()

        override suspend fun saveSession(session: RunSession) {
            this.session.value = session
        }

        override suspend fun savePointAndMetrics(
            point: RunPoint,
            distanceMeters: Int,
            durationSeconds: Long,
            averagePaceSecondsPerKm: Int,
            calories: Int,
            splits: List<RunSplit>,
        ) {
            points += point
            session.value =
                session.value?.copy(
                    distanceMeters = distanceMeters,
                    durationSeconds = durationSeconds,
                    averagePaceSecondsPerKm = averagePaceSecondsPerKm,
                    calories = calories,
                    splits = splits,
                )
        }

        override suspend fun updateRunningMetrics(
            sessionId: String,
            distanceMeters: Int,
            durationSeconds: Long,
            averagePaceSecondsPerKm: Int,
            calories: Int,
            splits: List<RunSplit>,
        ) {
            session.value =
                session.value?.copy(
                    distanceMeters = distanceMeters,
                    durationSeconds = durationSeconds,
                    averagePaceSecondsPerKm = averagePaceSecondsPerKm,
                    calories = calories,
                    splits = splits,
                )
        }

        override suspend fun findSession(sessionId: String): RunSession? = session.value?.takeIf { it.id == sessionId }

        override suspend fun findActiveSession(): RunSession? = session.value?.takeIf { it.endedAt == null }

        override fun observeActiveSession(): Flow<RunSession?> = session

        override fun observePoints(sessionId: String): Flow<List<RunPoint>> =
            session.map { points.filter { point -> point.sessionId == sessionId } }

        override suspend fun findPoints(sessionId: String): List<RunPoint> = points.filter { it.sessionId == sessionId }

        override suspend fun findLastPoint(sessionId: String): RunPoint? = points.lastOrNull { it.sessionId == sessionId }

        override suspend fun countPoints(sessionId: String): Int = points.count { it.sessionId == sessionId }

        override suspend fun finishSession(
            session: RunSession,
            status: RunSyncStatus,
        ) {
            this.session.value = session.copy(syncStatus = status)
        }

        override suspend fun deleteActiveSession() {
            session.value = null
            points.clear()
        }

        override suspend fun updateSyncStatus(
            sessionId: String,
            status: RunSyncStatus,
        ) {
            session.value = session.value?.copy(syncStatus = status)
        }

        override suspend fun findPendingSessions(): List<RunSession> = emptyList()
    }

    private companion object {
        val STARTED_AT: Instant = Instant.parse("2026-07-26T00:00:00Z")
    }
}
