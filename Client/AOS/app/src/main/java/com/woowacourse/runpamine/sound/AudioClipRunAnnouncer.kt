package com.woowacourse.runpamine.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import com.woowacourse.runpamine.R

/**
 * 앱에 번들된 사전 녹음 오디오 클립([MediaPlayer])으로 러닝 상태 전이를 안내한다.
 * 시스템 TTS와 달리 모든 기기에서 동일한 목소리로 재생된다.
 */
class AudioClipRunAnnouncer(
    context: Context,
) : RunAnnouncer {
    private val appContext = context.applicationContext
    private val audioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val speechAudioAttributes: AudioAttributes =
        AudioAttributes
            .Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

    private var focusRequest: AudioFocusRequest? = null
    private var player: MediaPlayer? = null

    override fun prepare() = Unit

    override fun announce(announcement: RunAnnouncement) {
        releasePlayer()
        requestAudioFocus()

        val newPlayer =
            MediaPlayer.create(
                appContext,
                announcement.rawResId(),
                speechAudioAttributes,
                audioManager.generateAudioSessionId(),
            )
        if (newPlayer == null) {
            abandonAudioFocus()
            return
        }

        newPlayer.setOnCompletionListener { finished ->
            finished.release()
            if (player === finished) player = null
            abandonAudioFocus()
        }
        player = newPlayer
        newPlayer.start()
    }

    override fun shutdown() {
        releasePlayer()
        abandonAudioFocus()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
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

    private fun RunAnnouncement.rawResId(): Int =
        when (this) {
            RunAnnouncement.STARTED -> R.raw.run_start
            RunAnnouncement.PAUSED -> R.raw.run_pause
            RunAnnouncement.RESUMED -> R.raw.run_resume
            RunAnnouncement.ENDED -> R.raw.run_stop
        }
}
