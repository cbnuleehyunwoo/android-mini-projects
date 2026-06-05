package com.woowacourse.runpamine.domain.run

data class RunResult(
    val id: String,
    val distanceMeters: Int,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Int,
    val calories: Int,
)
