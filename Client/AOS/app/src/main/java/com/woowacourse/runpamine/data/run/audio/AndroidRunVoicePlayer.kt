package com.woowacourse.runpamine.data.run.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.domain.run.RunVoiceCue
import com.woowacourse.runpamine.domain.run.RunVoicePlayer

class AndroidRunVoicePlayer(
    context: Context,
) : RunVoicePlayer {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val audioAttributes =
        AudioAttributes
            .Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
    private val audioFocusRequest =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            null
        }

    private var currentPlayer: MediaPlayer? = null

    @Synchronized
    override fun play(cue: RunVoiceCue) {
        releaseCurrentPlayer()

        try {
            requestAudioFocus()
            currentPlayer =
                MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    appContext.resources.openRawResourceFd(cue.rawResourceId).use { descriptor ->
                        setDataSource(
                            descriptor.fileDescriptor,
                            descriptor.startOffset,
                            descriptor.length,
                        )
                    }
                    setOnCompletionListener { player ->
                        player.release()
                        synchronized(this@AndroidRunVoicePlayer) {
                            if (currentPlayer === player) {
                                currentPlayer = null
                                abandonAudioFocus()
                            }
                        }
                    }
                    setOnErrorListener { player, _, _ ->
                        player.release()
                        synchronized(this@AndroidRunVoicePlayer) {
                            if (currentPlayer === player) {
                                currentPlayer = null
                                abandonAudioFocus()
                            }
                        }
                        true
                    }
                    prepare()
                    start()
                }
        } catch (_: Exception) {
            releaseCurrentPlayer()
        }
    }

    @Synchronized
    private fun releaseCurrentPlayer() {
        currentPlayer?.release()
        currentPlayer = null
        abandonAudioFocus()
    }

    private fun requestAudioFocus() {
        val request = audioFocusRequest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && request != null) {
            audioManager.requestAudioFocus(request)
        }
    }

    private fun abandonAudioFocus() {
        val request = audioFocusRequest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && request != null) {
            audioManager.abandonAudioFocusRequest(request)
        }
    }
}

private val RunVoiceCue.rawResourceId: Int
    get() =
        when (this) {
            RunVoiceCue.START -> R.raw.running_start
            RunVoiceCue.PAUSE -> R.raw.running_pause
            RunVoiceCue.RESUME -> R.raw.running_resume
            RunVoiceCue.STOP -> R.raw.running_stop
        }
