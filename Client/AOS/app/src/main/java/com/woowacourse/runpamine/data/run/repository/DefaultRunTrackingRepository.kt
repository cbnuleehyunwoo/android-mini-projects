package com.woowacourse.runpamine.data.run.repository

import com.woowacourse.runpamine.data.run.local.RunLocalDataSource
import com.woowacourse.runpamine.domain.run.LocationTracker
import com.woowacourse.runpamine.domain.run.LocationTrackingMode
import com.woowacourse.runpamine.domain.run.NoOpRunVoicePlayer
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunTrackingRepository
import com.woowacourse.runpamine.domain.run.RunVoiceCue
import com.woowacourse.runpamine.domain.run.RunVoicePlayer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultRunTrackingRepository(
    private val localDataSource: RunLocalDataSource,
    private val locationTracker: LocationTracker,
    private val metricCalculator: RunMetricCalculator = RunMetricCalculator(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val now: () -> Instant = Instant::now,
    private val currentUserId: suspend () -> String? = { null },
    private val runVoicePlayer: RunVoicePlayer = NoOpRunVoicePlayer,
    private val autoPauseInactivitySeconds: Long = AUTO_PAUSE_INACTIVITY_SECONDS,
    private val inactivityCheckIntervalMillis: Long = INACTIVITY_CHECK_INTERVAL_MILLIS,
    private val movementDistanceMeters: (RunPoint, RunPoint) -> Int = metricCalculator::distanceBetweenMeters,
    private val isAutoResumePointPlausible: (RunPoint, RunPoint) -> Boolean = { reference, candidate ->
        candidate.isPlausibleAfter(reference)
    },
) : RunTrackingRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutex = Mutex()
    private var trackingJob: Job? = null
    private var inactivityJob: Job? = null
    private var autoResumeJob: Job? = null
    private val isPaused = MutableStateFlow(false)

    @Volatile
    private var isAutoPaused = false

    @Volatile
    private var accumulatedDurationSeconds: Long = 0

    @Volatile
    private var currentSegmentStartedAt: Instant? = null

    private var activeSessionId: String? = null

    @Volatile
    private var lastMovementAt: Instant? = null

    @Volatile
    private var latestAcceptedPoint: RunPoint? = null

    override suspend fun startRun(): RunSession =
        mutex.withLock {
            val activeSession = localDataSource.findActiveSession()
            val isNewSession = activeSession == null
            val session =
                activeSession
                    ?.withCurrentUserId()
                    ?: RunSession(
                        id = UUID.randomUUID().toString(),
                        startedAt = now(),
                        accountUserId = currentUserId(),
                    ).also { localDataSource.saveSession(it) }

            if (activeSessionId != session.id) {
                activeSessionId = session.id
                accumulatedDurationSeconds = session.durationSeconds
                currentSegmentStartedAt = now()
                lastMovementAt = now()
                latestAcceptedPoint = null
                isPaused.value = false
                isAutoPaused = false
            }
            startCollectingIfNeeded(session, resetLastPoint = false)
            if (isNewSession) {
                runVoicePlayer.play(RunVoiceCue.START)
            }
            session
        }

    override suspend fun pauseRun() {
        mutex.withLock {
            val activeSession = localDataSource.findActiveSession() ?: return@withLock
            if (isPaused.value) return@withLock

            accumulatedDurationSeconds = currentElapsedSeconds()
            currentSegmentStartedAt = null
            isPaused.value = true
            isAutoPaused = false
            cancelTrackingJobs()
            saveCurrentMetrics(activeSession, accumulatedDurationSeconds)
            runVoicePlayer.play(RunVoiceCue.PAUSE)
        }
    }

    override suspend fun resumeRun() {
        mutex.withLock {
            val activeSession = localDataSource.findActiveSession() ?: return@withLock
            if (!isPaused.value) return@withLock

            currentSegmentStartedAt = now()
            lastMovementAt = now()
            isPaused.value = false
            isAutoPaused = false
            autoResumeJob?.cancel()
            autoResumeJob = null
            startCollectingIfNeeded(activeSession, resetLastPoint = true)
            runVoicePlayer.play(RunVoiceCue.RESUME)
        }
    }

    override suspend fun stopRun(): RunSession? =
        mutex.withLock {
            val activeSession = localDataSource.findActiveSession() ?: return@withLock null
            cancelTrackingJobs()

            val endedAt = now()
            val durationSeconds = currentElapsedSeconds()
            val finishedSession =
                activeSession.copy(
                    endedAt = endedAt,
                    durationSeconds = durationSeconds,
                    averagePaceSecondsPerKm =
                        metricCalculator.averagePaceSecondsPerKm(
                            distanceMeters = activeSession.distanceMeters,
                            durationSeconds = durationSeconds,
                        ),
                    calories = metricCalculator.calories(activeSession.distanceMeters),
                )
            localDataSource.finishSession(finishedSession)
            activeSessionId = null
            accumulatedDurationSeconds = 0
            currentSegmentStartedAt = null
            lastMovementAt = null
            latestAcceptedPoint = null
            isPaused.value = false
            isAutoPaused = false
            runVoicePlayer.play(RunVoiceCue.STOP)
            localDataSource.findSession(finishedSession.id)
        }

    override suspend fun discardActiveRun() {
        mutex.withLock {
            cancelTrackingJobs()
            localDataSource.deleteActiveSession()
            activeSessionId = null
            accumulatedDurationSeconds = 0
            currentSegmentStartedAt = null
            lastMovementAt = null
            latestAcceptedPoint = null
            isPaused.value = false
            isAutoPaused = false
        }
    }

    override fun observeCurrentRun(): Flow<RunSession?> = localDataSource.observeActiveSession()

    override fun observeCurrentRoutePoints(): Flow<List<RunPoint>> =
        localDataSource
            .observeActiveSession()
            .flatMapLatest { session ->
                if (session == null) {
                    flowOf(emptyList())
                } else {
                    localDataSource.observePoints(session.id)
                }
            }

    override fun observePaused(): Flow<Boolean> = isPaused

    override fun currentElapsedSeconds(): Long {
        val segmentStartedAt = currentSegmentStartedAt ?: return accumulatedDurationSeconds
        val segmentDuration = Duration.between(segmentStartedAt, now()).seconds.coerceAtLeast(0)
        return accumulatedDurationSeconds + segmentDuration
    }

    private fun startCollectingIfNeeded(
        session: RunSession,
        resetLastPoint: Boolean,
    ) {
        if (trackingJob?.isActive == true) return

        trackingJob =
            scope.launch {
                collectLocation(session, resetLastPoint)
            }
        startInactivityMonitorIfNeeded()
    }

    private suspend fun RunSession.withCurrentUserId(): RunSession {
        if (accountUserId != null) return this
        val userId = currentUserId() ?: return this
        return copy(accountUserId = userId).also { localDataSource.saveSession(it) }
    }

    private fun startInactivityMonitorIfNeeded() {
        if (inactivityJob?.isActive == true) return

        inactivityJob =
            scope.launch {
                while (true) {
                    delay(inactivityCheckIntervalMillis)
                    val lastMovement = lastMovementAt ?: currentSegmentStartedAt ?: now()
                    val inactiveSeconds = Duration.between(lastMovement, now()).seconds
                    if (!isPaused.value && inactiveSeconds >= autoPauseInactivitySeconds) {
                        autoPauseRun()
                        return@launch
                    }
                }
            }
    }

    private suspend fun autoPauseRun() {
        mutex.withLock {
            val activeSession = localDataSource.findActiveSession() ?: return@withLock
            if (isPaused.value) return@withLock

            accumulatedDurationSeconds = currentElapsedSeconds()
            currentSegmentStartedAt = null
            isPaused.value = true
            isAutoPaused = true
            trackingJob?.cancel()
            trackingJob = null
            inactivityJob = null
            saveCurrentMetrics(activeSession, accumulatedDurationSeconds)
            startAutoResumeMonitoringIfNeeded()
            runVoicePlayer.play(RunVoiceCue.PAUSE)
        }
    }

    private fun startAutoResumeMonitoringIfNeeded() {
        if (autoResumeJob?.isActive == true) return

        autoResumeJob =
            scope.launch {
                val currentJob = currentCoroutineContext()[Job]
                try {
                    var stationaryPoint = latestAcceptedPoint
                    var movementCandidatePoint: RunPoint? = null
                    val movementPoint =
                        locationTracker
                            .observeLocation(LocationTrackingMode.AUTO_RESUME)
                            .retryWhen { _, _ ->
                                if (!isAutoPaused) {
                                    false
                                } else {
                                    delay(LocationTrackingMode.AUTO_RESUME.updateIntervalMillis)
                                    true
                                }
                            }.firstOrNull { candidate ->
                                val referencePoint = stationaryPoint
                                if (referencePoint == null) {
                                    stationaryPoint = candidate
                                    false
                                } else {
                                    val movedFromStationaryPoint =
                                        isAutoResumePointPlausible(referencePoint, candidate) &&
                                            movementDistanceMeters(referencePoint, candidate) >=
                                            MOVEMENT_DISTANCE_THRESHOLD_METERS
                                    if (!movedFromStationaryPoint) {
                                        movementCandidatePoint = null
                                        false
                                    } else {
                                        val previousMovementPoint = movementCandidatePoint
                                        movementCandidatePoint = candidate
                                        previousMovementPoint != null &&
                                            isAutoResumePointPlausible(previousMovementPoint, candidate) &&
                                            movementDistanceMeters(previousMovementPoint, candidate) >=
                                            MOVEMENT_DISTANCE_THRESHOLD_METERS
                                    }
                                }
                            }

                    if (movementPoint != null) {
                        resumeAutomatically()
                    }
                } finally {
                    if (autoResumeJob === currentJob) {
                        autoResumeJob = null
                    }
                }
            }
    }

    private suspend fun resumeAutomatically() {
        mutex.withLock {
            val activeSession = localDataSource.findActiveSession() ?: return@withLock
            if (!isPaused.value || !isAutoPaused) return@withLock

            currentSegmentStartedAt = now()
            lastMovementAt = now()
            isPaused.value = false
            isAutoPaused = false
            autoResumeJob = null
            startCollectingIfNeeded(activeSession, resetLastPoint = true)
            runVoicePlayer.play(RunVoiceCue.RESUME)
        }
    }

    private suspend fun collectLocation(
        session: RunSession,
        resetLastPoint: Boolean,
    ) {
        var currentSession = localDataSource.findSession(session.id) ?: session
        var lastPoint = localDataSource.findLastPoint(session.id).takeUnless { resetLastPoint }
        var nextSequence = localDataSource.countPoints(session.id) + 1
        var distanceMeters = currentSession.distanceMeters

        locationTracker
            .observeLocation(LocationTrackingMode.RUNNING)
            .catch {
                trackingJob = null
            }.collect { rawPoint ->
                if (isPaused.value) return@collect

                val point =
                    rawPoint.copy(
                        sessionId = session.id,
                        sequence = nextSequence,
                    )
                if (lastPoint != null && !point.isPlausibleAfter(requireNotNull(lastPoint))) {
                    return@collect
                }
                val movementMeters = lastPoint?.let { metricCalculator.distanceBetweenMeters(it, point) } ?: 0
                if (lastPoint == null || movementMeters >= MOVEMENT_DISTANCE_THRESHOLD_METERS) {
                    lastMovementAt = now()
                }
                latestAcceptedPoint = point
                distanceMeters += movementMeters

                val durationSeconds = currentElapsedSeconds()
                val averagePaceSecondsPerKm =
                    metricCalculator.averagePaceSecondsPerKm(
                        distanceMeters = distanceMeters,
                        durationSeconds = durationSeconds,
                    )
                val calories = metricCalculator.calories(distanceMeters)

                withContext(dispatcher) {
                    localDataSource.savePointAndMetrics(
                        point = point,
                        distanceMeters = distanceMeters,
                        durationSeconds = durationSeconds,
                        averagePaceSecondsPerKm = averagePaceSecondsPerKm,
                        calories = calories,
                    )
                }

                currentSession =
                    currentSession.copy(
                        distanceMeters = distanceMeters,
                        durationSeconds = durationSeconds,
                        averagePaceSecondsPerKm = averagePaceSecondsPerKm,
                        calories = calories,
                    )
                lastPoint = point
                nextSequence += 1
            }
    }

    private fun cancelTrackingJobs() {
        trackingJob?.cancel()
        trackingJob = null
        inactivityJob?.cancel()
        inactivityJob = null
        autoResumeJob?.cancel()
        autoResumeJob = null
    }

    private suspend fun saveCurrentMetrics(
        session: RunSession,
        durationSeconds: Long,
    ) {
        localDataSource.updateRunningMetrics(
            sessionId = session.id,
            distanceMeters = session.distanceMeters,
            durationSeconds = durationSeconds,
            averagePaceSecondsPerKm =
                metricCalculator.averagePaceSecondsPerKm(
                    distanceMeters = session.distanceMeters,
                    durationSeconds = durationSeconds,
                ),
            calories = metricCalculator.calories(session.distanceMeters),
        )
    }
}

private const val AUTO_PAUSE_INACTIVITY_SECONDS = 10L
private const val INACTIVITY_CHECK_INTERVAL_MILLIS = 1_000L
private const val MOVEMENT_DISTANCE_THRESHOLD_METERS = 1
