package com.woowacourse.runpamine.di

import android.content.Context
import com.woowacourse.runpamine.RunpamineApplication

val Context.runpamineContainer: RunpamineContainer
    get() = (applicationContext as RunpamineApplication).container
