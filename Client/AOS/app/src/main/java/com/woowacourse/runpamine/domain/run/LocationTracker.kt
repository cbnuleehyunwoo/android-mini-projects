package com.woowacourse.runpamine.domain.run

import kotlinx.coroutines.flow.Flow

interface LocationTracker {
    fun observeLocation(mode: LocationTrackingMode = LocationTrackingMode.RUNNING): Flow<RunPoint>
}

enum class LocationTrackingMode(
    val updateIntervalMillis: Long,
    val minimumUpdateDistanceMeters: Float,
) {
    RUNNING(
        updateIntervalMillis = 1_000L,
        minimumUpdateDistanceMeters = 1f,
    ),
    AUTO_RESUME(
        updateIntervalMillis = 3_000L,
        minimumUpdateDistanceMeters = 0f,
    ),
}
