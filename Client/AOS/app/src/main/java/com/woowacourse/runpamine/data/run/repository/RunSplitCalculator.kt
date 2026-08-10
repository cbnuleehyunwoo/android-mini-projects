package com.woowacourse.runpamine.data.run.repository

import com.woowacourse.runpamine.domain.run.RunSplit
import kotlin.math.roundToLong

class RunSplitCalculator {
    fun appendCompletedSplits(
        completedSplits: List<RunSplit>,
        previousDistanceMeters: Int,
        currentDistanceMeters: Int,
        previousElapsedMillis: Long,
        currentElapsedMillis: Long,
    ): List<RunSplit> {
        if (currentDistanceMeters <= previousDistanceMeters) return completedSplits

        val updated = completedSplits.toMutableList()
        var nextBoundaryMeters = (updated.size + 1) * METERS_PER_KILOMETER
        while (nextBoundaryMeters <= currentDistanceMeters) {
            val progress =
                (nextBoundaryMeters - previousDistanceMeters).toDouble() /
                    (currentDistanceMeters - previousDistanceMeters)
            val boundaryElapsedMillis =
                previousElapsedMillis +
                    ((currentElapsedMillis - previousElapsedMillis) * progress).roundToLong()
            val previousBoundaryElapsedMillis = updated.sumOf { it.durationMillis }
            val durationMillis = (boundaryElapsedMillis - previousBoundaryElapsedMillis).coerceAtLeast(1)
            updated +=
                RunSplit(
                    sequence = updated.size + 1,
                    fromDistanceMeters = nextBoundaryMeters - METERS_PER_KILOMETER,
                    toDistanceMeters = nextBoundaryMeters,
                    distanceMeters = METERS_PER_KILOMETER,
                    durationMillis = durationMillis,
                    paceSecondsPerKm = durationMillis / MILLIS_PER_SECOND.toDouble(),
                )
            nextBoundaryMeters += METERS_PER_KILOMETER
        }
        return updated
    }

    fun finalizeSplits(
        completedSplits: List<RunSplit>,
        totalDistanceMeters: Int,
        totalDurationMillis: Long,
    ): List<RunSplit> {
        if (totalDistanceMeters <= 0 || totalDurationMillis < 0) return emptyList()

        val fullSplitDistanceMeters = completedSplits.size * METERS_PER_KILOMETER
        val remainingDistanceMeters = totalDistanceMeters - fullSplitDistanceMeters
        val completedDurationMillis = completedSplits.sumOf { it.durationMillis }

        if (remainingDistanceMeters > 0) {
            val durationMillis = (totalDurationMillis - completedDurationMillis).coerceAtLeast(1)
            return completedSplits +
                RunSplit(
                    sequence = completedSplits.size + 1,
                    fromDistanceMeters = fullSplitDistanceMeters,
                    toDistanceMeters = totalDistanceMeters,
                    distanceMeters = remainingDistanceMeters,
                    durationMillis = durationMillis,
                    paceSecondsPerKm =
                        durationMillis * METERS_PER_KILOMETER /
                            (MILLIS_PER_SECOND * remainingDistanceMeters.toDouble()),
                )
        }

        if (completedSplits.isEmpty()) return emptyList()
        val durationDifference = totalDurationMillis - completedDurationMillis
        val last = completedSplits.last()
        val adjustedDurationMillis = (last.durationMillis + durationDifference).coerceAtLeast(1)
        return completedSplits.dropLast(1) +
            last.copy(
                durationMillis = adjustedDurationMillis,
                paceSecondsPerKm = adjustedDurationMillis / MILLIS_PER_SECOND.toDouble(),
            )
    }

    private companion object {
        const val METERS_PER_KILOMETER = 1_000
        const val MILLIS_PER_SECOND = 1_000
    }
}
