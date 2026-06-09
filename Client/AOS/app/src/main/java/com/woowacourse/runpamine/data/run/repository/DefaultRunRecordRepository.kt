package com.woowacourse.runpamine.data.run.repository

import com.woowacourse.runpamine.data.run.remote.RunRemoteDataSource
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.run.RunPeriodSummary
import com.woowacourse.runpamine.domain.run.RunRecordRepository
import java.time.LocalDate
import java.time.YearMonth

class DefaultRunRecordRepository(
    private val authRepository: AuthRepository,
    private val remoteDataSource: RunRemoteDataSource,
) : RunRecordRepository {
    override suspend fun getWeeklyRuns(anchorDate: LocalDate): RunPeriodSummary =
        remoteDataSource.getWeeklyRuns(
            accessToken = requireAccessToken(),
            anchorDate = anchorDate,
        )

    override suspend fun getMonthlyRuns(yearMonth: YearMonth): RunPeriodSummary =
        remoteDataSource.getMonthlyRuns(
            accessToken = requireAccessToken(),
            yearMonth = yearMonth,
        )

    private suspend fun requireAccessToken(): String =
        requireNotNull(authRepository.getCurrentSession()?.accessToken) {
            "로그인이 필요해요."
        }
}
