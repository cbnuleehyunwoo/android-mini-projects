package com.woowacourse.runpamine.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.os.IBinder
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.di.runpamineContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RunTrackingService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        observeKilometerMilestones()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        startTracking(discardActiveRun = intent?.getBooleanExtra(EXTRA_DISCARD_ACTIVE_RUN, false) == true)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        runBlocking {
            runpamineContainer.runTrackingRepository.discardActiveRun()
        }
        scope.cancel()
        super.onDestroy()
    }

    private fun startTracking(discardActiveRun: Boolean) {
        startForeground(NOTIFICATION_ID, buildNotification())
        scope.launch {
            if (discardActiveRun) {
                runpamineContainer.runTrackingRepository.discardActiveRun()
            }
            runpamineContainer.runTrackingRepository.startRun()
        }
    }

    private fun observeKilometerMilestones() {
        scope.launch {
            var activeSessionId: String? = null
            var completedKilometers = 0

            runpamineContainer.runTrackingRepository.observeCurrentRun().collect { session ->
                if (session == null) {
                    activeSessionId = null
                    completedKilometers = 0
                    return@collect
                }

                if (session.id != activeSessionId) {
                    activeSessionId = session.id
                    completedKilometers = session.splits.size
                    return@collect
                }

                if (session.splits.size > completedKilometers) {
                    vibrateKilometerMilestone()
                }
                completedKilometers = session.splits.size
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateKilometerMilestone() {
        val vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(VibratorManager::class.java).defaultVibrator
            } else {
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        if (!vibrator.hasVibrator()) return

        val effect = VibrationEffect.createWaveform(KILOMETER_VIBRATION_PATTERN, -1)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                vibrator.vibrate(
                    effect,
                    VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION),
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                vibrator.vibrate(
                    effect,
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build(),
                )
            }

            else -> vibrator.vibrate(KILOMETER_VIBRATION_PATTERN, -1)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_record)
            .setContentTitle(getString(R.string.running_notification_title))
            .setContentText(getString(R.string.running_notification_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.running_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "run_tracking"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_START = "com.woowacourse.runpamine.action.START_RUN_TRACKING"
        private const val EXTRA_DISCARD_ACTIVE_RUN = "discard_active_run"
        private val KILOMETER_VIBRATION_PATTERN = longArrayOf(0, 180, 100, 180)

        fun startIntent(
            context: Context,
            discardActiveRun: Boolean = false,
        ): Intent =
            Intent(context, RunTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DISCARD_ACTIVE_RUN, discardActiveRun)
            }
    }
}
