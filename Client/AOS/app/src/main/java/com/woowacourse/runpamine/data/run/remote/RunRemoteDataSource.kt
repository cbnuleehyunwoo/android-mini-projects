package com.woowacourse.runpamine.data.run.remote

import com.woowacourse.runpamine.domain.run.RunPeriodSummary
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunResult
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunSplit
import java.time.LocalDate
import java.time.YearMonth

interface RunRemoteDataSource {
    suspend fun createRun(
        accessToken: String,
        session: RunSession,
        points: List<RunPoint>,
    ): RunResult

    suspend fun getWeeklyRuns(
        accessToken: String,
        anchorDate: LocalDate,
    ): RunPeriodSummary

    suspend fun getMonthlyRuns(
        accessToken: String,
        yearMonth: YearMonth,
    ): RunPeriodSummary

    suspend fun getRunDetail(
        accessToken: String,
        runId: String,
    ): RunSession

    suspend fun getRunSplits(
        accessToken: String,
        runId: String,
    ): List<RunSplit>
}
