package com.woowacourse.runpamine.sound

interface RunAnnouncer {
    /** TTS 엔진을 미리 초기화(워밍업)한다. 러닝 화면 진입 시점에 호출한다. */
    fun prepare()

    fun announce(announcement: RunAnnouncement)

    fun shutdown()
}

enum class RunAnnouncement {
    STARTED,
    PAUSED,
    RESUMED,
    ENDED,
}
