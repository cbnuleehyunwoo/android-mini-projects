package com.woowacourse.runpamine.data.run.repository

import com.woowacourse.runpamine.data.run.local.RunLocalDataSource
import com.woowacourse.runpamine.domain.run.LocationTracker
import com.woowacourse.runpamine.domain.run.LocationTrackingMode
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunSession
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
import java.util.concurrent.atomic.AtomicReference

class DefaultRunTrackingRepositoryTest {
    @Test
    fun `자동 정지 후 3초 위치 감시가 이동을 감지하면 새 세그먼트로 재개한다`() =
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
            awaitCondition { locationTracker.requestedModes == listOf(LocationTrackingMode.RUNNING) }
            locationTracker.emit(LocationTrackingMode.RUNNING, point(longitude = 127.0, secondsAfterStart = 0))
            awaitCondition { localDataSource.points.size == 1 }

            clock.set(STARTED_AT.plusSeconds(10))
            awaitCondition { repository.observePaused().latestValue() }
            awaitCondition {
                locationTracker.requestedModes ==
                    listOf(LocationTrackingMode.RUNNING, LocationTrackingMode.AUTO_RESUME)
            }
            assertEquals(3_000L, LocationTrackingMode.AUTO_RESUME.updateIntervalMillis)
            assertEquals(0f, LocationTrackingMode.AUTO_RESUME.minimumUpdateDistanceMeters)

            locationTracker.emit(LocationTrackingMode.AUTO_RESUME, point(longitude = 127.0, secondsAfterStart = 13))
            delay(10)
            assertTrue(repository.observePaused().latestValue())
            assertEquals(1, localDataSource.points.size)

            locationTracker.emit(LocationTrackingMode.AUTO_RESUME, point(longitude = 127.0001, secondsAfterStart = 16))
            delay(10)
            assertTrue(repository.observePaused().latestValue())
            assertEquals(1, localDataSource.points.size)

            locationTracker.emit(LocationTrackingMode.AUTO_RESUME, point(longitude = 127.0002, secondsAfterStart = 19))
            awaitCondition { !repository.observePaused().latestValue() }
            awaitCondition {
                locationTracker.requestedModes ==
                    listOf(
                        LocationTrackingMode.RUNNING,
                        LocationTrackingMode.AUTO_RESUME,
                        LocationTrackingMode.RUNNING,
                    )
            }
            assertEquals(1, localDataSource.points.size)

            locationTracker.emit(LocationTrackingMode.RUNNING, point(longitude = 127.0002, secondsAfterStart = 20))
            awaitCondition { localDataSource.points.size == 2 }
            assertEquals(2, localDataSource.points.last().sequence)
            assertEquals(0, localDataSource.session.value?.distanceMeters)

            repository.discardActiveRun()
        }

    @Test
    fun `사용자 수동 일시정지는 자동 재개 위치 감시를 시작하지 않는다`() =
        runBlocking {
            val localDataSource = FakeRunLocalDataSource()
            val locationTracker = FakeLocationTracker()
            val repository =
                repository(
                    localDataSource = localDataSource,
                    locationTracker = locationTracker,
                    clock = AtomicReference(STARTED_AT),
                )

            repository.startRun()
            awaitCondition { locationTracker.requestedModes == listOf(LocationTrackingMode.RUNNING) }

            repository.pauseRun()
            delay(10)

            assertTrue(repository.observePaused().latestValue())
            assertFalse(locationTracker.requestedModes.contains(LocationTrackingMode.AUTO_RESUME))

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
        autoPauseInactivitySeconds = 10,
        inactivityCheckIntervalMillis = 1,
        movementDistanceMeters = { from, to ->
            if (from.latitude == to.latitude && from.longitude == to.longitude) 0 else 5
        },
        isAutoResumePointPlausible = { _, _ -> true },
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
        private val streams =
            LocationTrackingMode.entries.associateWith {
                MutableSharedFlow<RunPoint>(extraBufferCapacity = 8)
            }
        val requestedModes = CopyOnWriteArrayList<LocationTrackingMode>()

        override fun observeLocation(mode: LocationTrackingMode): Flow<RunPoint> =
            flow {
                requestedModes += mode
                streams.getValue(mode).collect { emit(it) }
            }

        suspend fun emit(
            mode: LocationTrackingMode,
            point: RunPoint,
        ) {
            val stream = streams.getValue(mode)
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
        ) {
            points += point
            session.value =
                session.value?.copy(
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
            session.value =
                session.value?.copy(
                    distanceMeters = distanceMeters,
                    durationSeconds = durationSeconds,
                    averagePaceSecondsPerKm = averagePaceSecondsPerKm,
                    calories = calories,
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
