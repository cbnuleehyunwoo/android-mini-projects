package com.woowacourse.runpamine.data.auth.google

import android.content.Context

interface GoogleAuthCredentialDataSource {
    suspend fun requestCredential(context: Context): GoogleAuthCredential
}
