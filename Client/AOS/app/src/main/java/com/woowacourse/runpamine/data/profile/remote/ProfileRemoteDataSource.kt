package com.woowacourse.runpamine.data.profile.remote

import com.woowacourse.runpamine.domain.profile.HomeState
import com.woowacourse.runpamine.domain.profile.UserProfile

interface ProfileRemoteDataSource {
    suspend fun getHomeState(accessToken: String): HomeState

    suspend fun getMyProfile(accessToken: String): UserProfile?

    suspend fun createProfile(
        accessToken: String,
        request: CreateProfileRequest,
    ): UserProfile

    suspend fun updateMyProfile(
        accessToken: String,
        request: CreateProfileRequest,
    ): UserProfile
}
