package com.woowacourse.runpamine.presentation.share

import com.woowacourse.runpamine.domain.run.RunPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RunShareModelsTest {
    @Test
    fun `경로 레이아웃만 루트 요소를 포함한다`() {
        assertEquals(setOf(RunShareElement.Data), RunShareLayout.Distance.activeElements())
        assertEquals(setOf(RunShareElement.Data), RunShareLayout.AllMetrics.activeElements())
        assertEquals(setOf(RunShareElement.Data, RunShareElement.Route), RunShareLayout.DistanceRoute.activeElements())
        assertEquals(setOf(RunShareElement.Data, RunShareElement.Route), RunShareLayout.AllMetricsRoute.activeElements())
    }

    @Test
    fun `iOS와 동일한 일곱 개의 공유 스티커를 제공한다`() {
        assertEquals(
            listOf(
                RunShareSticker.Date,
                RunShareSticker.Region,
                RunShareSticker.Pamin,
                RunShareSticker.Cheetah,
                RunShareSticker.Surprised,
                RunShareSticker.Hamburger,
                RunShareSticker.Handstand,
            ),
            RunShareSticker.entries,
        )
        assertEquals("pamin_sticker", RunShareSticker.Pamin.assetName)
        assertTrue(RunShareSticker.Date.supportsColorToggle)
        assertTrue(RunShareSticker.Region.supportsColorToggle)
        assertEquals(
            setOf(0.30f),
            RunShareSticker.entries
                .filter { it.assetName != null }
                .map { it.widthRatio }
                .toSet(),
        )
        assertTrue(
            RunShareSticker.entries
                .filter { it.assetName != null }
                .all { requireNotNull(it.assetBounds).run { width > 0 && height > 0 && aspectRatio > 0f } },
        )
    }

    @Test
    fun `공유 아이템 변형은 정규화된 기본값을 가진다`() {
        val transform = RunShareItemTransform()

        assertEquals(0f, transform.offsetXFraction)
        assertEquals(0f, transform.offsetYFraction)
        assertEquals(1f, transform.scale)
        assertEquals(0f, transform.rotationDegrees)
    }

    @Test
    fun `루트 좌표를 캔버스 내부 점으로 정규화한다`() {
        val polyline =
            runShareRoutePolyline(
                points =
                    listOf(
                        point(sequence = 2, latitude = 37.52, longitude = 127.02),
                        point(sequence = 1, latitude = 37.51, longitude = 127.01),
                        point(sequence = 3, latitude = 37.53, longitude = 127.03),
                    ),
                width = 300f,
                height = 200f,
                inset = 20f,
            )

        assertEquals(3, polyline.size)
        assertTrue(polyline.all { it.x in 0f..300f })
        assertTrue(polyline.all { it.y in 0f..200f })
        assertTrue(polyline.first().x < polyline.last().x)
        assertTrue(polyline.first().y < polyline.last().y)
    }

    @Test
    fun `루트 점이 둘 미만이면 빈 선을 반환한다`() {
        val polyline =
            runShareRoutePolyline(
                points = listOf(point(sequence = 1, latitude = 37.51, longitude = 127.01)),
                width = 300f,
                height = 200f,
                inset = 20f,
            )

        assertTrue(polyline.isEmpty())
    }

    private fun point(
        sequence: Int,
        latitude: Double,
        longitude: Double,
    ): RunPoint =
        RunPoint(
            sessionId = "session",
            sequence = sequence,
            latitude = latitude,
            longitude = longitude,
            recordedAt = Instant.EPOCH,
        )
}
