package com.woowacourse.runpamine

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.kmp.AndroidRunpamineGateway
import com.woowacourse.runpamine.presentation.home.components.HomeMapSection
import com.woowacourse.runpamine.shared.app.RunpamineController
import com.woowacourse.runpamine.shared.ui.model.AppRoute
import com.woowacourse.runpamine.shared.ui.model.RunpamineAction
import com.woowacourse.runpamine.shared.ui.model.RunpamineUiState
import com.woowacourse.runpamine.shared.RunpamineApp as SharedRunpamineApp

class MainActivity : ComponentActivity() {
    private val hostViewModel: RunpamineHostViewModel by viewModels()
    private var gateway: AndroidRunpamineGateway? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hostViewModel.initialize(
            RunpamineUiState(
                supportsAppleLogin = false,
                appVersion = BuildConfig.VERSION_NAME,
                hasLocationPermission = hasLocationPermission(),
            ),
        )
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
        )
        setContent {
            val controller = hostViewModel.controller
            val gateway =
                remember(controller) {
                    AndroidRunpamineGateway(
                        activity = this,
                        container = applicationContext.runpamineContainer,
                        controller = controller,
                    ).also { gateway = it }
                }

            DisposableEffect(controller, gateway) {
                controller.setActionListener(gateway)
                onDispose {
                    controller.setActionListener(null)
                    gateway.close()
                    if (this@MainActivity.gateway === gateway) this@MainActivity.gateway = null
                }
            }

            val route = controller.state.route
            BackHandler(enabled = route.usesCommonBackAction()) {
                controller.dispatch(RunpamineAction.Back)
            }
            BackHandler(enabled = route == AppRoute.Running || route == AppRoute.RunningSummary) {
                // 진행 중인 러닝이 화면 뒤로가기로 숨겨지지 않도록 막는다.
            }

            SharedRunpamineApp(
                controller = controller,
                homeMapContent = {
                    HomeMapSection(modifier = Modifier.fillMaxSize())
                },
            )
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        gateway?.refreshLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        gateway?.refreshLocationPermission()
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

class RunpamineHostViewModel : ViewModel() {
    val controller = RunpamineController()
    private var initialized = false

    fun initialize(initialState: RunpamineUiState) {
        if (initialized) return
        controller.updateState(initialState)
        initialized = true
    }
}

private fun AppRoute.usesCommonBackAction(): Boolean =
    when (this) {
        AppRoute.Terms,
        AppRoute.NicknameSetup,
        AppRoute.MyPage,
        AppRoute.NicknameChange,
        AppRoute.TeamCreate,
        AppRoute.TeamJoin,
        AppRoute.InviteMember,
        AppRoute.RunDetail,
        -> true

        AppRoute.Splash,
        AppRoute.Login,
        AppRoute.Onboarding,
        AppRoute.Main,
        AppRoute.Running,
        AppRoute.RunningSummary,
        -> false
    }
