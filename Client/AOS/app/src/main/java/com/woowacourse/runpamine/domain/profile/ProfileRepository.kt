package com.woowacourse.runpamine.domain.profile

interface ProfileRepository {
    fun getCachedProfile(): UserProfile?

    suspend fun getHomeState(): HomeState

    suspend fun getMyProfile(): UserProfile?

    suspend fun createProfile(nickname: String): UserProfile

    suspend fun updateMyProfile(nickname: String): UserProfile
}
