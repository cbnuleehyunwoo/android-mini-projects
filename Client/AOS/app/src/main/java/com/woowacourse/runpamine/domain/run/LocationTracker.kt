package com.woowacourse.runpamine.domain.run

import kotlinx.coroutines.flow.Flow

interface LocationTracker {
    fun observeLocation(): Flow<RunPoint>
}
