package com.woowacourse.runpamine.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.shared.app.RunpamineController
import com.woowacourse.runpamine.shared.generated.resources.Res
import com.woowacourse.runpamine.shared.generated.resources.error
import com.woowacourse.runpamine.shared.ui.components.AppTabBar
import com.woowacourse.runpamine.shared.ui.model.AppRoute
import com.woowacourse.runpamine.shared.ui.model.LoginProvider
import com.woowacourse.runpamine.shared.ui.model.MainTab
import com.woowacourse.runpamine.shared.ui.model.RunningPhase
import com.woowacourse.runpamine.shared.ui.model.RunpamineAction
import com.woowacourse.runpamine.shared.ui.screens.auth.LoginScreen
import com.woowacourse.runpamine.shared.ui.screens.auth.NicknameEditorMode
import com.woowacourse.runpamine.shared.ui.screens.auth.NicknameEditorScreen
import com.woowacourse.runpamine.shared.ui.screens.auth.SplashScreen
import com.woowacourse.runpamine.shared.ui.screens.auth.TermsAgreementScreen
import com.woowacourse.runpamine.shared.ui.screens.history.HistoryScreen
import com.woowacourse.runpamine.shared.ui.screens.home.HomeMapPlaceholder
import com.woowacourse.runpamine.shared.ui.screens.home.HomeScreen
import com.woowacourse.runpamine.shared.ui.screens.mypage.MyPageScreen
import com.woowacourse.runpamine.shared.ui.screens.onboarding.OnboardingScreen
import com.woowacourse.runpamine.shared.ui.screens.ranking.RankingScreen
import com.woowacourse.runpamine.shared.ui.screens.running.RunDetailScreen
import com.woowacourse.runpamine.shared.ui.screens.running.RunningScreen
import com.woowacourse.runpamine.shared.ui.screens.running.RunningSummaryScreen
import com.woowacourse.runpamine.shared.ui.screens.team.InviteMemberScreen
import com.woowacourse.runpamine.shared.ui.screens.team.TeamCreateScreen
import com.woowacourse.runpamine.shared.ui.screens.team.TeamCreateUiState
import com.woowacourse.runpamine.shared.ui.screens.team.TeamJoinScreen
import com.woowacourse.runpamine.shared.ui.screens.team.TeamJoinUiState
import com.woowacourse.runpamine.shared.ui.screens.team.TeamMemberDetailSheet
import com.woowacourse.runpamine.shared.ui.screens.team.TeamScreen
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography
import org.jetbrains.compose.resources.painterResource

@Composable
fun RunpamineApp(
    controller: RunpamineController = RunpamineController(),
    homeMapContent: @Composable BoxScope.() -> Unit = {
        HomeMapPlaceholder(modifier = Modifier.fillMaxSize())
    },
) {
    val state = controller.state
    var serviceTermsAccepted by rememberSaveable { mutableStateOf(false) }
    var privacyPolicyAccepted by rememberSaveable { mutableStateOf(false) }
    var nicknameDraft by rememberSaveable(state.route) {
        mutableStateOf(if (state.route == AppRoute.NicknameChange) state.nickname else "")
    }
    var teamNameDraft by rememberSaveable(state.route) { mutableStateOf("") }
    var inviteCodeDraft by rememberSaveable(state.route) { mutableStateOf("") }

    LaunchedEffect(state.route) {
        if (state.route == AppRoute.Login) {
            serviceTermsAccepted = false
            privacyPolicyAccepted = false
        }
    }

    RunpamineTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            when (state.route) {
                AppRoute.Splash ->
                    SplashScreen(
                        onFinished = { controller.dispatch(RunpamineAction.SplashCompleted) },
                    )

                AppRoute.Login ->
                    LoginScreen(
                        isLoading = state.isBusy,
                        errorMessage = state.errorMessage,
                        supportsAppleLogin = state.supportsAppleLogin,
                        onGoogleLogin = { controller.dispatch(RunpamineAction.Login(LoginProvider.Google)) },
                        onAppleLogin = { controller.dispatch(RunpamineAction.Login(LoginProvider.Apple)) },
                    )

                AppRoute.Terms ->
                    TermsAgreementScreen(
                        serviceTermsAccepted = serviceTermsAccepted,
                        privacyPolicyAccepted = privacyPolicyAccepted,
                        onToggleAll = {
                            val next = !(serviceTermsAccepted && privacyPolicyAccepted)
                            serviceTermsAccepted = next
                            privacyPolicyAccepted = next
                        },
                        onToggleServiceTerms = { serviceTermsAccepted = !serviceTermsAccepted },
                        onTogglePrivacyPolicy = { privacyPolicyAccepted = !privacyPolicyAccepted },
                        onOpenServiceTerms = {
                            controller.dispatch(RunpamineAction.OpenExternalUrl(SERVICE_TERMS_URL))
                        },
                        onOpenPrivacyPolicy = {
                            controller.dispatch(RunpamineAction.OpenExternalUrl(PRIVACY_POLICY_URL))
                        },
                        onBack = { controller.dispatch(RunpamineAction.Back) },
                        onComplete = { controller.dispatch(RunpamineAction.TermsCompleted) },
                    )

                AppRoute.NicknameSetup,
                AppRoute.NicknameChange,
                -> {
                    val isChange = state.route == AppRoute.NicknameChange
                    NicknameEditorScreen(
                        nickname = nicknameDraft,
                        mode = if (isChange) NicknameEditorMode.Change else NicknameEditorMode.Setup,
                        isLoading = state.isBusy,
                        errorMessage = state.errorMessage,
                        onNicknameChange = { nicknameDraft = it.take(NICKNAME_MAX_LENGTH) },
                        onBack = { controller.dispatch(RunpamineAction.Back) },
                        onSubmit = { nickname ->
                            controller.dispatch(
                                if (isChange) {
                                    RunpamineAction.ChangeNickname(nickname)
                                } else {
                                    RunpamineAction.NicknameSubmitted(nickname)
                                },
                            )
                        },
                    )
                }

                AppRoute.Onboarding ->
                    OnboardingScreen(
                        onStart = { controller.dispatch(RunpamineAction.OnboardingCompleted) },
                    )

                AppRoute.Main ->
                    MainScreen(
                        controller = controller,
                        homeMapContent = homeMapContent,
                    )

                AppRoute.TeamCreate ->
                    InsetScreen {
                        TeamCreateScreen(
                            state =
                                TeamCreateUiState(
                                    teamName = teamNameDraft,
                                    isLoading = state.isBusy,
                                    errorMessage = state.errorMessage,
                                ),
                            onTeamNameChange = { teamNameDraft = it.take(TEAM_NAME_MAX_LENGTH) },
                            onCreateTeam = { controller.dispatch(RunpamineAction.CreateTeam(it)) },
                            onBack = { controller.dispatch(RunpamineAction.Back) },
                        )
                    }

                AppRoute.TeamJoin ->
                    InsetScreen {
                        TeamJoinScreen(
                            state =
                                TeamJoinUiState(
                                    inviteCode = inviteCodeDraft,
                                    isLoading = state.isBusy,
                                    errorMessage = state.errorMessage,
                                ),
                            onInviteCodeChange = { value ->
                                inviteCodeDraft = value.uppercase().filter(Char::isLetterOrDigit).take(INVITE_CODE_MAX_LENGTH)
                            },
                            onJoinTeam = { controller.dispatch(RunpamineAction.JoinTeam(it)) },
                            onBack = { controller.dispatch(RunpamineAction.Back) },
                        )
                    }

                AppRoute.InviteMember ->
                    InsetScreen {
                        InviteMemberScreen(
                            inviteCode = state.team?.inviteCode.orEmpty(),
                            onCopyCode = { controller.dispatch(RunpamineAction.CopyInviteCode) },
                            onBack = { controller.dispatch(RunpamineAction.Back) },
                        )
                    }

                AppRoute.Running ->
                    RunningScreen(
                        state = state.running,
                        onPause = { controller.dispatch(RunpamineAction.RunningPause) },
                        onResume = { controller.dispatch(RunpamineAction.RunningResume) },
                        onStop = { controller.dispatch(RunpamineAction.RunningStop) },
                        errorMessage = state.errorMessage,
                        locationPermissionGranted = state.hasLocationPermission,
                        onOpenLocationSettings = {
                            controller.dispatch(RunpamineAction.OpenLocationSettings)
                        },
                        onDismissError = {
                            controller.updateState { it.copy(errorMessage = null) }
                        },
                    )

                AppRoute.RunningSummary ->
                    RunningSummaryScreen(
                        state = state.running,
                        onDone = {
                            controller.updateState {
                                it.copy(
                                    route = AppRoute.Main,
                                    selectedTab = MainTab.Home,
                                    running = it.running.copy(phase = RunningPhase.Idle),
                                )
                            }
                        },
                    )

                AppRoute.MyPage ->
                    InsetScreen {
                        MyPageScreen(
                            nickname = state.nickname,
                            appVersion = state.appVersion,
                            onClose = { controller.dispatch(RunpamineAction.Back) },
                            onOpenNicknameChange = { controller.dispatch(RunpamineAction.OpenNicknameChange) },
                            onLogout = { controller.dispatch(RunpamineAction.Logout) },
                            onDeleteAccount = { controller.dispatch(RunpamineAction.DeleteAccount) },
                            onOpenPrivacyPolicy = {
                                controller.dispatch(RunpamineAction.OpenExternalUrl(PRIVACY_POLICY_URL))
                            },
                            onOpenTerms = {
                                controller.dispatch(RunpamineAction.OpenExternalUrl(SERVICE_TERMS_URL))
                            },
                            isBusy = state.isBusy,
                            errorMessage = state.errorMessage,
                        )
                    }

                AppRoute.RunDetail -> {
                    val record = state.selectedRun
                    if (record == null) {
                        controller.dispatch(RunpamineAction.Back)
                    } else {
                        RunDetailScreen(
                            record = record,
                            onBack = { controller.dispatch(RunpamineAction.Back) },
                        )
                    }
                }
            }

            state.selectedTeamMember?.let { member ->
                TeamMemberDetailSheet(
                    member = member,
                    onDismiss = { controller.dispatch(RunpamineAction.DismissOverlay) },
                )
            }

            if (
                !state.isNetworkAvailable &&
                state.route != AppRoute.Running &&
                state.route != AppRoute.RunningSummary
            ) {
                NetworkErrorScreen()
            }
        }
    }
}

@Composable
private fun MainScreen(
    controller: RunpamineController,
    homeMapContent: @Composable BoxScope.() -> Unit,
) {
    val state = controller.state
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (state.selectedTab) {
                MainTab.Home ->
                    HomeScreen(
                        nickname = state.nickname,
                        team = state.team,
                        onCreateTeam = { controller.dispatch(RunpamineAction.OpenTeamCreate) },
                        onJoinTeam = { controller.dispatch(RunpamineAction.OpenTeamJoin) },
                        onOpenTeam = { controller.dispatch(RunpamineAction.SelectTab(MainTab.Team)) },
                        onOpenMyPage = { controller.dispatch(RunpamineAction.OpenMyPage) },
                        onStartRunning = { controller.dispatch(RunpamineAction.OpenRunning) },
                        onRequestLocationPermission = {
                            controller.dispatch(RunpamineAction.RequestLocationPermission)
                        },
                        locationPermissionGranted = state.hasLocationPermission,
                        mapContent = homeMapContent,
                    )

                MainTab.Team ->
                    TeamScreen(
                        team = state.team,
                        dashboard = state.teamDashboard,
                        onCreateTeam = { controller.dispatch(RunpamineAction.OpenTeamCreate) },
                        onJoinTeam = { controller.dispatch(RunpamineAction.OpenTeamJoin) },
                        onInvite = { controller.dispatch(RunpamineAction.OpenInvite) },
                        onLeaveTeam = { controller.dispatch(RunpamineAction.LeaveTeam) },
                        onPreviousDate = { controller.dispatch(RunpamineAction.MoveCalendarPrevious) },
                        onNextDate = { controller.dispatch(RunpamineAction.MoveCalendarNext) },
                        onSelectMember = { controller.dispatch(RunpamineAction.SelectTeamMember(it)) },
                        isLeavingTeam = state.isBusy,
                        leaveTeamErrorMessage = state.errorMessage,
                    )

                MainTab.Ranking ->
                    RankingScreen(
                        state = state.ranking,
                        onScopeSelected = { controller.dispatch(RunpamineAction.SelectRankingScope(it)) },
                        onMetricSelected = { controller.dispatch(RunpamineAction.SelectRankingMetric(it)) },
                    )

                MainTab.History ->
                    HistoryScreen(
                        state = state.history,
                        onPeriodSelected = { controller.dispatch(RunpamineAction.SelectHistoryPeriod(it)) },
                        onPreviousPeriod = { controller.dispatch(RunpamineAction.MoveCalendarPrevious) },
                        onNextPeriod = { controller.dispatch(RunpamineAction.MoveCalendarNext) },
                        onRecordSelected = { controller.dispatch(RunpamineAction.SelectRun(it)) },
                        canMoveNext = state.history.canMoveNextPeriod,
                        onDateSelected = { date ->
                            controller.updateState {
                                it.copy(history = it.history.copy(selectedDate = date))
                            }
                        },
                    )
            }
        }

        AppTabBar(
            selectedTab = state.selectedTab,
            onTabSelected = { controller.dispatch(RunpamineAction.SelectTab(it)) },
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        )
    }
}

@Composable
private fun InsetScreen(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        content()
    }
}

@Composable
private fun NetworkErrorScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.error),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.68f),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "네트워크 연결을 확인해주세요.",
            style = RunpamineTypography.Body1,
            color = RunpamineColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

private const val NICKNAME_MAX_LENGTH = 10
private const val TEAM_NAME_MAX_LENGTH = 10
private const val INVITE_CODE_MAX_LENGTH = 6
private const val SERVICE_TERMS_URL =
    "https://sheer-mimosa-20f.notion.site/37958b8d8e6c8050b988fcc4e6279e25?pvs=74"
private const val PRIVACY_POLICY_URL =
    "https://sheer-mimosa-20f.notion.site/37958b8d8e6c80cdb6b8c29d6d6935f5?pvs=74"
