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
import com.woowacourse.runpamine.presentation.history.HistoryScreen
import com.woowacourse.runpamine.presentation.home.HomeScreen
import com.woowacourse.runpamine.presentation.invite.InviteTeamScreen
import com.woowacourse.runpamine.presentation.join.JoinScreen
import com.woowacourse.runpamine.presentation.login.LoginScreen
import com.woowacourse.runpamine.presentation.login.viewmodel.LoginDestination
import com.woowacourse.runpamine.presentation.onboarding.OnboardingScreen
import com.woowacourse.runpamine.presentation.mypage.MyPageScreen
import com.woowacourse.runpamine.presentation.nickname.ChangeNicknameScreen
import com.woowacourse.runpamine.presentation.ranking.RankingScreen
import com.woowacourse.runpamine.presentation.record.RecordScreen
import com.woowacourse.runpamine.presentation.running.RunningScreen
import com.woowacourse.runpamine.presentation.splash.SplashDestination
import com.woowacourse.runpamine.presentation.splash.SplashScreen
import com.woowacourse.runpamine.presentation.team.TeamScreen
import com.woowacourse.runpamine.presentation.termsagreement.TermsAgreementScreen
import java.util.Locale

@Composable
fun NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(AppRoute.Splash.route) {
            SplashScreen(
                onSplashFinished = { destination ->
                    val route =
                        when (destination) {
                            SplashDestination.LOGIN -> AppRoute.Login.route
                            SplashDestination.TERMS_AGREEMENT -> AppRoute.TermsAgreement.route
                            SplashDestination.HOME -> AppRoute.Home.route
                        }
                    navController.navigate(route) {
                        popUpTo(AppRoute.Splash.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(AppRoute.Onboarding.route) {
            OnboardingScreen(
                onStartClick = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = { destination ->
                    val route =
                        when (destination) {
                            LoginDestination.NICKNAME -> AppRoute.TermsAgreement.route
                            LoginDestination.HOME -> AppRoute.Home.route
                        }
                    navController.navigate(route) {
                        popUpTo(AppRoute.Login.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(AppRoute.Home.route) {
            HomeScreen(
                onCreateTeamClick = {
                    navController.navigate(AppRoute.CreateTeam.route)
                },
                onJoinTeamClick = {
                    navController.navigate(AppRoute.JoinTeam.route)
                },
                onStartClick = {
                    navController.navigate(AppRoute.Running.route)
                },
                onMyPageClick = {
                    navController.navigate(AppRoute.MyPage.route)
                },
                onTeamClick = {
                    navController.navigate(AppRoute.Team.route)
                },
            )
        }

        composable(AppRoute.Team.route) {
            TeamScreen(
                onInviteClick = { joinCode ->
                    navController.navigate(AppRoute.InviteTeam.createRoute(joinCode))
                },
                onCreateTeamClick = {
                    navController.navigate(AppRoute.CreateTeam.route)
                },
                onJoinTeamClick = {
                    navController.navigate(AppRoute.JoinTeam.route)
                },
            )
        }

        composable(AppRoute.Record.route) {
            RecordScreen(
                onRecordClick = { record ->
                    navController.navigate(
                        AppRoute.History.createRoute(
                            runId = record.id,
                            distance = String.format(Locale.getDefault(), "%.1f", record.distanceKm),
                            time = record.duration,
                            pace = record.pace.removeSuffix("/km"),
                            calories = record.calories.toString(),
                        ),
                    )
                },
            )
        }

        composable(
            route = AppRoute.History.route,
            arguments =
                listOf(
                    navArgument(AppRoute.History.RUN_ID) {
                        type = NavType.StringType
                    },
                    navArgument(AppRoute.History.DISTANCE) {
                        type = NavType.StringType
                    },
                    navArgument(AppRoute.History.TIME) {
                        type = NavType.StringType
                    },
                    navArgument(AppRoute.History.PACE) {
                        type = NavType.StringType
                    },
                    navArgument(AppRoute.History.CALORIES) {
                        type = NavType.StringType
                    },
                ),
        ) { backStackEntry ->
            HistoryScreen(
                runId = backStackEntry.arguments?.getString(AppRoute.History.RUN_ID).orEmpty(),
                distance = backStackEntry.arguments?.getString(AppRoute.History.DISTANCE).orEmpty(),
                time = backStackEntry.arguments?.getString(AppRoute.History.TIME).orEmpty(),
                pace = backStackEntry.arguments?.getString(AppRoute.History.PACE).orEmpty(),
                calories = backStackEntry.arguments?.getString(AppRoute.History.CALORIES).orEmpty(),
                onBack = navController::popBackStack,
            )
        }

        composable(AppRoute.Ranking.route) {
            RankingScreen()
        }

        composable(AppRoute.Running.route) {
            RunningScreen(
                onStopCompleted = {
                    navController.popBackStack()
                },
            )
        }

        composable(AppRoute.CreateTeam.route) {
            CreateTeamScreen(
                onCreateSuccess = { code ->
                    navController.navigate(AppRoute.InviteTeam.createRoute(code)) {
                        popUpTo(AppRoute.CreateTeam.route) {
                            inclusive = true
                        }
                    }
                },
                onBackClick = navController::popBackStack,
            )
        }

        composable(AppRoute.JoinTeam.route) {
            JoinScreen(
                onBackClick = navController::popBackStack,
                onJoinSuccess = {
                    val removedExistingTeam =
                        navController.popBackStack(
                            route = AppRoute.Team.route,
                            inclusive = true,
                        )
                    navController.navigate(AppRoute.Team.route) {
                        if (!removedExistingTeam) {
                            popUpTo(AppRoute.JoinTeam.route) {
                                inclusive = true
                            }
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoute.MyPage.route) {
            MyPageScreen(
                onBackClick = navController::popBackStack,
                onChangeNicknameClick = {
                    navController.navigate(AppRoute.ChangeNickname.route)
                },
                onLogoutCompleted = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(AppRoute.TermsAgreement.route) {
            TermsAgreementScreen(
                onBackClick = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.TermsAgreement.route) {
                            inclusive = true
                        }
                    }
                },
                onJoinClick = {
                    navController.navigate(AppRoute.SetupNickname.route) {
                        popUpTo(AppRoute.TermsAgreement.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(AppRoute.ChangeNickname.route) {
            ChangeNicknameScreen(
                onBackClick = navController::popBackStack,
                onCompleted = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.ChangeNickname.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(AppRoute.SetupNickname.route) {
            ChangeNicknameScreen(
                onBackClick = navController::popBackStack,
                onCompleted = {
                    navController.navigate(AppRoute.Onboarding.route) {
                        popUpTo(AppRoute.SetupNickname.route) {
                            inclusive = true
                        }
                    }
                },
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
