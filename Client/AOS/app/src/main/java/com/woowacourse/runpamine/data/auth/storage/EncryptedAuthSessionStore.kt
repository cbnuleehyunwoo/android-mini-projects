package com.woowacourse.runpamine.data.auth.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.woowacourse.runpamine.domain.auth.AuthSession
import com.woowacourse.runpamine.domain.auth.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptedAuthSessionStore(
    context: Context,
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val sessionState = MutableStateFlow(readSession())

    fun observe(): Flow<AuthSession?> = sessionState

    fun current(): AuthSession? = sessionState.value

    fun save(session: AuthSession) {
        val user = JSONObject().put("id", session.user.id).put("email", session.user.email)
        val json =
            JSONObject()
                .put("accessToken", session.accessToken)
                .put("refreshToken", session.refreshToken)
                .put("user", user)
                .toString()
        preferences.edit().putString(SESSION_KEY, encrypt(json)).apply()
        sessionState.value = session
    }

    fun clear() {
        preferences.edit().remove(SESSION_KEY).apply()
        sessionState.value = null
    }

    private fun readSession(): AuthSession? {
        val encrypted = preferences.getString(SESSION_KEY, null) ?: return null
        return runCatching {
            val data = JSONObject(decrypt(encrypted))
            val user = data.getJSONObject("user")
            AuthSession(
                accessToken = data.getString("accessToken"),
                refreshToken = data.getString("refreshToken"),
                user =
                    AuthUser(
                        id = user.getString("id"),
                        email = user.optString("email").takeIf { it.isNotBlank() },
                    ),
            )
        }.onFailure {
            preferences.edit().remove(SESSION_KEY).apply()
        }.getOrNull()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return listOf(cipher.iv, ciphertext)
            .joinToString(SEPARATOR) { Base64.encodeToString(it, Base64.NO_WRAP) }
    }

    private fun decrypt(value: String): String {
        val parts = value.split(SEPARATOR, limit = 2)
        require(parts.size == 2) { "Invalid encrypted auth session." }
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keySpecBuilder =
            KeyGenParameterSpec
                .Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
        val keySpec =
            keySpecBuilder
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            .run {
                init(
                    keySpec,
                )
                generateKey()
            }
    }
}

private const val PREFERENCES_NAME = "runpamine_auth"
private const val SESSION_KEY = "encrypted_session"
private const val KEY_ALIAS = "runpamine_auth_session_key"
private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH_BITS = 128
private const val SEPARATOR = "."
