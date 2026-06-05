package com.woowacourse.runpamine

import android.app.Application
import com.woowacourse.runpamine.di.RunpamineContainer

class RunpamineApplication : Application() {
    lateinit var container: RunpamineContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = RunpamineContainer(this)
    }
}
