package com.woowacourse.runpamine.kmp

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.woowacourse.runpamine.data.network.NetworkConnectivityObserver
import com.woowacourse.runpamine.di.RunpamineContainer
import com.woowacourse.runpamine.domain.profile.HomeState
import com.woowacourse.runpamine.domain.ranking.MyRankingSummary
import com.woowacourse.runpamine.domain.ranking.TeamRanking
import com.woowacourse.runpamine.domain.ranking.UserRanking
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.team.Team
import com.woowacourse.runpamine.domain.team.TeamDailySummary
import com.woowacourse.runpamine.domain.team.TeamMemberSeasonStats
import com.woowacourse.runpamine.service.RunTrackingService
import com.woowacourse.runpamine.shared.app.RunpamineActionListener
import com.woowacourse.runpamine.shared.app.RunpamineController
import com.woowacourse.runpamine.shared.ui.model.AppRoute
import com.woowacourse.runpamine.shared.ui.model.GeoPointUi
import com.woowacourse.runpamine.shared.ui.model.HistoryPeriod
import com.woowacourse.runpamine.shared.ui.model.HistoryUiState
import com.woowacourse.runpamine.shared.ui.model.LoginProvider
import com.woowacourse.runpamine.shared.ui.model.MainTab
import com.woowacourse.runpamine.shared.ui.model.RankingEntryUi
import com.woowacourse.runpamine.shared.ui.model.RankingMetric
import com.woowacourse.runpamine.shared.ui.model.RankingScope
import com.woowacourse.runpamine.shared.ui.model.RankingUiState
import com.woowacourse.runpamine.shared.ui.model.RunRecordUi
import com.woowacourse.runpamine.shared.ui.model.RunningPhase
import com.woowacourse.runpamine.shared.ui.model.RunningUiState
import com.woowacourse.runpamine.shared.ui.model.RunpamineAction
import com.woowacourse.runpamine.shared.ui.model.TeamDashboardUi
import com.woowacourse.runpamine.shared.ui.model.TeamMemberUi
import com.woowacourse.runpamine.shared.ui.model.TeamSummaryUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import com.woowacourse.runpamine.domain.profile.TeamSummary as ProfileTeamSummary
import com.woowacourse.runpamine.domain.ranking.RankingMetric as DomainRankingMetric

class AndroidRunpamineGateway(
    private val activity: Activity,
    private val container: RunpamineContainer,
    private val controller: RunpamineController,
) : RunpamineActionListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val networkObserver = NetworkConnectivityObserver(activity.applicationContext)
    private var teamDate: LocalDate = LocalDate.now()
    private var historyAnchor: LocalDate = LocalDate.now()
    private var actionJob: Job? = null
    private var recoveredRunSessionId: String? = null
    private var isStoppingRun: Boolean = false
    private val runSyncMutex = Mutex()

    init {
        refreshLocationPermission()
        observeConnectivity()
        observeRunning()
    }

    override fun onAction(action: RunpamineAction) {
        when (action) {
            RunpamineAction.SplashCompleted -> launchAction(::restoreSession)
            is RunpamineAction.Login -> if (action.provider == LoginProvider.Google) launchAction(::loginWithGoogle)
            is RunpamineAction.NicknameSubmitted -> launchAction(block = { createProfile(action.nickname) })
            RunpamineAction.OnboardingCompleted -> launchAction(::loadHome)
            is RunpamineAction.SelectTab ->
                launchAction(
                    block = { loadTab(action.tab) },
                    failureTarget = action.tab.toFailureTarget(),
                )
            is RunpamineAction.CreateTeam ->
                launchAction(
                    block = { createTeam(action.name) },
                    failureTarget = ActionFailureTarget.Team,
                )
            is RunpamineAction.JoinTeam ->
                launchAction(
                    block = { joinTeam(action.code) },
                    failureTarget = ActionFailureTarget.Team,
                )
            RunpamineAction.LeaveTeam -> launchAction(::leaveTeam)
            RunpamineAction.Logout -> launchAction(::logout)
            RunpamineAction.DeleteAccount -> launchAction(::deleteAccount)
            is RunpamineAction.ChangeNickname -> launchAction(block = { changeNickname(action.nickname) })
            is RunpamineAction.SelectRankingScope,
            is RunpamineAction.SelectRankingMetric,
            -> launchAction(::loadRanking, ActionFailureTarget.Ranking)

            is RunpamineAction.SelectHistoryPeriod -> launchAction(::loadHistory, ActionFailureTarget.History)
            is RunpamineAction.SelectRun -> launchAction(block = { loadRunDetail(action.run.id) })
            RunpamineAction.MoveCalendarPrevious ->
                launchAction(
                    block = { moveCalendar(-1) },
                    failureTarget = controller.state.selectedTab.toFailureTarget(),
                )
            RunpamineAction.MoveCalendarNext ->
                launchAction(
                    block = { moveCalendar(1) },
                    failureTarget = controller.state.selectedTab.toFailureTarget(),
                )
            RunpamineAction.OpenRunning -> startRunning()
            RunpamineAction.RunningPause -> scope.launch { container.runTrackingRepository.pauseRun() }
            RunpamineAction.RunningResume -> scope.launch { container.runTrackingRepository.resumeRun() }
            RunpamineAction.RunningStop -> launchAction(::stopRunning, ActionFailureTarget.Running)
            RunpamineAction.RunningDiscard -> launchAction(::discardRunning)
            RunpamineAction.CopyInviteCode -> copyInviteCode()
            is RunpamineAction.OpenExternalUrl -> openUrl(action.url)
            RunpamineAction.RequestLocationPermission -> requestLocationPermission()
            RunpamineAction.OpenLocationSettings -> openLocationSettings()
            RunpamineAction.OpenMyPage,
            RunpamineAction.OpenTeamCreate,
            RunpamineAction.OpenTeamJoin,
            RunpamineAction.OpenInvite,
            RunpamineAction.OpenNicknameChange,
            RunpamineAction.TermsCompleted,
            RunpamineAction.Back,
            is RunpamineAction.SelectTeamMember,
            RunpamineAction.DismissOverlay,
            -> Unit
        }
    }

    fun close() {
        scope.cancel()
    }

    fun refreshLocationPermission() {
        val isGranted = hasLocationPermission()
        controller.updateState {
            it.copy(
                hasLocationPermission = isGranted,
                errorMessage = if (isGranted && it.errorMessage?.contains("위치 권한") == true) null else it.errorMessage,
            )
        }
    }

    private fun launchAction(
        block: suspend () -> Unit,
        failureTarget: ActionFailureTarget = ActionFailureTarget.General,
    ) {
        actionJob?.cancel()
        actionJob =
            scope.launch {
                try {
                    block()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (throwable: Throwable) {
                    handleActionFailure(
                        target = failureTarget,
                        message = throwable.message ?: "요청을 처리하지 못했어요.",
                    )
                }
            }
    }

    private fun handleActionFailure(
        target: ActionFailureTarget,
        message: String,
    ) {
        controller.updateState { state ->
            when (target) {
                ActionFailureTarget.General ->
                    state.copy(
                        isBusy = false,
                        errorMessage = message,
                    )
                ActionFailureTarget.Team ->
                    state.copy(
                        teamDashboard = state.teamDashboard.copy(isLoading = false),
                        isBusy = false,
                        errorMessage = message,
                    )
                ActionFailureTarget.Ranking ->
                    state.copy(
                        ranking =
                            state.ranking.copy(
                                isLoading = false,
                                errorMessage = message,
                            ),
                        isBusy = false,
                        errorMessage = message,
                    )
                ActionFailureTarget.History ->
                    state.copy(
                        history = state.history.copy(isLoading = false),
                        isBusy = false,
                        errorMessage = message,
                    )
                ActionFailureTarget.Running ->
                    state.copy(
                        route = AppRoute.Running,
                        running = state.running.copy(phase = RunningPhase.Paused),
                        isBusy = false,
                        errorMessage = message,
                    )
            }
        }
    }

    private suspend fun restoreSession() {
        val session =
            container.authRepository.loadSessionFromStorage()
                ?: container.authRepository.getCurrentSession()
        if (session == null) {
            controller.updateState { it.copy(route = AppRoute.Login, isBusy = false) }
            return
        }
        val profile = container.profileRepository.getMyProfile()
        if (profile == null) {
            controller.updateState { it.copy(route = AppRoute.Terms, isBusy = false) }
        } else {
            controller.updateState { it.copy(nickname = profile.nickname, route = AppRoute.Main, isBusy = false) }
            loadHome()
        }
    }

    private suspend fun loginWithGoogle() {
        controller.updateState { it.copy(isBusy = true, errorMessage = null) }
        val credential = container.googleAuthCredentialDataSource.requestCredential(activity)
        container.authRepository.signInWithGoogleIdToken(credential.idToken, credential.nonce)
        val profile = container.profileRepository.getMyProfile()
        if (profile == null) {
            controller.updateState { it.copy(route = AppRoute.Terms, isBusy = false) }
        } else {
            controller.updateState { it.copy(route = AppRoute.Main, nickname = profile.nickname, isBusy = false) }
            loadHome()
        }
    }

    private suspend fun logout() {
        container.authRepository.signOut()
        controller.completeSignOut()
    }

    private suspend fun deleteAccount() {
        container.authRepository.deleteAccount()
        controller.completeSignOut()
    }

    private suspend fun createProfile(nickname: String) {
        controller.updateState { it.copy(isBusy = true, errorMessage = null) }
        val profile = container.profileRepository.createProfile(nickname.trim())
        controller.updateState { it.copy(route = AppRoute.Onboarding, nickname = profile.nickname, isBusy = false) }
    }

    private suspend fun changeNickname(nickname: String) {
        val profile = container.profileRepository.updateMyProfile(nickname.trim())
        controller.updateState { it.copy(nickname = profile.nickname, isBusy = false) }
        controller.dispatch(RunpamineAction.Back)
    }

    private suspend fun loadTab(tab: MainTab) {
        when (tab) {
            MainTab.Home -> loadHome()
            MainTab.Team -> loadTeamDashboard()
            MainTab.Ranking -> loadRanking()
            MainTab.History -> loadHistory()
        }
    }

    private suspend fun loadHome() {
        val home = container.profileRepository.getHomeState()
        controller.updateState { current ->
            current.copy(
                nickname = home.profile?.nickname ?: current.nickname,
                team = home.team.toUi(),
                isBusy = false,
                errorMessage = null,
            )
        }
        retryPendingRuns()
    }

    private suspend fun createTeam(name: String) {
        controller.updateState { it.copy(isBusy = true, errorMessage = null) }
        val team = container.teamRepository.createTeam(name.trim())
        controller.updateState {
            it.copy(team = team.toUi(), route = AppRoute.Main, selectedTab = MainTab.Team, isBusy = false)
        }
        loadTeamDashboard()
    }

    private suspend fun joinTeam(code: String) {
        controller.updateState { it.copy(isBusy = true, errorMessage = null) }
        val team = container.teamRepository.joinTeam(code.trim().uppercase())
        controller.updateState {
            it.copy(team = team.toUi(), route = AppRoute.Main, selectedTab = MainTab.Team, isBusy = false)
        }
        loadTeamDashboard()
    }

    private suspend fun leaveTeam() {
        container.teamRepository.leaveTeam()
        container.teamDashboardCache.clear()
        controller.updateState {
            it.copy(
                team = null,
                teamDashboard = TeamDashboardUi(),
                selectedTeamMember = null,
                isBusy = false,
            )
        }
    }

    private suspend fun loadTeamDashboard() {
        controller.updateState { it.copy(teamDashboard = it.teamDashboard.copy(isLoading = true), errorMessage = null) }
        val home = container.profileRepository.getHomeState()
        val team = home.team?.let { container.teamRepository.getMyTeam() }
        if (team == null) {
            controller.updateState { it.copy(team = null, teamDashboard = TeamDashboardUi()) }
            return
        }
        val daily = container.teamRepository.getMyTeamDailySummary(teamDate)
        val season =
            try {
                container.teamRepository.getMyTeamSeasonStats()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Throwable) {
                emptyList()
            }
        controller.updateState {
            it.copy(
                nickname = home.profile?.nickname ?: it.nickname,
                team = team.toUi(daily.completedMemberCount, daily.totalMemberCount),
                teamDashboard = daily.toUi(home.profile?.id, season, teamDate),
            )
        }
    }

    private suspend fun loadRanking() {
        val scope = controller.state.ranking.scope
        val metric = controller.state.ranking.metric
        controller.updateState { it.copy(ranking = it.ranking.copy(isLoading = true, errorMessage = null)) }
        val home = container.profileRepository.getHomeState()
        val summary = container.rankingRepository.getMyRankingSummary()
        val rankingState =
            when (scope) {
                RankingScope.Team -> {
                    val rows = container.rankingRepository.getTeamRankings(metric.toDomain())
                    rows.toTeamUiState(home, summary, metric)
                }

                RankingScope.Individual -> {
                    val rows = container.rankingRepository.getUserRankings(metric.toDomain())
                    rows.toUserUiState(home, summary, metric)
                }
            }
        controller.updateState { it.copy(ranking = rankingState) }
    }

    private suspend fun loadHistory() {
        val period = controller.state.history.period
        controller.updateState { it.copy(history = it.history.copy(isLoading = true)) }
        val summary =
            when (period) {
                HistoryPeriod.Week -> container.runRecordRepository.getWeeklyRuns(historyAnchor)
                HistoryPeriod.Month -> container.runRecordRepository.getMonthlyRuns(YearMonth.from(historyAnchor))
            }
        val records = summary.runs.map(RunSession::toUi)
        controller.updateState {
            it.copy(
                history =
                    HistoryUiState(
                        period = period,
                        periodTitle = historyAnchor.toPeriodTitle(period),
                        totalDistanceKm = summary.totalDistanceMeters / 1_000.0,
                        selectedDate = historyAnchor.toString(),
                        datesWithRecords =
                            summary.days
                                .filter { day -> day.hasRun }
                                .map { day -> day.date.toString() }
                                .toSet(),
                        records = records,
                        isLoading = false,
                        canMoveNextPeriod =
                            when (period) {
                                HistoryPeriod.Week -> historyAnchor.isBefore(LocalDate.now())
                                HistoryPeriod.Month -> YearMonth.from(historyAnchor).isBefore(YearMonth.now())
                            },
                    ),
            )
        }
    }

    private suspend fun loadRunDetail(runId: String) {
        val detail = container.runRecordRepository.getRunDetail(runId).toUi()
        controller.updateState { it.copy(selectedRun = detail, isBusy = false) }
    }

    private suspend fun moveCalendar(direction: Int) {
        if (controller.state.selectedTab == MainTab.Team) {
            val next = teamDate.plusDays(direction.toLong())
            if (!next.isAfter(LocalDate.now())) teamDate = next
            loadTeamDashboard()
        } else {
            historyAnchor =
                when (controller.state.history.period) {
                    HistoryPeriod.Week -> historyAnchor.plusWeeks(direction.toLong())
                    HistoryPeriod.Month -> historyAnchor.plusMonths(direction.toLong())
                }.coerceAtMost(LocalDate.now())
            loadHistory()
        }
    }

    private fun startRunning() {
        if (!hasLocationPermission()) {
            controller.dispatch(RunpamineAction.Back)
            requestLocationPermission()
            controller.updateState {
                it.copy(
                    running = it.running.copy(phase = RunningPhase.Idle),
                    hasLocationPermission = false,
                    errorMessage = "러닝을 시작하려면 위치 권한이 필요해요.",
                )
            }
            return
        }
        runCatching {
            ContextCompat.startForegroundService(
                activity,
                RunTrackingService.startIntent(activity, discardActiveRun = false),
            )
        }.onFailure { throwable ->
            controller.dispatch(RunpamineAction.Back)
            controller.updateState {
                it.copy(
                    running = it.running.copy(phase = RunningPhase.Idle),
                    errorMessage = throwable.message ?: "러닝을 시작하지 못했어요.",
                )
            }
        }
    }

    private suspend fun stopRunning() {
        isStoppingRun = true
        try {
            val session = checkNotNull(container.runTrackingRepository.stopRun()) { "종료할 러닝 기록이 없어요." }
            activity.stopService(Intent(activity, RunTrackingService::class.java))
            controller.updateState { it.copy(running = session.toRunningUi(RunningPhase.Completed)) }
            val syncResult = runSyncMutex.withLock { container.runSyncRepository.syncRun(session.id) }
            syncResult.exceptionOrNull()?.let {
                controller.updateState {
                    it.copy(errorMessage = PENDING_RUN_SYNC_MESSAGE)
                }
            }
        } finally {
            isStoppingRun = false
        }
    }

    private suspend fun discardRunning() {
        container.runTrackingRepository.discardActiveRun()
        activity.stopService(Intent(activity, RunTrackingService::class.java))
    }

    private fun observeRunning() {
        scope.launch {
            combine(
                container.runTrackingRepository.observeCurrentRun(),
                container.runTrackingRepository.observeCurrentRoutePoints(),
                container.runTrackingRepository.observePaused(),
                tickerFlow(),
            ) { session, points, paused, _ -> Triple(session, points, paused) }
                .collect { (session, points, paused) ->
                    if (session == null) {
                        recoveredRunSessionId = null
                        return@collect
                    }
                    ensureRunTrackingService(session.id)
                    val phase = if (paused) RunningPhase.Paused else RunningPhase.Running
                    controller.updateState {
                        it.copy(
                            route =
                                if (!isStoppingRun && it.route == AppRoute.Main) {
                                    AppRoute.Running
                                } else {
                                    it.route
                                },
                            running =
                                session.toRunningUi(
                                    phase = phase,
                                    elapsedSeconds = container.runTrackingRepository.currentElapsedSeconds(),
                                    points = points,
                                ),
                        )
                    }
                }
        }
    }

    private fun ensureRunTrackingService(sessionId: String) {
        if (recoveredRunSessionId == sessionId) return
        runCatching {
            ContextCompat.startForegroundService(
                activity,
                RunTrackingService.startIntent(activity, discardActiveRun = false),
            )
        }.onSuccess {
            recoveredRunSessionId = sessionId
        }.onFailure { throwable ->
            controller.updateState {
                it.copy(errorMessage = throwable.message ?: "진행 중인 러닝을 복구하지 못했어요.")
            }
        }
    }

    private fun observeConnectivity() {
        controller.updateState {
            it.copy(
                isNetworkAvailable = networkObserver.isConnected,
                hasConnectedOnce = networkObserver.isConnected,
            )
        }
        scope.launch {
            networkObserver.connectionState.collect { connected ->
                controller.updateState {
                    it.copy(
                        isNetworkAvailable = connected,
                        hasConnectedOnce = it.hasConnectedOnce || connected,
                    )
                }
                if (connected) retryPendingRuns()
            }
        }
    }

    private suspend fun retryPendingRuns() {
        if (container.authRepository.getCurrentSession() == null) return
        val results =
            try {
                runSyncMutex.withLock { container.runSyncRepository.syncPendingRuns() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Throwable) {
                controller.updateState { it.copy(errorMessage = PENDING_RUN_SYNC_MESSAGE) }
                return
            }
        val hasFailure = results.any { it.isFailure }
        controller.updateState {
            it.copy(
                errorMessage =
                    when {
                        hasFailure -> PENDING_RUN_SYNC_MESSAGE
                        it.errorMessage == PENDING_RUN_SYNC_MESSAGE -> null
                        else -> it.errorMessage
                    },
            )
        }
    }

    private fun copyInviteCode() {
        val code =
            controller.state.team
                ?.inviteCode
                .orEmpty()
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("팀 초대 코드", code))
    }

    private fun openUrl(url: String) {
        runCatching { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    private fun requestLocationPermission() {
        val preferences = activity.getSharedPreferences(PERMISSION_PREFERENCES, Context.MODE_PRIVATE)
        val wasRequested = preferences.getBoolean(LOCATION_PERMISSION_REQUESTED, false)
        val canAskAgain =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (wasRequested && !canAskAgain) {
            controller.updateState {
                it.copy(errorMessage = "위치 권한을 사용하려면 앱 설정에서 권한을 허용해 주세요.")
            }
            openLocationSettings()
            return
        }
        preferences.edit().putBoolean(LOCATION_PERMISSION_REQUESTED, true).apply()
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE,
        )
    }

    private fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${activity.packageName}"))
        activity.startActivity(intent)
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

private enum class ActionFailureTarget {
    General,
    Team,
    Ranking,
    History,
    Running,
}

private fun MainTab.toFailureTarget(): ActionFailureTarget =
    when (this) {
        MainTab.Home -> ActionFailureTarget.General
        MainTab.Team -> ActionFailureTarget.Team
        MainTab.Ranking -> ActionFailureTarget.Ranking
        MainTab.History -> ActionFailureTarget.History
    }

private fun ProfileTeamSummary?.toUi(): TeamSummaryUi? =
    this?.let {
        TeamSummaryUi(
            id = id,
            name = name,
            inviteCode = joinCode.orEmpty(),
            completedMemberCount = todayRunMemberCount,
            totalMemberCount = memberCount,
        )
    }

private fun Team.toUi(
    completedMemberCount: Int = 0,
    totalMemberCount: Int = memberCount,
): TeamSummaryUi = TeamSummaryUi(id, name, joinCode, completedMemberCount, totalMemberCount)

private fun TeamDailySummary.toUi(
    currentUserId: String?,
    seasonStats: List<TeamMemberSeasonStats>,
    date: LocalDate,
): TeamDashboardUi {
    val statsById = seasonStats.associateBy(TeamMemberSeasonStats::id)
    return TeamDashboardUi(
        dateText = date.toKoreanDateText(),
        totalDistanceKm = teamTotalDistanceMeters / 1_000.0,
        completedMemberCount = completedMemberCount,
        totalMemberCount = totalMemberCount,
        members =
            members.map { member ->
                val stats = statsById[member.userId]
                TeamMemberUi(
                    id = member.userId,
                    nickname = member.nickname,
                    isCurrentUser = member.userId == currentUserId,
                    distanceKm = member.distanceMeters / 1_000.0,
                    durationText = member.durationSeconds.toDurationText(),
                    paceText = member.averagePaceSecondsPerKm.toPaceText(),
                    calories = member.calories,
                    consecutiveDays = stats?.consecutiveRunDays ?: 0,
                    isCompleted = member.completed && (member.distanceMeters > 0 || member.durationSeconds > 0),
                    joinedAtText = member.teamJoinedAt.ifBlank { stats?.teamJoinedAt.orEmpty() },
                    totalRunCount = stats?.seasonRunCount ?: member.totalRunCount,
                    averagePaceText = (stats?.averagePaceSecondsPerKm ?: member.totalAveragePaceSecondsPerKm ?: 0).toPaceText(),
                    seasonDistanceKm = (stats?.seasonDistanceMeters ?: 0) / 1_000.0,
                )
            },
        isLoading = false,
        canMoveNextDate = date.isBefore(LocalDate.now()),
    )
}

private fun RankingMetric.toDomain(): DomainRankingMetric =
    when (this) {
        RankingMetric.Distance -> DomainRankingMetric.DISTANCE
        RankingMetric.Pace -> DomainRankingMetric.PACE
        RankingMetric.Activity -> DomainRankingMetric.CONSISTENCY
    }

private fun List<TeamRanking>.toTeamUiState(
    home: HomeState,
    summary: MyRankingSummary,
    metric: RankingMetric,
): RankingUiState {
    val myTeamId = home.team?.id
    val entries =
        map { item ->
            RankingEntryUi(
                id = item.teamId,
                rank = item.rank,
                name = item.teamName,
                value = item.valueText(metric, team = true),
                isCurrent = item.teamId == myTeamId,
                percentile = if (item.teamId == myTeamId) summary.percentile(metric) else null,
            )
        }
    return RankingUiState(
        scope = RankingScope.Team,
        metric = metric,
        summary = entries.firstOrNull { it.isCurrent },
        entries = entries,
    )
}

private fun List<UserRanking>.toUserUiState(
    home: HomeState,
    summary: MyRankingSummary,
    metric: RankingMetric,
): RankingUiState {
    val myUserId = home.profile?.id
    val entries =
        map { item ->
            RankingEntryUi(
                id = item.userId,
                rank = item.rank,
                name = item.nickname,
                value = item.valueText(metric),
                isCurrent = item.userId == myUserId,
                percentile = if (item.userId == myUserId) summary.percentile(metric) else null,
            )
        }
    return RankingUiState(
        scope = RankingScope.Individual,
        metric = metric,
        summary = entries.firstOrNull { it.isCurrent },
        entries = entries,
    )
}

private fun TeamRanking.valueText(
    metric: RankingMetric,
    team: Boolean,
): String =
    when (metric) {
        RankingMetric.Distance -> "%.2f km".format(Locale.US, distanceMeters / 1_000.0)
        RankingMetric.Pace -> averagePaceSecondsPerKm.toPaceText() + "/km"
        RankingMetric.Activity -> if (team) "%.1f일".format(Locale.US, averageActiveDays) else "${totalActiveDays}일"
    }

private fun UserRanking.valueText(metric: RankingMetric): String =
    when (metric) {
        RankingMetric.Distance -> "%.2f km".format(Locale.US, distanceMeters / 1_000.0)
        RankingMetric.Pace -> averagePaceSecondsPerKm.toPaceText() + "/km"
        RankingMetric.Activity -> "${activeDays}일"
    }

private fun MyRankingSummary.percentile(metric: RankingMetric): Int? =
    when (metric) {
        RankingMetric.Distance -> distanceTopPercent
        RankingMetric.Pace -> paceTopPercent
        RankingMetric.Activity -> consistencyTopPercent
    }?.toInt()

private fun RunSession.toUi(): RunRecordUi {
    val zone = ZoneId.systemDefault()
    val start = startedAt.atZone(zone)
    val end = endedAt?.atZone(zone)
    return RunRecordUi(
        id = id,
        dateText = start.toLocalDate().toRecordDateText(),
        distanceKm = distanceMeters / 1_000.0,
        durationText = durationSeconds.toDurationText(),
        paceText = averagePaceSecondsPerKm.toPaceText(),
        calories = calories,
        startTimeText = start.toLocalTime().toKoreanTimeText(),
        endTimeText = end?.toLocalTime()?.toKoreanTimeText().orEmpty(),
        route = routePoints.map(RunPoint::toUi),
    )
}

private fun RunSession.toRunningUi(
    phase: RunningPhase,
    elapsedSeconds: Long = durationSeconds,
    points: List<RunPoint> = routePoints,
): RunningUiState {
    val zone = ZoneId.systemDefault()
    val start = startedAt.atZone(zone)
    val end = endedAt?.atZone(zone)
    return RunningUiState(
        phase = phase,
        elapsedText = elapsedSeconds.toDurationText(),
        distanceKm = distanceMeters / 1_000.0,
        paceText = averagePaceSecondsPerKm.toPaceText(),
        calories = calories,
        route = points.map(RunPoint::toUi),
        dateText = start.toLocalDate().toKoreanDateText(separator = " "),
        timeRangeText = "${start.toLocalTime().toKoreanTimeText()} ~ ${end?.toLocalTime()?.toKoreanTimeText().orEmpty()}",
    )
}

private fun RunPoint.toUi(): GeoPointUi = GeoPointUi(latitude, longitude)

private fun Long.toDurationText(): String =
    "%02d:%02d:%02d".format(
        Locale.US,
        this / 3_600,
        (this % 3_600) / 60,
        this % 60,
    )

private fun Int.toDurationText(): String = toLong().toDurationText()

private fun Int.toPaceText(): String =
    if (this <= 0) {
        "0'00\""
    } else {
        "%d'%02d\"".format(Locale.US, this / 60, this % 60)
    }

private fun LocalDate.toKoreanDateText(separator: String = " - "): String {
    val day = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
    return "${year}년 ${monthValue}월 ${dayOfMonth}일${separator}$day"
}

private fun LocalDate.toRecordDateText(): String {
    val day = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
    return "%d. %02d. %02d %s".format(Locale.KOREAN, year, monthValue, dayOfMonth, day)
}

private fun java.time.LocalTime.toKoreanTimeText(): String {
    val period = if (hour < 12) "오전" else "오후"
    val displayHour = (hour % 12).takeIf { it > 0 } ?: 12
    return "%s %d:%02d".format(Locale.KOREAN, period, displayHour, minute)
}

private fun LocalDate.toPeriodTitle(period: HistoryPeriod): String =
    when (period) {
        HistoryPeriod.Week -> {
            val monday = minusDays((dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
            "${monday.monthValue}월 ${monday.dayOfMonth}일 주"
        }

        HistoryPeriod.Month -> "${year}년 ${monthValue}월"
    }

private fun LocalDate.coerceAtMost(maximum: LocalDate): LocalDate = if (isAfter(maximum)) maximum else this

private fun tickerFlow() =
    flow {
        while (true) {
            emit(Unit)
            delay(1_000)
        }
    }

private const val LOCATION_PERMISSION_REQUEST_CODE = 4102
private const val PERMISSION_PREFERENCES = "runpamine_permissions"
private const val LOCATION_PERMISSION_REQUESTED = "location_permission_requested"
private const val PENDING_RUN_SYNC_MESSAGE = "러닝 기록은 기기에 저장됐어요. 네트워크 연결 후 다시 동기화할게요."
