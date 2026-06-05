package com.woowacourse.runpamine.presentation.running.viewmodel

import com.woowacourse.runpamine.domain.run.RunSession

data class RunTrackingUiState(
    val session: RunSession? = null,
    val elapsedSeconds: Long = 0,
    val isRunning: Boolean = false,
    val lastErrorMessage: String? = null,
)
