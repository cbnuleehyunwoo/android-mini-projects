package com.woowacourse.runpamine.data.auth.google

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.security.MessageDigest

class AndroidGoogleAuthCredentialDataSource(
    private val webClientId: String,
    private val nonceGenerator: SecureNonceGenerator = SecureNonceGenerator(),
) : GoogleAuthCredentialDataSource {
    override suspend fun requestCredential(context: Context): GoogleAuthCredential {
        require(webClientId.isNotBlank()) {
            "google_web_client_id is required in local.properties."
        }

        val nonce = nonceGenerator.generate()
        val hashedNonce = nonce.sha256()
        val credentialManager = CredentialManager.create(context)

        val credential =
            try {
                withTimeout(GOOGLE_CREDENTIAL_TIMEOUT_MILLIS) {
                    credentialManager.requestGoogleCredential(
                        context = context,
                        nonce = hashedNonce,
                    )
                }
            } catch (exception: TimeoutCancellationException) {
                throw IllegalStateException(
                    "Google 로그인 응답이 지연되고 있어요. 기기의 Google Play 서비스를 확인한 뒤 다시 시도해 주세요.",
                    exception,
                )
            }.credential

        return try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            GoogleAuthCredential(
                idToken = googleIdTokenCredential.idToken,
                nonce = nonce,
            )
        } catch (exception: GoogleIdTokenParsingException) {
            throw IllegalStateException("Google ID token parsing failed.", exception)
        }
    }

    private suspend fun CredentialManager.requestGoogleCredential(
        context: Context,
        nonce: String,
    ): GetCredentialResponse =
        runCatching {
            getCredential(
                context = context,
                request = signInButtonRequest(nonce),
            )
        }.recoverCatching { throwable ->
            if (throwable.shouldRetryWithAccountPicker()) {
                getCredential(
                    context = context,
                    request = accountPickerRequest(nonce),
                )
            } else {
                throw throwable
            }
        }.getOrThrow()

    private fun accountPickerRequest(nonce: String): GetCredentialRequest {
        val googleIdOption =
            GetGoogleIdOption
                .Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .setNonce(nonce)
                .build()

        return GetCredentialRequest
            .Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    private fun signInButtonRequest(nonce: String): GetCredentialRequest {
        val signInWithGoogleOption =
            GetSignInWithGoogleOption
                .Builder(webClientId)
                .setNonce(nonce)
                .build()

        return GetCredentialRequest
            .Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
    }
}

private fun Throwable.shouldRetryWithAccountPicker(): Boolean = this is NoCredentialException

private fun String.sha256(): String {
    val bytes =
        MessageDigest
            .getInstance("SHA-256")
            .digest(toByteArray())
    return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
}

private const val GOOGLE_CREDENTIAL_TIMEOUT_MILLIS = 15_000L
