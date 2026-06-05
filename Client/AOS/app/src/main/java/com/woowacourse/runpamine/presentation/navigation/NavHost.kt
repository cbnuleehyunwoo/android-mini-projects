package com.woowacourse.runpamine.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.woowacourse.runpamine.presentation.createteam.CreateTeamScreen
import com.woowacourse.runpamine.presentation.home.HomeScreen
import com.woowacourse.runpamine.presentation.invite.InviteTeamScreen
import com.woowacourse.runpamine.presentation.join.JoinScreen
import com.woowacourse.runpamine.presentation.mypage.MyPageScreen
import com.woowacourse.runpamine.presentation.nickname.ChangeNicknameScreen
import com.woowacourse.runpamine.presentation.record.RecordScreen
import com.woowacourse.runpamine.presentation.team.TeamScreen

@Composable
fun NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(AppRoute.Home.route) {
            HomeScreen(
                name = "호이",
                onCreateTeamClick = {
                    navController.navigate(AppRoute.CreateTeam.route)
                },
                onJoinTeamClick = {
                    navController.navigate(AppRoute.JoinTeam.route)
                },
                onStartClick = {},
                onMyPageClick = {
                    navController.navigate(AppRoute.MyPage.route)
                },
            )
        }

        composable(AppRoute.Team.route) {
            TeamScreen(
                onInviteClick = {
                    navController.navigate(AppRoute.InviteTeam.route)
                },
            )
        }

        composable(AppRoute.Record.route) {
            RecordScreen()
        }

        composable(AppRoute.CreateTeam.route) {
            CreateTeamScreen(
                onCreateSuccess = { code ->
                    navController.navigate(AppRoute.InviteTeam.createRoute(code))
                },
                onBackClick = navController::popBackStack,
            )
        }

        composable(AppRoute.JoinTeam.route) {
            JoinScreen(
                onBackClick = navController::popBackStack,
            )
        }

        composable(AppRoute.MyPage.route) {
            MyPageScreen(
                onBackClick = navController::popBackStack,
                onChangeNicknameClick = {
                    navController.navigate(AppRoute.ChangeNickname.route)
                },
            )
        }

        composable(AppRoute.ChangeNickname.route) {
            ChangeNicknameScreen(
                onBackClick = navController::popBackStack,
            )
        }

        composable(
            route = AppRoute.InviteTeam.route,
            arguments =
                listOf(
                    navArgument("code") {
                        type = NavType.StringType
                    },
                ),
        ) { backStackEntry ->
            InviteTeamScreen(
                code = backStackEntry.arguments?.getString("code").orEmpty(),
                onBackClick = navController::popBackStack,
            )
        }
    }
}
