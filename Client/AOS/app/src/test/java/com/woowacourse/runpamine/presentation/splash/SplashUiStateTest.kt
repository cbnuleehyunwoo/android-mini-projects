package com.woowacourse.runpamine.presentation.splash

import com.woowacourse.runpamine.domain.profile.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class SplashUiStateTest {
    @Test
    fun `프로필이 없다는 정상 응답만 회원가입 화면으로 이동한다`() {
        val result = Result.success<UserProfile?>(null)

        assertEquals(
            SplashUiState.Completed(SplashDestination.TERMS_AGREEMENT),
            result.toSplashUiState(),
        )
    }

    @Test
    fun `프로필 조회가 실패하면 회원가입 화면으로 이동하지 않는다`() {
        val result = Result.failure<UserProfile?>(IllegalStateException("temporary failure"))

        assertEquals(
            SplashUiState.ProfileLoadFailed,
            result.toSplashUiState(),
        )
    }

    @Test
    fun `프로필이 있으면 홈 화면으로 이동한다`() {
        val result =
            Result.success(
                UserProfile(
                    id = "user-id",
                    nickname = "러너",
                    avatarKey = null,
                    teamId = null,
                ),
            )

        assertEquals(
            SplashUiState.Completed(SplashDestination.HOME),
            result.toSplashUiState(),
        )
    }
}
