package com.woowacourse.runpamine.presentation.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.createBitmap
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import java.time.Instant

class RunShareVisualTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK),
        )
        setContent {
            RunpamineTheme(darkTheme = true) {
                RunShareEditorScreen(
                    photo = visualTestPhoto(),
                    data = visualTestData,
                    onBack = ::finish,
                    onSaved = ::finish,
                )
            }
        }
    }
}

private fun visualTestPhoto(): Bitmap {
    val bitmap = createBitmap(1080, 1938)
    Canvas(bitmap).drawRect(
        0f,
        0f,
        bitmap.width.toFloat(),
        bitmap.height.toFloat(),
        Paint().apply {
            shader =
                LinearGradient(
                    0f,
                    0f,
                    bitmap.width.toFloat(),
                    bitmap.height.toFloat(),
                    intArrayOf(Color.rgb(23, 55, 66), Color.rgb(102, 137, 115), Color.rgb(22, 31, 29)),
                    null,
                    Shader.TileMode.CLAMP,
                )
        },
    )
    return bitmap
}

private val visualTestData =
    RunShareData(
        distance = "5.20",
        time = "32:05",
        pace = "6'10\"",
        calories = "302",
        date = "2026.08.03",
        routePoints =
            listOf(
                RunPoint(sequence = 0, latitude = 37.5665, longitude = 126.9780, recordedAt = Instant.EPOCH),
                RunPoint(sequence = 1, latitude = 37.5680, longitude = 126.9810, recordedAt = Instant.EPOCH),
                RunPoint(sequence = 2, latitude = 37.5650, longitude = 126.9840, recordedAt = Instant.EPOCH),
            ),
    )
