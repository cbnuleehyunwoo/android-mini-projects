package com.woowacourse.runpamine.data.run.repository

import android.location.Location
import com.woowacourse.runpamine.domain.run.RunPoint
import kotlin.math.abs

object RunPointSimplifier {
    fun simplify(points: List<RunPoint>): List<RunPoint> {
        val sortedPoints = points.sortedBy { it.sequence }
        if (sortedPoints.size <= MIN_POINTS_TO_SIMPLIFY) return sortedPoints.resequence()

        val keep = BooleanArray(sortedPoints.size)
        keep[0] = true
        keep[sortedPoints.lastIndex] = true
        simplifyRange(sortedPoints, keep, 0, sortedPoints.lastIndex)

        return sortedPoints
            .filterIndexed { index, _ -> keep[index] }
            .resequence()
    }

    private fun simplifyRange(
        points: List<RunPoint>,
        keep: BooleanArray,
        startIndex: Int,
        endIndex: Int,
    ) {
        if (endIndex <= startIndex + 1) return

        var maxDistance = 0.0
        var maxIndex = startIndex
        for (index in startIndex + 1 until endIndex) {
            val distance = points[index].distanceFromSegment(points[startIndex], points[endIndex])
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = index
            }
        }

        if (maxDistance > EPSILON_METERS) {
            keep[maxIndex] = true
            simplifyRange(points, keep, startIndex, maxIndex)
            simplifyRange(points, keep, maxIndex, endIndex)
        }
    }

    private fun RunPoint.distanceFromSegment(
        start: RunPoint,
        end: RunPoint,
    ): Double {
        val startToEndMeters = start.distanceTo(end).toDouble()
        if (startToEndMeters == 0.0) return start.distanceTo(this).toDouble()

        val x =
            start
                .distanceTo(RunPoint(latitude = start.latitude, longitude = longitude, recordedAt = recordedAt))
                .toDouble() * if (longitude >= start.longitude) 1 else -1
        val y =
            start
                .distanceTo(RunPoint(latitude = latitude, longitude = start.longitude, recordedAt = recordedAt))
                .toDouble() * if (latitude >= start.latitude) 1 else -1
        val endX =
            start
                .distanceTo(RunPoint(latitude = start.latitude, longitude = end.longitude, recordedAt = end.recordedAt))
                .toDouble() * if (end.longitude >= start.longitude) 1 else -1
        val endY =
            start
                .distanceTo(RunPoint(latitude = end.latitude, longitude = start.longitude, recordedAt = end.recordedAt))
                .toDouble() * if (end.latitude >= start.latitude) 1 else -1

        val progress = ((x * endX) + (y * endY)) / ((endX * endX) + (endY * endY))
        val clampedProgress = progress.coerceIn(0.0, 1.0)
        val projectedX = endX * clampedProgress
        val projectedY = endY * clampedProgress
        return kotlin.math.hypot(x - projectedX, y - projectedY)
    }

    private fun List<RunPoint>.resequence(): List<RunPoint> = mapIndexed { index, point -> point.copy(sequence = index + 1) }

    private const val EPSILON_METERS = 8.0
    private const val MIN_POINTS_TO_SIMPLIFY = 2
}

fun RunPoint.isPlausibleAfter(previous: RunPoint): Boolean {
    val elapsedSeconds =
        abs(recordedAt.toEpochMilli() - previous.recordedAt.toEpochMilli()) / MILLIS_PER_SECOND
    if (elapsedSeconds <= 0) return true

    val allowedDistanceMeters = elapsedSeconds * MAX_RUNNING_METERS_PER_SECOND + JUMP_TOLERANCE_METERS
    return previous.distanceTo(this) <= allowedDistanceMeters
}

private const val MAX_RUNNING_METERS_PER_SECOND = 7
private const val JUMP_TOLERANCE_METERS = 6
private const val MILLIS_PER_SECOND = 1_000

private fun RunPoint.distanceTo(other: RunPoint): Int {
    val from =
        Location("from").apply {
            latitude = this@distanceTo.latitude
            longitude = this@distanceTo.longitude
        }
    val to =
        Location("to").apply {
            latitude = other.latitude
            longitude = other.longitude
        }
    return from.distanceTo(to).toInt()
}
