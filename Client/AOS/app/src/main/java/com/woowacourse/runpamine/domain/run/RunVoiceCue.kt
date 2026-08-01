package com.woowacourse.runpamine.domain.run

enum class RunVoiceCue {
    START,
    PAUSE,
    RESUME,
    STOP,
}

interface RunVoicePlayer {
    fun play(cue: RunVoiceCue)
}

object NoOpRunVoicePlayer : RunVoicePlayer {
    override fun play(cue: RunVoiceCue) = Unit
}
