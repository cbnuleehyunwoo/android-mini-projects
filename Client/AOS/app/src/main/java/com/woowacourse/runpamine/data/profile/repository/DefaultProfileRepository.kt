package com.woowacourse.runpamine.data.profile.repository

import com.woowacourse.runpamine.data.profile.remote.CreateProfileRequest
import com.woowacourse.runpamine.data.profile.remote.ProfileRemoteDataSource
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.profile.HomeState
import com.woowacourse.runpamine.domain.profile.ProfileRepository
import com.woowacourse.runpamine.domain.profile.UserProfile

class DefaultProfileRepository(
    private val authRepository: AuthRepository,
    private val remoteDataSource: ProfileRemoteDataSource,
) : ProfileRepository {
    override suspend fun getHomeState(): HomeState =
        remoteDataSource.getHomeState(
            accessToken = requireAccessToken(),
        )

    override suspend fun getMyProfile(): UserProfile? =
        remoteDataSource.getMyProfile(
            accessToken = requireAccessToken(),
        )

    override suspend fun createProfile(nickname: String): UserProfile =
        remoteDataSource.createProfile(
            accessToken = requireAccessToken(),
            request = CreateProfileRequest(nickname = nickname.trim()),
        )

    override suspend fun updateMyProfile(nickname: String): UserProfile =
        remoteDataSource.updateMyProfile(
            accessToken = requireAccessToken(),
            request = CreateProfileRequest(nickname = nickname.trim()),
        )

    private suspend fun requireAccessToken(): String =
        requireNotNull(authRepository.getCurrentSession()?.accessToken) {
            "로그인이 필요해요."
        }
}
