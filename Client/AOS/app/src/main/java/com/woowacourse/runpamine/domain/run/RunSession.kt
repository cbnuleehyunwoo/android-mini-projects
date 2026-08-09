package com.woowacourse.runpamine.domain.run

import java.time.Instant

data class RunSession(
    val id: String,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val distanceMeters: Int = 0,
    val durationSeconds: Long = 0,
    val averagePaceSecondsPerKm: Int = 0,
    val calories: Int = 0,
    val syncStatus: RunSyncStatus = RunSyncStatus.LOCAL_ONLY,
    val accountUserId: String? = null,
    val routePoints: List<RunPoint> = emptyList(),
    val splits: List<RunSplit> = emptyList(),
)
