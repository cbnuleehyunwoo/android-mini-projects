package com.woowacourse.runpamine.presentation.navigation

sealed interface AppRoute {
    val route: String

    data object Splash : AppRoute {
        override val route = "splash"
    }

    data object Login : AppRoute {
        override val route = "login"
    }

    data object Home : AppRoute {
        override val route = "home"
    }

    data object Team : AppRoute {
        override val route = "team"
    }

    data object Record : AppRoute {
        override val route = "record"
    }

    data object MyPage : AppRoute {
        override val route = "my_page"
    }

    data object CreateTeam : AppRoute {
        override val route = "create_team"
    }

    data object JoinTeam : AppRoute {
        override val route = "join_team"
    }

    data object ChangeNickname : AppRoute {
        override val route = "change_nickname"
    }

    data object InviteTeam : AppRoute {
        override val route = "invite_team/{code}"

        fun createRoute(code: String): String = "invite_team/$code"
    }
}
