package com.woowacourse.runpamine.data.auth.google

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
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
            runCatching {
                credentialManager.getCredential(
                    context = context,
                    request = accountPickerRequest(hashedNonce),
                )
            }.recoverCatching { throwable ->
                if (throwable.shouldRetryWithSignInButton()) {
                    credentialManager.getCredential(
                        context = context,
                        request = signInButtonRequest(hashedNonce),
                    )
                } else {
                    throw throwable
                }
            }.getOrThrow()
                .credential

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

private fun Throwable.shouldRetryWithSignInButton(): Boolean = this is GetCredentialException

private fun String.sha256(): String {
    val bytes =
        MessageDigest
            .getInstance("SHA-256")
            .digest(toByteArray())
    return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
}
