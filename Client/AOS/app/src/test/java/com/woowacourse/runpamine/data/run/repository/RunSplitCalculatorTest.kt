package com.woowacourse.runpamine.data.run.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class RunSplitCalculatorTest {
    private val calculator = RunSplitCalculator()

    @Test
    fun `거리 지점 사이의 1km 경과 시간을 보간한다`() {
        val splits =
            calculator.appendCompletedSplits(
                completedSplits = emptyList(),
                previousDistanceMeters = 900,
                currentDistanceMeters = 1_100,
                previousElapsedMillis = 300_000,
                currentElapsedMillis = 380_000,
            )

        assertEquals(1, splits.size)
        assertEquals(340_000L, splits.single().durationMillis)
        assertEquals(340.0, splits.single().paceSecondsPerKm, 0.001)
    }

    @Test
    fun `종료 시 마지막 부분 구간을 추가하고 거리와 시간을 보존한다`() {
        val firstKilometer =
            calculator.appendCompletedSplits(
                completedSplits = emptyList(),
                previousDistanceMeters = 0,
                currentDistanceMeters = 1_000,
                previousElapsedMillis = 0,
                currentElapsedMillis = 360_000,
            )

        val splits =
            calculator.finalizeSplits(
                completedSplits = firstKilometer,
                totalDistanceMeters = 1_500,
                totalDurationMillis = 570_000,
            )

        assertEquals(listOf(1_000, 500), splits.map { it.distanceMeters })
        assertEquals(listOf(360_000L, 210_000L), splits.map { it.durationMillis })
        assertEquals(listOf(360.0, 420.0), splits.map { it.paceSecondsPerKm })
        assertEquals(1_500, splits.sumOf { it.distanceMeters })
        assertEquals(570_000L, splits.sumOf { it.durationMillis })
    }

    @Test
    fun `정확히 km에서 종료하면 정지 시간을 마지막 구간에 포함한다`() {
        val completed =
            calculator.appendCompletedSplits(
                completedSplits = emptyList(),
                previousDistanceMeters = 0,
                currentDistanceMeters = 1_000,
                previousElapsedMillis = 0,
                currentElapsedMillis = 360_000,
            )

        val splits = calculator.finalizeSplits(completed, 1_000, 420_000)

        assertEquals(1, splits.size)
        assertEquals(420_000L, splits.single().durationMillis)
        assertEquals(420.0, splits.single().paceSecondsPerKm, 0.001)
    }
}
