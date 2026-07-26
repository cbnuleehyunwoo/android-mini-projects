package com.woowacourse.runpamine

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.woowacourse.runpamine.data.network.NetworkConnectivityObserver
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.presentation.component.RunpamineBottomBar
import com.woowacourse.runpamine.presentation.component.RunpamineBottomTab
import com.woowacourse.runpamine.presentation.component.RunpamineConfirmationDialog
import com.woowacourse.runpamine.presentation.error.ErrorScreen
import com.woowacourse.runpamine.presentation.mypage.MyPageBottomSheet
import com.woowacourse.runpamine.presentation.navigation.AppRoute
import com.woowacourse.runpamine.presentation.navigation.NavHost
import com.woowacourse.runpamine.presentation.team.TeamMemberStatsBottomSheet
import com.woowacourse.runpamine.presentation.team.model.TeamMember

@Composable
fun RunpamineApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val networkObserver = remember { NetworkConnectivityObserver(context.applicationContext) }
    val isConnected by
        networkObserver.connectionState.collectAsStateWithLifecycle(
            initialValue = networkObserver.isConnected,
        )
    var hasConnectedOnce by rememberSaveable { mutableStateOf(networkObserver.isConnected) }
    LaunchedEffect(isConnected) {
        if (isConnected) hasConnectedOnce = true
    }
    var showUpdateDialog by rememberSaveable { mutableStateOf(false) }
    var isShowingMyPage by rememberSaveable { mutableStateOf(false) }
    var selectedTeamMember by remember { mutableStateOf<TeamMember?>(null) }
    LaunchedEffect(Unit) {
        AppUpdateManagerFactory
            .create(context)
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    showUpdateDialog = true
                }
            }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showNetworkError = !isConnected && currentRoute != AppRoute.Running.route

    val selectedTab =
        RunpamineBottomTab.entries.firstOrNull { tab ->
            tab.route == currentRoute
        }

    BackHandler(enabled = showNetworkError) {}

    Box(modifier = modifier.fillMaxSize()) {
        if (hasConnectedOnce) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.White,
                bottomBar = {
                    if (selectedTab != null) {
                        RunpamineBottomBar(
                            selectedTab = selectedTab,
                            onTabClick = { tab ->
                                navController.navigateToBottomTab(tab)
                            },
                        )
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    onOpenMyPage = { isShowingMyPage = true },
                    onTeamMemberClick = { member -> selectedTeamMember = member },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }

        if (isShowingMyPage) {
            MyPageBottomSheet(
                onDismissRequest = { isShowingMyPage = false },
                onChangeNicknameClick = {
                    isShowingMyPage = false
                    navController.navigate(AppRoute.ChangeNickname.route)
                },
                onLogoutCompleted = {
                    context.runpamineContainer.clearMainTabCaches()
                    isShowingMyPage = false
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        selectedTeamMember?.let { member ->
            TeamMemberStatsBottomSheet(
                member = member,
                onDismissRequest = { selectedTeamMember = null },
            )
        }

        if (showNetworkError) {
            ErrorScreen()
        }

        if (showUpdateDialog) {
            RunpamineConfirmationDialog(
                title = stringResource(R.string.update_required_title),
                message = stringResource(R.string.update_required_message),
                dismissText = stringResource(R.string.update_required_later),
                confirmText = stringResource(R.string.update_required_confirm),
                onDismiss = { showUpdateDialog = false },
                onConfirm = {
                    showUpdateDialog = false
                    context.openPlayStore()
                },
            )
        }
    }
}

private fun Context.openPlayStore() {
    val appPackageName = packageName
    val marketIntent =
        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")).apply {
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    runCatching {
        startActivity(marketIntent)
    }.onFailure {
        val webIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"),
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        runCatching {
            startActivity(webIntent)
        }
    }
}

private fun NavHostController.navigateToBottomTab(tab: RunpamineBottomTab) {
    if (currentDestination?.route == tab.route) return

    if (popBackStack(route = tab.route, inclusive = false)) return

    RunpamineBottomTab.entries.forEach { bottomTab ->
        popBackStack(
            route = bottomTab.route,
            inclusive = true,
            saveState = true,
        )
    }
    navigate(tab.route) {
        launchSingleTop = true
        restoreState = true
    }
}
