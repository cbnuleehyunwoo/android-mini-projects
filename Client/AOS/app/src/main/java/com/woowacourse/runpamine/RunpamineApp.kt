package com.woowacourse.runpamine

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.woowacourse.runpamine.data.network.NetworkConnectivityObserver
import com.woowacourse.runpamine.presentation.component.RunpamineBottomBar
import com.woowacourse.runpamine.presentation.component.RunpamineBottomTab
import com.woowacourse.runpamine.presentation.error.ErrorScreen
import com.woowacourse.runpamine.presentation.navigation.AppRoute
import com.woowacourse.runpamine.presentation.navigation.NavHost

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
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }

        if (showNetworkError) {
            ErrorScreen()
        }
    }
}

private fun NavHostController.navigateToBottomTab(tab: RunpamineBottomTab) {
    if (currentDestination?.route == tab.route) return

    RunpamineBottomTab.entries.forEach { bottomTab ->
        popBackStack(
            route = bottomTab.route,
            inclusive = true,
        )
    }
    navigate(tab.route) {
        launchSingleTop = true
    }
}
