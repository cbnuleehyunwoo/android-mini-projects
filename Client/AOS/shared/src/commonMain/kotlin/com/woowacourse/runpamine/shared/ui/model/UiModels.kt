package com.woowacourse.runpamine.shared.ui.model

enum class AppRoute {
    Splash,
    Login,
    Terms,
    NicknameSetup,
    Onboarding,
    Main,
    TeamCreate,
    TeamJoin,
    InviteMember,
    Running,
    RunningSummary,
    MyPage,
    NicknameChange,
    RunDetail,
}

enum class MainTab(
    val label: String,
) {
    Home("홈"),
    Team("팀"),
    Ranking("랭킹"),
    History("기록"),
}

enum class LoginProvider {
    Google,
    Apple,
}

enum class RankingScope {
    Team,
    Individual,
}

enum class RankingMetric {
    Distance,
    Pace,
    Activity,
}

enum class HistoryPeriod {
    Week,
    Month,
}

enum class RunningPhase {
    Idle,
    Running,
    Paused,
    Saving,
    Completed,
}

enum class CharacterMotion {
    Hamburger,
    Idle,
    Running,
    Reverse,
    Cheetah,
}

data class GeoPointUi(
    val latitude: Double,
    val longitude: Double,
)

data class TeamSummaryUi(
    val id: String,
    val name: String,
    val inviteCode: String,
    val completedMemberCount: Int = 0,
    val totalMemberCount: Int = 0,
)

data class TeamMemberUi(
    val id: String,
    val nickname: String,
    val isCurrentUser: Boolean = false,
    val distanceKm: Double = 0.0,
    val durationText: String = "00:00:00",
    val paceText: String = "0'00\"",
    val calories: Int = 0,
    val consecutiveDays: Int = 0,
    val isCompleted: Boolean = false,
    val joinedAtText: String = "",
    val totalRunCount: Int = 0,
    val averagePaceText: String = "0'00\"",
    val seasonDistanceKm: Double = distanceKm,
) {
    val motion: CharacterMotion
        get() =
            when {
                consecutiveDays >= 5 -> CharacterMotion.Cheetah
                consecutiveDays == 4 -> CharacterMotion.Reverse
                consecutiveDays == 3 -> CharacterMotion.Running
                consecutiveDays == 2 -> CharacterMotion.Idle
                else -> CharacterMotion.Hamburger
            }
}

data class TeamDashboardUi(
    val dateText: String = "2026년 7월 16일 - 목요일",
    val totalDistanceKm: Double = 0.0,
    val completedMemberCount: Int = 0,
    val totalMemberCount: Int = 0,
    val members: List<TeamMemberUi> = emptyList(),
    val isLoading: Boolean = false,
    val canMoveNextDate: Boolean = false,
)

data class RankingEntryUi(
    val id: String,
    val rank: Int,
    val name: String,
    val value: String,
    val isCurrent: Boolean = false,
    val percentile: Int? = null,
)

data class RankingUiState(
    val scope: RankingScope = RankingScope.Team,
    val metric: RankingMetric = RankingMetric.Distance,
    val summary: RankingEntryUi? = null,
    val entries: List<RankingEntryUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class RunRecordUi(
    val id: String,
    val dateText: String,
    val distanceKm: Double,
    val durationText: String,
    val paceText: String,
    val calories: Int,
    val startTimeText: String = "",
    val endTimeText: String = "",
    val route: List<GeoPointUi> = emptyList(),
)

data class HistoryUiState(
    val period: HistoryPeriod = HistoryPeriod.Week,
    val periodTitle: String = "이번 주",
    val totalDistanceKm: Double = 0.0,
    val selectedDate: String = "",
    val datesWithRecords: Set<String> = emptySet(),
    val records: List<RunRecordUi> = emptyList(),
    val isLoading: Boolean = false,
    val canMoveNextPeriod: Boolean = false,
)

data class RunningUiState(
    val phase: RunningPhase = RunningPhase.Idle,
    val elapsedText: String = "00:00:00",
    val distanceKm: Double = 0.0,
    val paceText: String = "0'00\"",
    val calories: Int = 0,
    val route: List<GeoPointUi> = emptyList(),
    val dateText: String = "",
    val timeRangeText: String = "",
)

data class RunpamineUiState(
    val route: AppRoute = AppRoute.Splash,
    val previousRoute: AppRoute? = null,
    val selectedTab: MainTab = MainTab.Home,
    val nickname: String = "러너",
    val team: TeamSummaryUi? = null,
    val teamDashboard: TeamDashboardUi = TeamDashboardUi(),
    val ranking: RankingUiState = RankingUiState(),
    val history: HistoryUiState = HistoryUiState(),
    val running: RunningUiState = RunningUiState(),
    val selectedTeamMember: TeamMemberUi? = null,
    val selectedRun: RunRecordUi? = null,
    val isNetworkAvailable: Boolean = true,
    val hasConnectedOnce: Boolean = true,
    val hasLocationPermission: Boolean = true,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val supportsAppleLogin: Boolean = false,
    val appVersion: String = "1.0.0",
)

sealed interface RunpamineAction {
    data object SplashCompleted : RunpamineAction

    data class Login(
        val provider: LoginProvider,
    ) : RunpamineAction

    data object Back : RunpamineAction

    data object TermsCompleted : RunpamineAction

    data class NicknameSubmitted(
        val nickname: String,
    ) : RunpamineAction

    data object OnboardingCompleted : RunpamineAction

    data class SelectTab(
        val tab: MainTab,
    ) : RunpamineAction

    data object OpenMyPage : RunpamineAction

    data object OpenTeamCreate : RunpamineAction

    data object OpenTeamJoin : RunpamineAction

    data object OpenInvite : RunpamineAction

    data object OpenRunning : RunpamineAction

    data object OpenNicknameChange : RunpamineAction

    data class CreateTeam(
        val name: String,
    ) : RunpamineAction

    data class JoinTeam(
        val code: String,
    ) : RunpamineAction

    data object LeaveTeam : RunpamineAction

    data object Logout : RunpamineAction

    data object DeleteAccount : RunpamineAction

    data class ChangeNickname(
        val nickname: String,
    ) : RunpamineAction

    data class SelectRankingScope(
        val scope: RankingScope,
    ) : RunpamineAction

    data class SelectRankingMetric(
        val metric: RankingMetric,
    ) : RunpamineAction

    data class SelectHistoryPeriod(
        val period: HistoryPeriod,
    ) : RunpamineAction

    data class SelectRun(
        val run: RunRecordUi,
    ) : RunpamineAction

    data class SelectTeamMember(
        val member: TeamMemberUi,
    ) : RunpamineAction

    data object DismissOverlay : RunpamineAction

    data object RunningPause : RunpamineAction

    data object RunningResume : RunpamineAction

    data object RunningStop : RunpamineAction

    data object RunningDiscard : RunpamineAction

    data object CopyInviteCode : RunpamineAction

    data class OpenExternalUrl(
        val url: String,
    ) : RunpamineAction

    data object RequestLocationPermission : RunpamineAction

    data object OpenLocationSettings : RunpamineAction

    data object MoveCalendarPrevious : RunpamineAction

    data object MoveCalendarNext : RunpamineAction
}

object RunpamineSamples {
    val team = TeamSummaryUi("team-1", "런앤런", "A1B2C3", 2, 4)
    val members =
        listOf(
            TeamMemberUi("1", "커비", true, 5.24, "00:31:14", "5'58\"", 304, 5, true, "2026. 05. 01", 18, "6'02\""),
            TeamMemberUi("2", "번개", false, 3.16, "00:21:03", "6'40\"", 183, 3, true, "2026. 05. 11", 12, "6'31\""),
            TeamMemberUi("3", "감자", false, 0.0, "00:00:00", "0'00\"", 0, 1, false, "2026. 06. 02", 8, "7'12\""),
        )
    val rankingEntries =
        listOf(
            RankingEntryUi("1", 1, "달려라 하니", "42.18 km"),
            RankingEntryUi("2", 2, "런앤런", "36.42 km", true, 12),
            RankingEntryUi("3", 3, "오늘도 달려", "29.70 km"),
            RankingEntryUi("4", 4, "페이스메이커", "22.31 km"),
        )
    val records =
        listOf(
            RunRecordUi("run-1", "2026. 07. 16 목요일", 5.24, "00:31:14", "5'58\"", 304, "오전 7:10", "오전 7:41"),
            RunRecordUi("run-2", "2026. 07. 14 화요일", 3.16, "00:21:03", "6'40\"", 183, "오후 8:02", "오후 8:23"),
        )
    val state =
        RunpamineUiState(
            route = AppRoute.Main,
            nickname = "커비",
            team = team,
            teamDashboard = TeamDashboardUi(totalDistanceKm = 8.4, completedMemberCount = 2, totalMemberCount = 3, members = members),
            ranking = RankingUiState(summary = rankingEntries[1], entries = rankingEntries),
            history = HistoryUiState(totalDistanceKm = 8.4, records = records),
        )
}
