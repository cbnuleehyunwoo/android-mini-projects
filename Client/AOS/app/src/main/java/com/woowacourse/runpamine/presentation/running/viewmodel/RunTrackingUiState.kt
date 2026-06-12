package com.woowacourse.runpamine.presentation.running.viewmodel

import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunSession

data class RunTrackingUiState(
    val session: RunSession? = null,
    val routePoints: List<RunPoint> = emptyList(),
    val elapsedSeconds: Long = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val lastErrorMessage: String? = null,
)
