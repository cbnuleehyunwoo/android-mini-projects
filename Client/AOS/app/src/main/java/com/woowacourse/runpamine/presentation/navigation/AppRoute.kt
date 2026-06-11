package com.woowacourse.runpamine.presentation.navigation

import android.net.Uri

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

    data object History : AppRoute {
        const val DISTANCE = "distance"
        const val TIME = "time"
        const val PACE = "pace"
        const val CALORIES = "calories"

        override val route = "history/{$DISTANCE}/{$TIME}/{$PACE}/{$CALORIES}"

        fun createRoute(
            distance: String,
            time: String,
            pace: String,
            calories: String,
        ): String =
            listOf("history", distance, time, pace, calories)
                .joinToString(separator = "/") { value -> Uri.encode(value) }
    }

    data object Ranking : AppRoute {
        override val route = "ranking"
    }

    data object Running : AppRoute {
        override val route = "running"
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

    data object TermsAgreement : AppRoute {
        override val route = "terms_agreement"
    }

    data object ChangeNickname : AppRoute {
        override val route = "change_nickname"
    }

    data object InviteTeam : AppRoute {
        override val route = "invite_team/{code}"

        fun createRoute(code: String): String = "invite_team/$code"
    }
}
