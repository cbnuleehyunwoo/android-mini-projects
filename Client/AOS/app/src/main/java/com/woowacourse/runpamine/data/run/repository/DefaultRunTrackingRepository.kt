package com.woowacourse.runpamine.data.run.repository

import com.woowacourse.runpamine.data.run.local.RunLocalDataSource
import com.woowacourse.runpamine.domain.run.LocationTracker
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunTrackingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID

class DefaultRunTrackingRepository(
    private val localDataSource: RunLocalDataSource,
    private val locationTracker: LocationTracker,
    private val metricCalculator: RunMetricCalculator = RunMetricCalculator(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val now: () -> Instant = Instant::now,
) : RunTrackingRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutex = Mutex()
    private var trackingJob: Job? = null

    override suspend fun startRun(): RunSession =
        mutex.withLock {
            val session =
                localDataSource.findActiveSession()
                    ?: RunSession(
                        id = UUID.randomUUID().toString(),
                        startedAt = now(),
                    ).also { localDataSource.saveSession(it) }

            startCollectingIfNeeded(session)
            session
        }

    override suspend fun stopRun(): RunSession? =
        mutex.withLock {
            val activeSession = localDataSource.findActiveSession() ?: return@withLock null
            trackingJob?.cancel()
            trackingJob = null

            val endedAt = now()
            val finishedSession =
                activeSession.copy(
                    endedAt = endedAt,
                    durationSeconds = metricCalculator.durationSeconds(activeSession.startedAt, endedAt),
                    calories = metricCalculator.calories(activeSession.distanceMeters),
                )
            localDataSource.finishSession(finishedSession)
            localDataSource.findSession(finishedSession.id)
        }

    override fun observeCurrentRun(): Flow<RunSession?> = localDataSource.observeActiveSession()

    private fun startCollectingIfNeeded(session: RunSession) {
        if (trackingJob?.isActive == true) return

        trackingJob =
            scope.launch {
                collectLocation(session)
            }
    }

    private suspend fun collectLocation(session: RunSession) {
        var currentSession = localDataSource.findSession(session.id) ?: session
        var lastPoint = localDataSource.findLastPoint(session.id)
        var nextSequence = localDataSource.countPoints(session.id) + 1
        var distanceMeters = currentSession.distanceMeters

        locationTracker
            .observeLocation()
            .catch {
                trackingJob = null
            }.collect { rawPoint ->
                val point =
                    rawPoint.copy(
                        sessionId = session.id,
                        sequence = nextSequence,
                    )
                distanceMeters += lastPoint?.let { metricCalculator.distanceBetweenMeters(it, point) } ?: 0

                val durationSeconds = metricCalculator.durationSeconds(currentSession.startedAt, point.recordedAt)
                val calories = metricCalculator.calories(distanceMeters)

                withContext(dispatcher) {
                    localDataSource.savePointAndMetrics(
                        point = point,
                        distanceMeters = distanceMeters,
                        durationSeconds = durationSeconds,
                        calories = calories,
                    )
                }

                currentSession =
                    currentSession.copy(
                        distanceMeters = distanceMeters,
                        durationSeconds = durationSeconds,
                        calories = calories,
                    )
                lastPoint = point
                nextSequence += 1
            }
    }
}
