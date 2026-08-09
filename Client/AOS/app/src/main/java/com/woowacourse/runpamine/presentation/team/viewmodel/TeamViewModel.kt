package com.woowacourse.runpamine.presentation.team.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.profile.ProfileRepository
import com.woowacourse.runpamine.domain.team.Team
import com.woowacourse.runpamine.domain.team.TeamMemberStats
import com.woowacourse.runpamine.domain.team.TeamMemberSummary
import com.woowacourse.runpamine.domain.team.TeamRepository
import com.woowacourse.runpamine.domain.team.TeamRunSummary
import com.woowacourse.runpamine.presentation.cache.TeamDashboardCache
import com.woowacourse.runpamine.presentation.component.LoadingUiTiming
import com.woowacourse.runpamine.presentation.team.model.RunningStatus
import com.woowacourse.runpamine.presentation.team.model.TeamMember
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class TeamViewModel(
    private val teamRepository: TeamRepository,
    private val profileRepository: ProfileRepository,
    private val cache: TeamDashboardCache,
) : ViewModel() {
    private val hadCachedState = cache.state != null
    private var hasResolvedInitialLoad = hadCachedState
    private val _uiState = MutableStateFlow(cache.state ?: TeamUiState())
    val uiState = _uiState.asStateFlow()
    private var selectedDate: LocalDate = cache.selectedDate
    private var currentTeam: Team? = null
    private var teamMembers: List<TeamMemberSummary>? = null
    private var memberStats: List<TeamMemberStats>? = null
    private var currentUserId: String? = null
    private var loadTeamJob: Job? = null

    init {
        viewModelScope.launch {
            uiState.collectLatest { state ->
                cache.state = state
                cache.selectedDate = selectedDate
            }
        }
        loadTeam()
    }

    fun loadTeam() {
        loadTeamJob?.cancel()
        loadTeamJob =
            viewModelScope.launch {
                val shouldGateSkeleton = !hasResolvedInitialLoad
                val loadingStartedAt = TimeSource.Monotonic.markNow()
                _uiState.update { state ->
                    state.copy(
                        isLoading = shouldGateSkeleton,
                        isSkeletonVisible = false,
                        errorMessage = null,
                        memberErrorMessage = null,
                    )
                }
                val skeletonRevealJob =
                    launch {
                        delay(LoadingUiTiming.REVEAL_DELAY_MILLIS)
                        if (shouldGateSkeleton && _uiState.value.isLoading) {
                            _uiState.update { it.copy(isSkeletonVisible = true) }
                        }
                    }

                try {
                    runCatching {
                        currentUserId =
                            profileRepository.getCachedProfile()?.id
                                ?: profileRepository.getMyProfile()?.id
                        teamRepository.getMyTeam()
                    }.onSuccess { team ->
                        currentTeam = team
                        teamMembers = optionalLoadingResult { teamRepository.getMyTeamMembers() }
                        memberStats = optionalLoadingResult { teamRepository.getMyTeamStats() }
                        loadTeamDailySummary(
                            team = team,
                            date = selectedDate,
                            isInitialLoad = true,
                            initialLoadingStartedAt = loadingStartedAt.takeIf { shouldGateSkeleton },
                        )
                        hasResolvedInitialLoad = true
                    }.onFailure { throwable ->
                        if (throwable is CancellationException) throw throwable
                        waitForInitialSkeletonDisplayWindowIfNeeded(
                            loadingStartedAt.takeIf { shouldGateSkeleton },
                        )
                        _uiState.update {
                            it.copy(
                                hasTeam = false,
                                isLoading = false,
                                isSkeletonVisible = false,
                                errorMessage = throwable.message ?: "팀 정보를 불러오지 못했어요.",
                            )
                        }
                        hasResolvedInitialLoad = true
                    }
                } finally {
                    skeletonRevealJob.cancel()
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
                    memberStats = null
                    cache.clear()
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
        initialLoadingStartedAt: TimeMark? = null,
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
        summaryResult.exceptionOrNull()?.let { throwable ->
            if (throwable is CancellationException) throw throwable
        }
        waitForInitialSkeletonDisplayWindowIfNeeded(initialLoadingStartedAt)

        summaryResult
            .onSuccess { summary ->
                val members =
                    mergeMembersWithRuns(
                        members = teamMembers,
                        runs = summary.members,
                        memberStats = memberStats,
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
                        isSkeletonVisible = false,
                        isDateLoading = false,
                        canMoveToNextDate = date < LocalDate.now(),
                    )
                }
            }.onFailure { throwable ->
                val members =
                    buildEmptyTeamMembers(
                        members = teamMembers,
                        memberStats = memberStats,
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
                        isSkeletonVisible = false,
                        isDateLoading = false,
                        canMoveToNextDate = date < LocalDate.now(),
                        errorMessage = throwable.message ?: "팀 기록 정보를 불러오지 못했어요.",
                    )
                }
            }
    }

    private suspend fun waitForInitialSkeletonDisplayWindowIfNeeded(loadingStartedAt: TimeMark?) {
        loadingStartedAt ?: return
        if (!LoadingUiTiming.hasReachedRevealDelay(loadingStartedAt)) return
        _uiState.update { it.copy(isSkeletonVisible = true) }
        LoadingUiTiming.awaitContentReveal(loadingStartedAt)
    }

    private suspend fun <T> optionalLoadingResult(block: suspend () -> T): T? =
        try {
            block()
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            null
        }

    class Factory(
        private val teamRepository: TeamRepository,
        private val profileRepository: ProfileRepository,
        private val cache: TeamDashboardCache,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TeamViewModel::class.java))
            return TeamViewModel(teamRepository, profileRepository, cache) as T
        }
    }
}

private fun List<TeamMember>.markCurrentUser(currentUserId: String?): List<TeamMember> =
    map { member -> member.copy(isMe = member.id == currentUserId) }

private fun buildEmptyTeamMembers(
    members: List<TeamMemberSummary>?,
    memberStats: List<TeamMemberStats>?,
): List<TeamMember> {
    val statsByUserId = memberStats.orEmpty().associateBy { stats -> stats.id }
    if (members == null) {
        return memberStats
            .orEmpty()
            .map { stats -> stats.toEmptyTeamMember(stats.recentRunDays.toRunningStatus()) }
    }

    return members.map { member ->
        member.toEmptyTeamMember(statsByUserId[member.id])
    }
}

private fun mergeMembersWithRuns(
    members: List<TeamMemberSummary>?,
    runs: List<TeamRunSummary>,
    memberStats: List<TeamMemberStats>?,
): List<TeamMember> {
    val statsByUserId = memberStats.orEmpty().associateBy { stats -> stats.id }
    if (members == null) {
        if (memberStats != null) {
            val runsByUserId = runs.associateBy { run -> run.userId }
            return memberStats.map { stats ->
                runsByUserId[stats.id]?.toTeamMember(stats)
                    ?: stats.toEmptyTeamMember(stats.recentRunDays.toRunningStatus())
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

private fun TeamRunSummary.toTeamMember(memberStats: TeamMemberStats?): TeamMember =
    TeamMember(
        id = userId,
        name = nickname,
        distance = distanceMeters.toKilometerText(),
        time = durationSeconds.toDurationText(),
        pace = averagePaceSecondsPerKm.toPaceText(),
        calories = calories.toString(),
        runningStatus = memberStats?.recentRunDays.toRunningStatus(),
        teamJoinedAt = teamJoinedAt.ifBlank { memberStats?.teamJoinedAt.orEmpty() },
        totalDistance = (memberStats?.distanceMeters ?: totalDistanceMeters).toTotalKilometerText(),
        totalDuration = (memberStats?.durationSeconds ?: totalDurationSeconds).toDurationText(),
        totalRunCount = memberStats?.runCount ?: totalRunCount,
        averagePace = (memberStats?.averagePaceSecondsPerKm ?: totalAveragePaceSecondsPerKm).toTotalPaceText(),
        hasTodayRunRecord = hasRunRecord,
    )

private fun TeamMemberSummary.toEmptyTeamMember(memberStats: TeamMemberStats? = null): TeamMember =
    TeamMember(
        id = id,
        name = nickname,
        distance = 0.toKilometerText(),
        time = 0.toDurationText(),
        pace = "-",
        calories = "0",
        runningStatus = memberStats?.recentRunDays.toRunningStatus(),
        teamJoinedAt = memberStats?.teamJoinedAt.orEmpty(),
        totalDistance = memberStats?.distanceMeters.toTotalKilometerText(),
        totalDuration = memberStats?.durationSeconds.toDurationText(),
        totalRunCount = memberStats?.runCount ?: 0,
        averagePace = memberStats?.averagePaceSecondsPerKm.toTotalPaceText(),
    )

private fun TeamMemberStats.toEmptyTeamMember(runningStatus: RunningStatus = RunningStatus.Resting): TeamMember =
    TeamMember(
        id = id,
        name = nickname,
        distance = 0.toKilometerText(),
        time = 0.toDurationText(),
        pace = "-",
        calories = "0",
        runningStatus = runningStatus,
        teamJoinedAt = teamJoinedAt,
        totalDistance = distanceMeters.toTotalKilometerText(),
        totalDuration = durationSeconds.toDurationText(),
        totalRunCount = runCount,
        averagePace = averagePaceSecondsPerKm.toTotalPaceText(),
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

private fun Int?.toTotalKilometerText(): String = "%.2f".format(Locale.US, (this ?: 0) / 1000.0)

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

private fun Int?.toTotalPaceText(): String {
    if (this == null || this <= 0) return "-"
    val minutes = this / 60
    val seconds = this % 60
    return "%d′%02d″".format(Locale.US, minutes, seconds)
}

private fun Throwable.toMemberErrorMessage(): String = message?.let { "팀원 목록을 불러오지 못했어요. ($it)" } ?: "팀원 목록을 불러오지 못했어요."

private const val TAG = "TeamViewModel"
