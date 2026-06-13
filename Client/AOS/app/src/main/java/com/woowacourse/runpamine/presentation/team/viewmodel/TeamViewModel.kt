package com.woowacourse.runpamine.presentation.team.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.team.Team
import com.woowacourse.runpamine.domain.team.TeamMemberSeasonStats
import com.woowacourse.runpamine.domain.team.TeamMemberSummary
import com.woowacourse.runpamine.domain.team.TeamRepository
import com.woowacourse.runpamine.domain.team.TeamRunSummary
import com.woowacourse.runpamine.presentation.team.model.RunningStatus
import com.woowacourse.runpamine.presentation.team.model.TeamMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class TeamViewModel(
    private val teamRepository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTeam()
    }

    fun loadTeam() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    memberErrorMessage = null,
                )
            }
            runCatching {
                teamRepository.getMyTeam()
            }.onSuccess { team ->
                loadTeamDailySummary(team)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        hasTeam = false,
                        isLoading = false,
                        errorMessage = throwable.message ?: "팀 정보를 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    private suspend fun loadTeamDailySummary(team: Team) {
        val membersResult = runCatching { teamRepository.getMyTeamMembers() }
        val seasonStatsResult = runCatching { teamRepository.getMyTeamSeasonStats() }
        val summaryResult = runCatching { teamRepository.getMyTeamDailySummary() }

        summaryResult
            .onSuccess { summary ->
                val members =
                    mergeMembersWithRuns(
                        members = membersResult.getOrNull(),
                        runs = summary.members,
                        seasonStats = seasonStatsResult.getOrNull(),
                    )
                _uiState.update {
                    it.copy(
                        hasTeam = true,
                        teamName = summary.team.name,
                        joinCode = summary.team.joinCode,
                        date = summary.date.toKoreanDisplayText(),
                        totalDistance = summary.teamTotalDistanceMeters.toKilometerText(),
                        completedMemberCount = summary.completedMemberCount,
                        totalMemberCount = summary.totalMemberCount,
                        members = members,
                        isLoading = false,
                        memberErrorMessage = membersResult.exceptionOrNull()?.toMemberErrorMessage(),
                    )
                }
            }.onFailure { throwable ->
                val members =
                    buildEmptyTeamMembers(
                        members = membersResult.getOrNull(),
                        seasonStats = seasonStatsResult.getOrNull(),
                    )
                _uiState.update {
                    it.copy(
                        hasTeam = true,
                        teamName = team.name,
                        joinCode = team.joinCode,
                        date = LocalDate.now().toKoreanDisplayText(),
                        totalDistance = 0.toKilometerText(),
                        completedMemberCount = 0,
                        totalMemberCount = team.memberCount,
                        members = members,
                        isLoading = false,
                        errorMessage = throwable.message ?: "팀 기록 정보를 불러오지 못했어요.",
                        memberErrorMessage = membersResult.exceptionOrNull()?.toMemberErrorMessage(),
                    )
                }
            }
    }

    class Factory(
        private val teamRepository: TeamRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TeamViewModel::class.java))
            return TeamViewModel(teamRepository) as T
        }
    }
}

private fun buildEmptyTeamMembers(
    members: List<TeamMemberSummary>?,
    seasonStats: List<TeamMemberSeasonStats>?,
): List<TeamMember> {
    val statsByUserId = seasonStats.orEmpty().associateBy { stats -> stats.id }
    if (members == null) {
        return seasonStats
            .orEmpty()
            .map { stats -> stats.toEmptyTeamMember(stats.consecutiveRunDays.toRunningStatus()) }
    }

    return members.map { member ->
        member.toEmptyTeamMember(statsByUserId[member.id]?.consecutiveRunDays.toRunningStatus())
    }
}

private fun mergeMembersWithRuns(
    members: List<TeamMemberSummary>?,
    runs: List<TeamRunSummary>,
    seasonStats: List<TeamMemberSeasonStats>?,
): List<TeamMember> {
    val statsByUserId = seasonStats.orEmpty().associateBy { stats -> stats.id }
    if (members == null) {
        if (seasonStats != null) {
            val runsByUserId = runs.associateBy { run -> run.userId }
            return seasonStats.map { stats ->
                runsByUserId[stats.id]?.toTeamMember(stats)
                    ?: stats.toEmptyTeamMember(stats.consecutiveRunDays.toRunningStatus())
            }
        }
        return runs.map { run -> run.toTeamMember(statsByUserId[run.userId]) }
    }

    val runsByUserId = runs.associateBy { run -> run.userId }
    return members.map { member ->
        val stats = statsByUserId[member.id]
        runsByUserId[member.id]?.toTeamMember(stats)
            ?: member.toEmptyTeamMember(stats?.consecutiveRunDays.toRunningStatus())
    }
}

private fun TeamRunSummary.toTeamMember(seasonStats: TeamMemberSeasonStats?): TeamMember =
    TeamMember(
        id = userId,
        name = nickname,
        distance = distanceMeters.toKilometerText(),
        time = durationSeconds.toDurationText(),
        pace = averagePaceSecondsPerKm.toPaceText(),
        calories = calories.toString(),
        runningStatus = seasonStats?.consecutiveRunDays.toRunningStatus(hasRunRecord = hasRunRecord),
    )

private fun TeamMemberSummary.toEmptyTeamMember(runningStatus: RunningStatus = RunningStatus.Resting): TeamMember =
    TeamMember(
        id = id,
        name = nickname,
        distance = 0.toKilometerText(),
        time = 0.toDurationText(),
        pace = "-",
        calories = "0",
        runningStatus = runningStatus,
    )

private fun TeamMemberSeasonStats.toEmptyTeamMember(runningStatus: RunningStatus = RunningStatus.Resting): TeamMember =
    TeamMember(
        id = id,
        name = nickname,
        distance = 0.toKilometerText(),
        time = 0.toDurationText(),
        pace = "-",
        calories = "0",
        runningStatus = runningStatus,
    )

private val TeamRunSummary.hasRunRecord: Boolean
    get() = completed && (distanceMeters > 0 || durationSeconds > 0)

private fun Int?.toRunningStatus(hasRunRecord: Boolean = false): RunningStatus =
    when {
        this == null && hasRunRecord -> RunningStatus.Running
        this == null -> RunningStatus.Resting
        this >= 5 -> RunningStatus.FiveDayRunning
        this >= 3 -> RunningStatus.ThreeDayRunning
        this > 0 -> RunningStatus.Running
        this <= -5 -> RunningStatus.LongResting
        else -> RunningStatus.Resting
    }

private fun String.toKoreanDisplayText(): String =
    runCatching {
        val localDate = LocalDate.parse(this)
        localDate.toKoreanDisplayText()
    }.getOrDefault(this)

private fun LocalDate.toKoreanDisplayText(): String {
    val dayOfWeek = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
    return "${year}년 ${monthValue}월 ${dayOfMonth}일 - $dayOfWeek"
}

private fun Int.toKilometerText(): String = "%.1f km".format(Locale.US, this / 1000.0)

private fun Int.toDurationText(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(Locale.US, hours, minutes, seconds)
    } else {
        "%d:%02d".format(Locale.US, minutes, seconds)
    }
}

private fun Int.toPaceText(): String {
    if (this <= 0) return "-"
    val minutes = this / 60
    val seconds = this % 60
    return "%d'%02d\"".format(Locale.US, minutes, seconds)
}

private fun Throwable.toMemberErrorMessage(): String = message?.let { "팀원 목록을 불러오지 못했어요. ($it)" } ?: "팀원 목록을 불러오지 못했어요."

private const val TAG = "TeamViewModel"
