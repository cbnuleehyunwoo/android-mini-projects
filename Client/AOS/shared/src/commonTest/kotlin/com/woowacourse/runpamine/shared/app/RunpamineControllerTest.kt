package com.woowacourse.runpamine.shared.app

import com.woowacourse.runpamine.shared.ui.model.AppRoute
import com.woowacourse.runpamine.shared.ui.model.LoginProvider
import com.woowacourse.runpamine.shared.ui.model.MainTab
import com.woowacourse.runpamine.shared.ui.model.RunningPhase
import com.woowacourse.runpamine.shared.ui.model.RunpamineAction
import com.woowacourse.runpamine.shared.ui.model.RunpamineSamples
import com.woowacourse.runpamine.shared.ui.model.RunpamineUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RunpamineControllerTest {
    @Test
    fun onboardingFlow_reachesMain_withoutPlatformListener() {
        val controller = RunpamineController()

        controller.dispatch(RunpamineAction.SplashCompleted)
        assertEquals(AppRoute.Login, controller.state.route)

        controller.dispatch(RunpamineAction.Login(LoginProvider.Google))
        assertEquals(AppRoute.Terms, controller.state.route)

        controller.dispatch(RunpamineAction.TermsCompleted)
        assertEquals(AppRoute.NicknameSetup, controller.state.route)

        controller.dispatch(RunpamineAction.NicknameSubmitted("러너"))
        assertEquals(AppRoute.Onboarding, controller.state.route)
        assertEquals("러너", controller.state.nickname)

        controller.dispatch(RunpamineAction.OnboardingCompleted)
        assertEquals(AppRoute.Main, controller.state.route)
    }

    @Test
    fun backFromTerms_returnsToLogin_whenPlatformChangedRouteDirectly() {
        val controller = RunpamineController(RunpamineUiState(route = AppRoute.Terms))

        controller.dispatch(RunpamineAction.Back)

        assertEquals(AppRoute.Login, controller.state.route)
    }

    @Test
    fun selectingRun_opensDetail_andBackReturnsToMain() {
        val controller = RunpamineController(RunpamineSamples.state)
        val record = RunpamineSamples.records.first()

        controller.dispatch(RunpamineAction.SelectRun(record))
        assertEquals(AppRoute.RunDetail, controller.state.route)
        assertEquals(record, controller.state.selectedRun)

        controller.dispatch(RunpamineAction.Back)
        assertEquals(AppRoute.Main, controller.state.route)
    }

    @Test
    fun runningActions_changePhaseAndOpenSummary() {
        val controller = RunpamineController(RunpamineSamples.state)

        controller.dispatch(RunpamineAction.OpenRunning)
        assertEquals(AppRoute.Running, controller.state.route)
        assertEquals(RunningPhase.Running, controller.state.running.phase)

        controller.dispatch(RunpamineAction.RunningPause)
        assertEquals(RunningPhase.Paused, controller.state.running.phase)

        controller.dispatch(RunpamineAction.RunningResume)
        assertEquals(RunningPhase.Running, controller.state.running.phase)

        controller.dispatch(RunpamineAction.RunningStop)
        assertEquals(AppRoute.RunningSummary, controller.state.route)
        assertEquals(RunningPhase.Completed, controller.state.running.phase)
    }

    @Test
    fun logout_preservesPlatformCapabilities() {
        val controller =
            RunpamineController(
                RunpamineSamples.state.copy(
                    supportsAppleLogin = true,
                    hasLocationPermission = false,
                    appVersion = "2.4.0",
                ),
            )

        controller.dispatch(RunpamineAction.Logout)

        assertEquals(AppRoute.Login, controller.state.route)
        assertTrue(controller.state.supportsAppleLogin)
        assertFalse(controller.state.hasLocationPermission)
        assertEquals("2.4.0", controller.state.appVersion)
    }

    @Test
    fun platformListener_receivesAction_andOwnsAsyncLoginNavigation() {
        val actions = mutableListOf<RunpamineAction>()
        val controller =
            RunpamineController(
                initialState = RunpamineUiState(route = AppRoute.Login),
                actionListener =
                    object : RunpamineActionListener {
                        override fun onAction(action: RunpamineAction) {
                            actions += action
                        }
                    },
            )

        val login = RunpamineAction.Login(LoginProvider.Google)
        controller.dispatch(login)

        assertEquals(listOf<RunpamineAction>(login), actions)
        assertEquals(AppRoute.Login, controller.state.route)
        assertTrue(controller.state.isBusy)

        controller.dispatch(RunpamineAction.SelectTab(MainTab.History))
        assertEquals(MainTab.History, controller.state.selectedTab)
    }

    @Test
    fun platformListener_ownsLogoutCompletion() {
        val controller =
            RunpamineController(
                initialState = RunpamineSamples.state.copy(route = AppRoute.MyPage),
                actionListener =
                    object : RunpamineActionListener {
                        override fun onAction(action: RunpamineAction) = Unit
                    },
            )

        controller.dispatch(RunpamineAction.Logout)

        assertEquals(AppRoute.MyPage, controller.state.route)
        assertTrue(controller.state.isBusy)

        controller.completeSignOut()

        assertEquals(AppRoute.Login, controller.state.route)
        assertFalse(controller.state.isBusy)
    }
}
