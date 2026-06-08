package com.woowacourse.runpamine.presentation.team.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.team.Team
import com.woowacourse.runpamine.domain.team.TeamRepository
import com.woowacourse.runpamine.domain.team.TeamRunSummary
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
        runCatching {
            teamRepository.getMyTeamDailySummary()
        }.onSuccess { summary ->
            _uiState.update {
                it.copy(
                    hasTeam = true,
                    teamName = summary.team.name,
                    joinCode = summary.team.joinCode,
                    date = summary.date.toKoreanDisplayText(),
                    totalDistance = summary.teamTotalDistanceMeters.toKilometerText(),
                    completedMemberCount = summary.completedMemberCount,
                    totalMemberCount = summary.totalMemberCount,
                    members = summary.members.map { member -> member.toTeamMember() },
                    isLoading = false,
                )
            }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(
                    hasTeam = true,
                    teamName = team.name,
                    joinCode = team.joinCode,
                    date = LocalDate.now().toKoreanDisplayText(),
                    totalDistance = 0.toKilometerText(),
                    completedMemberCount = 0,
                    totalMemberCount = team.memberCount,
                    members = emptyList(),
                    isLoading = false,
                    errorMessage = throwable.message ?: "팀 기록 정보를 불러오지 못했어요.",
                )
            }
            loadTeamMembers(team)
        }
    }

    private fun loadTeamMembers(team: Team) {
        viewModelScope.launch {
            runCatching {
                teamRepository.getMyTeamMembers()
            }.onSuccess { members ->
                _uiState.update {
                    it.copy(
                        members =
                            members.map { member ->
                                TeamMember(
                                    id = member.id,
                                    name = member.nickname,
                                    distance = 0.toKilometerText(),
                                    time = 0.toDurationText(),
                                    pace = "-",
                                    calories = "0",
                                )
                            },
                        totalMemberCount = team.memberCount,
                    )
                }
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

private fun TeamRunSummary.toTeamMember(): TeamMember =
    TeamMember(
        id = userId,
        name = nickname,
        distance = distanceMeters.toKilometerText(),
        time = durationSeconds.toDurationText(),
        pace = averagePaceSecondsPerKm.toPaceText(),
        calories = calories.toString(),
    )

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
