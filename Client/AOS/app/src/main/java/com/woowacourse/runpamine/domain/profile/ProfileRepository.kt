package com.woowacourse.runpamine.domain.profile

interface ProfileRepository {
    suspend fun getHomeState(): HomeState

    suspend fun getMyProfile(): UserProfile?

    suspend fun createProfile(nickname: String): UserProfile
}
