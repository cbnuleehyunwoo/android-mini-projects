package com.woowacourse.runpamine.sound

interface RunAnnouncer {
    /** 재생 리소스를 미리 준비(워밍업)한다. 러닝 화면 진입 시점에 호출한다. */
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
