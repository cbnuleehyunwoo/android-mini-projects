package com.woowacourse.runpamine.shared.app

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.woowacourse.runpamine.shared.ui.model.AppRoute
import com.woowacourse.runpamine.shared.ui.model.MainTab
import com.woowacourse.runpamine.shared.ui.model.RunningPhase
import com.woowacourse.runpamine.shared.ui.model.RunpamineAction
import com.woowacourse.runpamine.shared.ui.model.RunpamineSamples
import com.woowacourse.runpamine.shared.ui.model.RunpamineUiState

interface RunpamineActionListener {
    fun onAction(action: RunpamineAction)
}

@Stable
class RunpamineController(
    initialState: RunpamineUiState = RunpamineUiState(),
    actionListener: RunpamineActionListener? = null,
) {
    private val backStack = mutableListOf<AppRoute>()
    private var actionListener: RunpamineActionListener? = actionListener

    var state: RunpamineUiState by mutableStateOf(initialState)
        private set

    fun updateState(newState: RunpamineUiState) {
        state = newState
    }

    fun updateState(transform: (RunpamineUiState) -> RunpamineUiState) {
        state = transform(state)
    }

    fun setActionListener(listener: RunpamineActionListener?) {
        actionListener = listener
    }

    fun completeSignOut() {
        backStack.clear()
        state =
            RunpamineUiState(
                route = AppRoute.Login,
                supportsAppleLogin = state.supportsAppleLogin,
                appVersion = state.appVersion,
                isNetworkAvailable = state.isNetworkAvailable,
                hasConnectedOnce = state.hasConnectedOnce,
                hasLocationPermission = state.hasLocationPermission,
            )
    }

    fun dispatch(action: RunpamineAction) {
        reduce(action)
        actionListener?.onAction(action)
    }

    private fun reduce(action: RunpamineAction) {
        when (action) {
            RunpamineAction.SplashCompleted -> {
                if (actionListener == null) replace(AppRoute.Login)
            }

            is RunpamineAction.Login -> {
                state = state.copy(isBusy = actionListener != null, errorMessage = null)
                if (actionListener == null) replace(AppRoute.Terms)
            }

            RunpamineAction.Back -> pop()
            RunpamineAction.TermsCompleted -> push(AppRoute.NicknameSetup)
            is RunpamineAction.NicknameSubmitted -> {
                state = state.copy(nickname = action.nickname, isBusy = actionListener != null)
                if (actionListener == null) replace(AppRoute.Onboarding)
            }

            RunpamineAction.OnboardingCompleted -> replace(AppRoute.Main)
            is RunpamineAction.SelectTab -> state = state.copy(route = AppRoute.Main, selectedTab = action.tab)
            RunpamineAction.OpenMyPage -> push(AppRoute.MyPage)
            RunpamineAction.OpenTeamCreate -> push(AppRoute.TeamCreate)
            RunpamineAction.OpenTeamJoin -> push(AppRoute.TeamJoin)
            RunpamineAction.OpenInvite -> push(AppRoute.InviteMember)
            RunpamineAction.OpenRunning -> {
                state = state.copy(running = state.running.copy(phase = RunningPhase.Running))
                push(AppRoute.Running)
            }

            RunpamineAction.OpenNicknameChange -> push(AppRoute.NicknameChange)
            is RunpamineAction.CreateTeam -> {
                if (actionListener == null) {
                    state = state.copy(team = RunpamineSamples.team.copy(name = action.name), selectedTab = MainTab.Team)
                    replace(AppRoute.Main)
                } else {
                    state = state.copy(isBusy = true, errorMessage = null)
                }
            }

            is RunpamineAction.JoinTeam -> {
                if (actionListener == null) {
                    state = state.copy(team = RunpamineSamples.team.copy(inviteCode = action.code), selectedTab = MainTab.Team)
                    replace(AppRoute.Main)
                } else {
                    state = state.copy(isBusy = true, errorMessage = null)
                }
            }

            RunpamineAction.LeaveTeam -> {
                state =
                    if (actionListener == null) {
                        state.copy(team = null, selectedTeamMember = null)
                    } else {
                        state.copy(isBusy = true, errorMessage = null)
                    }
            }
            RunpamineAction.Logout -> {
                if (actionListener == null) {
                    completeSignOut()
                } else {
                    state = state.copy(isBusy = true, errorMessage = null)
                }
            }

            RunpamineAction.DeleteAccount -> {
                if (actionListener == null) {
                    completeSignOut()
                } else {
                    state = state.copy(isBusy = true, errorMessage = null)
                }
            }

            is RunpamineAction.ChangeNickname -> {
                if (actionListener == null) {
                    state = state.copy(nickname = action.nickname)
                    pop()
                } else {
                    state = state.copy(isBusy = true, errorMessage = null)
                }
            }

            is RunpamineAction.SelectRankingScope -> state = state.copy(ranking = state.ranking.copy(scope = action.scope))
            is RunpamineAction.SelectRankingMetric -> state = state.copy(ranking = state.ranking.copy(metric = action.metric))
            is RunpamineAction.SelectHistoryPeriod -> state = state.copy(history = state.history.copy(period = action.period))
            is RunpamineAction.SelectRun -> {
                state = state.copy(selectedRun = action.run)
                push(AppRoute.RunDetail)
            }

            is RunpamineAction.SelectTeamMember -> state = state.copy(selectedTeamMember = action.member)
            RunpamineAction.DismissOverlay -> {
                if (state.selectedTeamMember != null) {
                    state = state.copy(selectedTeamMember = null)
                } else {
                    pop()
                }
            }

            RunpamineAction.RunningPause -> state = state.copy(running = state.running.copy(phase = RunningPhase.Paused))
            RunpamineAction.RunningResume -> state = state.copy(running = state.running.copy(phase = RunningPhase.Running))
            RunpamineAction.RunningStop -> {
                state = state.copy(running = state.running.copy(phase = RunningPhase.Completed))
                replace(AppRoute.RunningSummary)
            }

            RunpamineAction.RunningDiscard -> {
                state = state.copy(running = state.running.copy(phase = RunningPhase.Idle))
                replace(AppRoute.Main)
            }

            RunpamineAction.CopyInviteCode,
            is RunpamineAction.OpenExternalUrl,
            RunpamineAction.RequestLocationPermission,
            RunpamineAction.OpenLocationSettings,
            RunpamineAction.MoveCalendarPrevious,
            RunpamineAction.MoveCalendarNext,
            -> Unit
        }
    }

    private fun push(route: AppRoute) {
        if (state.route != route) backStack += state.route
        state = state.copy(route = route, previousRoute = backStack.lastOrNull())
    }

    private fun replace(route: AppRoute) {
        state = state.copy(route = route, previousRoute = backStack.lastOrNull(), isBusy = false)
    }

    private fun pop() {
        val destination = backStack.removeLastOrNull() ?: state.route.fallbackRoute()
        state = state.copy(route = destination, previousRoute = backStack.lastOrNull(), isBusy = false, errorMessage = null)
    }

    private fun AppRoute.fallbackRoute(): AppRoute =
        when (this) {
            AppRoute.Terms -> AppRoute.Login
            AppRoute.NicknameSetup -> AppRoute.Terms
            AppRoute.NicknameChange -> AppRoute.MyPage
            AppRoute.MyPage,
            AppRoute.TeamCreate,
            AppRoute.TeamJoin,
            AppRoute.InviteMember,
            AppRoute.Running,
            AppRoute.RunningSummary,
            AppRoute.RunDetail,
            -> AppRoute.Main

            AppRoute.Splash,
            AppRoute.Login,
            AppRoute.Onboarding,
            AppRoute.Main,
            -> AppRoute.Main
        }
}
