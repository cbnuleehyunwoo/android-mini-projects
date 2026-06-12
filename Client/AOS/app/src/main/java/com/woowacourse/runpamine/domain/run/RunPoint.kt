package com.woowacourse.runpamine.domain.run

import java.time.Instant

data class RunPoint(
    val sessionId: String = "",
    val sequence: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val recordedAt: Instant,
    val horizontalAccuracyMeters: Float? = null,
)
