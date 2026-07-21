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
    private var cachedProfile: UserProfile? = null

    override fun getCachedProfile(): UserProfile? = cachedProfile

    override suspend fun getHomeState(): HomeState =
        remoteDataSource
            .getHomeState(
                accessToken = requireAccessToken(),
            ).also { homeState ->
                cachedProfile = homeState.profile
            }

    override suspend fun getMyProfile(): UserProfile? =
        remoteDataSource
            .getMyProfile(
                accessToken = requireAccessToken(),
            ).also { profile ->
                cachedProfile = profile
            }

    override suspend fun createProfile(nickname: String): UserProfile =
        remoteDataSource
            .createProfile(
                accessToken = requireAccessToken(),
                request = CreateProfileRequest(nickname = nickname.trim()),
            ).also { profile ->
                cachedProfile = profile
            }

    override suspend fun updateMyProfile(nickname: String): UserProfile =
        remoteDataSource
            .updateMyProfile(
                accessToken = requireAccessToken(),
                request = CreateProfileRequest(nickname = nickname.trim()),
            ).also { profile ->
                cachedProfile = profile
            }

    private suspend fun requireAccessToken(): String =
        requireNotNull(authRepository.getCurrentSession()?.accessToken) {
            "로그인이 필요해요."
        }
}
