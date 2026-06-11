package com.woowacourse.runpamine

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.woowacourse.runpamine.presentation.component.RunpamineBottomBar
import com.woowacourse.runpamine.presentation.component.RunpamineBottomTab
import com.woowacourse.runpamine.presentation.navigation.NavHost

@Composable
fun RunpamineApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedTab =
        RunpamineBottomTab.entries.firstOrNull { tab ->
            tab.route == currentRoute
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
