package com.woowacourse.runpamine.domain.run

import java.time.LocalDate
import java.time.YearMonth

interface RunRecordRepository {
    suspend fun getWeeklyRuns(anchorDate: LocalDate): RunPeriodSummary

    suspend fun getMonthlyRuns(yearMonth: YearMonth): RunPeriodSummary
}
