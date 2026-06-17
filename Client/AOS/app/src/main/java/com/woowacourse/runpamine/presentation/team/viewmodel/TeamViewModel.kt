package com.woowacourse.runpamine.presentation.team.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.profile.ProfileRepository
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
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState = _uiState.asStateFlow()
    private var selectedDate: LocalDate = LocalDate.now()
    private var currentTeam: Team? = null
    private var teamMembers: List<TeamMemberSummary>? = null
    private var seasonStats: List<TeamMemberSeasonStats>? = null
    private var currentUserId: String? = null

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
                currentUserId =
                    profileRepository.getCachedProfile()?.id
                        ?: profileRepository.getMyProfile()?.id
                teamRepository.getMyTeam()
            }.onSuccess { team ->
                currentTeam = team
                teamMembers = runCatching { teamRepository.getMyTeamMembers() }.getOrNull()
                seasonStats = runCatching { teamRepository.getMyTeamSeasonStats() }.getOrNull()
                loadTeamDailySummary(team, selectedDate, isInitialLoad = true)
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

    fun moveToPreviousDate() {
        moveToDate(selectedDate.minusDays(1))
    }

    fun moveToNextDate() {
        if (selectedDate >= LocalDate.now()) return
        moveToDate(selectedDate.plusDays(1))
    }

    fun leaveTeam() {
        if (uiState.value.isLeavingTeam) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLeavingTeam = true,
                    memberErrorMessage = null,
                )
            }
            runCatching {
                teamRepository.leaveTeam()
            }.onSuccess { result ->
                if (result.left) {
                    runCatching { profileRepository.getMyProfile() }
                    currentTeam = null
                    teamMembers = null
                    seasonStats = null
                }
                _uiState.update {
                    it.copy(
                        isLeavingTeam = false,
                        isTeamLeft = result.left,
                        hasTeam = if (result.left) false else it.hasTeam,
                        teamName = if (result.left) "" else it.teamName,
                        joinCode = if (result.left) "" else it.joinCode,
                        totalDistance = if (result.left) "" else it.totalDistance,
                        completedMemberCount = if (result.left) 0 else it.completedMemberCount,
                        totalMemberCount = if (result.left) 0 else it.totalMemberCount,
                        members = if (result.left) emptyList() else it.members,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLeavingTeam = false,
                        memberErrorMessage = throwable.message ?: "팀 탈퇴에 실패했어요.",
                    )
                }
            }
        }
    }

    private fun moveToDate(date: LocalDate) {
        val team = currentTeam ?: return
        if (uiState.value.isDateLoading) return

        selectedDate = date
        viewModelScope.launch {
            loadTeamDailySummary(team, date, isInitialLoad = false)
        }
    }

    private suspend fun loadTeamDailySummary(
        team: Team,
        date: LocalDate,
        isInitialLoad: Boolean,
    ) {
        _uiState.update {
            it.copy(
                date = date.toKoreanDisplayText(),
                isDateLoading = !isInitialLoad,
                canMoveToNextDate = date < LocalDate.now(),
                errorMessage = null,
            )
        }
        val summaryResult = runCatching { teamRepository.getMyTeamDailySummary(date) }

        summaryResult
            .onSuccess { summary ->
                val members =
                    mergeMembersWithRuns(
                        members = teamMembers,
                        runs = summary.members,
                        seasonStats = seasonStats,
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
                        members = members.markCurrentUser(currentUserId),
                        isLoading = false,
                        isDateLoading = false,
                        canMoveToNextDate = date < LocalDate.now(),
                    )
                }
            }.onFailure { throwable ->
                val members =
                    buildEmptyTeamMembers(
                        members = teamMembers,
                        seasonStats = seasonStats,
                    )
                _uiState.update {
                    it.copy(
                        hasTeam = true,
                        teamName = team.name,
                        joinCode = team.joinCode,
                        date = date.toKoreanDisplayText(),
                        totalDistance = 0.toKilometerText(),
                        completedMemberCount = 0,
                        totalMemberCount = team.memberCount,
                        members = members.markCurrentUser(currentUserId),
                        isLoading = false,
                        isDateLoading = false,
                        canMoveToNextDate = date < LocalDate.now(),
                        errorMessage = throwable.message ?: "팀 기록 정보를 불러오지 못했어요.",
                    )
                }
            }
    }

    class Factory(
        private val teamRepository: TeamRepository,
        private val profileRepository: ProfileRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TeamViewModel::class.java))
            return TeamViewModel(teamRepository, profileRepository) as T
        }
    }
}

private fun List<TeamMember>.markCurrentUser(currentUserId: String?): List<TeamMember> =
    map { member -> member.copy(isMe = member.id == currentUserId) }

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
        member.toEmptyTeamMember(statsByUserId[member.id])
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
            ?: member.toEmptyTeamMember(stats)
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
        runningStatus = seasonStats?.consecutiveRunDays.toRunningStatus(),
        teamJoinedAt = teamJoinedAt.ifBlank { seasonStats?.teamJoinedAt.orEmpty() },
        seasonDistance = totalDistanceMeters.toSeasonKilometerText(),
        seasonDuration = totalDurationSeconds.toDurationText(),
        seasonRunCount = totalRunCount,
        seasonAveragePace = totalAveragePaceSecondsPerKm.toSeasonPaceText(),
        hasTodayRunRecord = hasRunRecord,
    )

private fun TeamMemberSummary.toEmptyTeamMember(seasonStats: TeamMemberSeasonStats? = null): TeamMember =
    TeamMember(
        id = id,
        name = nickname,
        distance = 0.toKilometerText(),
        time = 0.toDurationText(),
        pace = "-",
        calories = "0",
        runningStatus = seasonStats?.consecutiveRunDays.toRunningStatus(),
        teamJoinedAt = seasonStats?.teamJoinedAt.orEmpty(),
        seasonDistance = seasonStats?.seasonDistanceMeters.toSeasonKilometerText(),
        seasonDuration = seasonStats?.seasonDurationSeconds.toDurationText(),
        seasonRunCount = seasonStats?.seasonRunCount ?: 0,
        seasonAveragePace = seasonStats?.averagePaceSecondsPerKm.toSeasonPaceText(),
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
        teamJoinedAt = teamJoinedAt,
        seasonDistance = seasonDistanceMeters.toSeasonKilometerText(),
        seasonDuration = seasonDurationSeconds.toDurationText(),
        seasonRunCount = seasonRunCount,
        seasonAveragePace = averagePaceSecondsPerKm.toSeasonPaceText(),
    )

private val TeamRunSummary.hasRunRecord: Boolean
    get() = completed && (distanceMeters > 0 || durationSeconds > 0)

private fun Int?.toRunningStatus(): RunningStatus =
    when {
        this == null -> RunningStatus.LongResting
        this >= 5 -> RunningStatus.FiveDayRunning
        this == 4 -> RunningStatus.ThreeDayRunning
        this == 3 -> RunningStatus.Running
        this == 2 -> RunningStatus.Resting
        else -> RunningStatus.LongResting
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

private fun Int.toKilometerText(): String = "%.2f km".format(Locale.US, this / 1000.0)

private fun Int?.toSeasonKilometerText(): String = "%.2f".format(Locale.US, (this ?: 0) / 1000.0)

private fun Int?.toDurationText(): String = (this ?: 0).toDurationText()

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

private fun Int?.toSeasonPaceText(): String {
    if (this == null || this <= 0) return "-"
    val minutes = this / 60
    val seconds = this % 60
    return "%d′%02d″".format(Locale.US, minutes, seconds)
}

private fun Throwable.toMemberErrorMessage(): String = message?.let { "팀원 목록을 불러오지 못했어요. ($it)" } ?: "팀원 목록을 불러오지 못했어요."

private const val TAG = "TeamViewModel"
