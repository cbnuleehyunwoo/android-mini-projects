package com.woowacourse.runpamine.presentation.component

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark

object LoadingUiTiming {
    const val REVEAL_DELAY_MILLIS = 500L
    private const val CONTENT_REVEAL_MILLIS = 750L

    fun hasReachedRevealDelay(startedAt: TimeMark): Boolean = startedAt.elapsedNow() >= REVEAL_DELAY_MILLIS.milliseconds

    suspend fun awaitContentReveal(startedAt: TimeMark) {
        val remainingMillis = CONTENT_REVEAL_MILLIS - startedAt.elapsedNow().inWholeMilliseconds
        if (remainingMillis > 0) delay(remainingMillis)
    }
}
