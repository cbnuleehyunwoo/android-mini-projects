package com.woowacourse.runpamine.domain.run

data class RunSplit(
    val sequence: Int,
    val fromDistanceMeters: Int,
    val toDistanceMeters: Int,
    val distanceMeters: Int,
    val durationMillis: Long,
    val paceSecondsPerKm: Double,
)
