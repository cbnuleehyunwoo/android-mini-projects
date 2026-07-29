package com.woowacourse.runpamine.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.woowacourse.runpamine.R
import java.util.Locale

/**
 * 온디바이스 [TextToSpeech]로 러닝 상태 전이를 한국어 음성으로 안내한다.
 * 앱 스코프 싱글턴으로 유지되어 러닝 화면을 벗어나도 재생 중인 발화가 끊기지 않는다.
 */
class TtsRunAnnouncer(
    context: Context,
) : RunAnnouncer {
    private val appContext = context.applicationContext
    private val audioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var focusRequest: AudioFocusRequest? = null

    private val speechAudioAttributes: AudioAttributes =
        AudioAttributes
            .Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

    private val utteranceListener =
        object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                abandonAudioFocus()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                abandonAudioFocus()
            }
        }

    override fun prepare() {
        if (tts != null) return
        tts =
            TextToSpeech(appContext) { status ->
                val engine = tts
                if (status == TextToSpeech.SUCCESS && engine != null) {
                    val languageResult = engine.setLanguage(Locale.KOREAN)
                    isReady =
                        languageResult != TextToSpeech.LANG_MISSING_DATA &&
                        languageResult != TextToSpeech.LANG_NOT_SUPPORTED
                    if (isReady) {
                        engine.setAudioAttributes(speechAudioAttributes)
                        engine.setOnUtteranceProgressListener(utteranceListener)
                    }
                }
            }
    }

    override fun announce(announcement: RunAnnouncement) {
        val engine = tts ?: return
        if (!isReady) return

        requestAudioFocus()
        engine.speak(
            appContext.getString(announcement.messageResId()),
            TextToSpeech.QUEUE_FLUSH,
            null,
            announcement.name,
        )
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
        abandonAudioFocus()
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request =
                AudioFocusRequest
                    .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(speechAudioAttributes)
                    .build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun RunAnnouncement.messageResId(): Int =
        when (this) {
            RunAnnouncement.STARTED -> R.string.running_announce_start
            RunAnnouncement.PAUSED -> R.string.running_announce_pause
            RunAnnouncement.RESUMED -> R.string.running_announce_resume
            RunAnnouncement.ENDED -> R.string.running_announce_end
        }
}
