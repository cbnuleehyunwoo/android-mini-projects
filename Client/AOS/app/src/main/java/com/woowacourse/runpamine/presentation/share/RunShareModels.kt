package com.woowacourse.runpamine.presentation.share

import com.woowacourse.runpamine.domain.run.RunPoint
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

data class RunShareData(
    val distance: String,
    val time: String,
    val pace: String,
    val calories: String,
    val date: String,
    val routePoints: List<RunPoint>,
)

enum class RunShareLayout(
    val title: String,
    val showsDetails: Boolean,
    val showsRoute: Boolean,
) {
    Distance(
        title = "러닝 거리",
        showsDetails = false,
        showsRoute = false,
    ),
    AllMetrics(
        title = "러닝 데이터",
        showsDetails = true,
        showsRoute = false,
    ),
    DistanceRoute(
        title = "러닝 거리 + 루트",
        showsDetails = false,
        showsRoute = true,
    ),
    AllMetricsRoute(
        title = "러닝 데이터 + 루트",
        showsDetails = true,
        showsRoute = true,
    ),
}

enum class RunShareSticker(
    val title: String,
    val assetName: String?,
    val widthRatio: Float,
    val defaultX: Float,
    val defaultY: Float,
    val assetBounds: RunShareStickerAssetBounds? = null,
    val supportsColorToggle: Boolean = false,
) {
    Date("러닝 날짜", null, 0f, 0.30f, 0.954f, supportsColorToggle = true),
    Region("러닝 위치", null, 0f, 0.76f, 0.40f, supportsColorToggle = true),
    Pamin("파민", "pamin_sticker", 0.30f, 0.84f, 0.77f, RunShareStickerAssetBounds(28, 11, 103, 126)),
    Cheetah("치타파민", "cheetah_pamin_sticker", 0.30f, 0.76f, 0.75f, RunShareStickerAssetBounds(37, 86, 423, 278)),
    Surprised("놀란 파민", "surprised_pamin_sticker", 0.30f, 0.84f, 0.73f, RunShareStickerAssetBounds(309, 83, 378, 882)),
    Hamburger("햄버거 파민", "hamburger_pamin_sticker", 0.30f, 0.78f, 0.76f, RunShareStickerAssetBounds(168, 191, 1645, 1652)),
    Handstand("물구나무 파민", "handstand_pamin_sticker", 0.30f, 0.72f, 0.73f, RunShareStickerAssetBounds(264, 70, 1784, 1888)),
    ;

    val value: String
        get() = assetName ?: ""

    companion object {
        val Cheers: RunShareSticker = Pamin
    }
}

data class RunShareStickerAssetBounds(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
) {
    val aspectRatio: Float
        get() = width.toFloat() / height
}

data class RunShareOffset(
    val x: Float,
    val y: Float,
)

data class RunShareItemTransform(
    val offsetXFraction: Float = 0f,
    val offsetYFraction: Float = 0f,
    val scale: Float = 1f,
    val rotationDegrees: Float = 0f,
)

internal fun RunShareLayout.activeElements(): Set<RunShareElement> =
    buildSet {
        add(RunShareElement.Data)
        if (showsRoute) add(RunShareElement.Route)
    }

enum class RunShareElement {
    Data,
    Route,
}

internal fun runShareRoutePolyline(
    points: List<RunPoint>,
    width: Float,
    height: Float,
    inset: Float,
): List<RunShareOffset> {
    val route = points.sortedBy { it.sequence }
    if (route.size < 2 || width <= 0f || height <= 0f) return emptyList()

    val projected =
        route.map { point ->
            val longitude = point.longitude.coerceIn(-180.0, 180.0)
            val latitude = point.latitude.coerceIn(-MAX_MERCATOR_LATITUDE, MAX_MERCATOR_LATITUDE)
            val x = ((longitude + 180.0) / 360.0).toFloat()
            val latitudeRadians = latitude * PI / 180.0
            val y = ((1.0 - ln(tan(latitudeRadians) + 1.0 / kotlin.math.cos(latitudeRadians)) / PI) / 2.0).toFloat()
            RunShareOffset(x = x, y = y)
        }

    val minX = projected.minOf { it.x }
    val maxX = projected.maxOf { it.x }
    val minY = projected.minOf { it.y }
    val maxY = projected.maxOf { it.y }
    val routeWidth = max(maxX - minX, MIN_ROUTE_SPAN)
    val routeHeight = max(maxY - minY, MIN_ROUTE_SPAN)
    val drawableWidth = max(width - inset * 2f, 1f)
    val drawableHeight = max(height - inset * 2f, 1f)
    val scale = min(drawableWidth / routeWidth, drawableHeight / routeHeight)
    val fittedWidth = routeWidth * scale
    val fittedHeight = routeHeight * scale
    val originX = (width - fittedWidth) / 2f
    val originY = (height - fittedHeight) / 2f

    return projected.map { point ->
        RunShareOffset(
            x = originX + (point.x - minX) * scale,
            y = originY + (maxY - point.y) * scale,
        )
    }
}

private const val MAX_MERCATOR_LATITUDE = 85.05112878
private const val MIN_ROUTE_SPAN = 0.000001f
