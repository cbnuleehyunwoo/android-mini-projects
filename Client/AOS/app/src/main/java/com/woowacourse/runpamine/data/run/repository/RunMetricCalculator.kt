package com.woowacourse.runpamine.data.run.repository

import android.location.Location
import com.woowacourse.runpamine.domain.run.RunPoint
import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt

class RunMetricCalculator {
    fun distanceBetweenMeters(
        from: RunPoint,
        to: RunPoint,
    ): Int {
        val fromLocation =
            Location("previous").apply {
                latitude = from.latitude
                longitude = from.longitude
            }
        val toLocation =
            Location("current").apply {
                latitude = to.latitude
                longitude = to.longitude
            }
        return fromLocation.distanceTo(toLocation).roundToInt()
    }

    fun durationSeconds(
        startedAt: Instant,
        endedAt: Instant,
    ): Long = Duration.between(startedAt, endedAt).seconds.coerceAtLeast(0)

    fun calories(distanceMeters: Int): Int {
        val distanceKm = distanceMeters / METERS_PER_KILOMETER
        return (distanceKm * CALORIES_PER_KILOMETER).roundToInt()
    }

    fun averagePaceSecondsPerKm(
        distanceMeters: Int,
        durationSeconds: Long,
    ): Int {
        if (distanceMeters <= 0) return 0
        return (durationSeconds / (distanceMeters / METERS_PER_KILOMETER)).roundToInt()
    }

    private companion object {
        const val METERS_PER_KILOMETER = 1_000.0
        const val CALORIES_PER_KILOMETER = 60
    }
}
