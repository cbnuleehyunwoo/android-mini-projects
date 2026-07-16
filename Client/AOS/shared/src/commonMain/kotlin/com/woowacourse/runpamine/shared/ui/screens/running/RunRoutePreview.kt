package com.woowacourse.runpamine.shared.ui.screens.running

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.shared.ui.model.GeoPointUi
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import kotlin.math.roundToInt

@Composable
fun RunRoutePreview(
    route: List<GeoPointUi>,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 0,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(cornerRadius.dp))
                .background(RouteColors.Background)
                .semantics {
                    contentDescription =
                        if (route.size >= 2) "러닝 경로 미리보기" else "러닝 경로 없음"
                },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val minorRoadWidth = 1.2.dp.toPx()
            val majorRoadWidth = 3.dp.toPx()

            repeat(5) { index ->
                val fraction = (index + 1f) / 6f
                drawLine(
                    color = RouteColors.MinorRoad,
                    start = Offset(size.width * fraction, 0f),
                    end = Offset(size.width * fraction, size.height),
                    strokeWidth = minorRoadWidth,
                )
                drawLine(
                    color = RouteColors.MinorRoad,
                    start = Offset(0f, size.height * fraction),
                    end = Offset(size.width, size.height * fraction),
                    strokeWidth = minorRoadWidth,
                )
            }

            drawLine(
                color = RouteColors.MajorRoad,
                start = Offset(-size.width * 0.05f, size.height * 0.78f),
                end = Offset(size.width * 1.05f, size.height * 0.18f),
                strokeWidth = majorRoadWidth,
            )
            drawLine(
                color = RouteColors.MajorRoad,
                start = Offset(size.width * 0.18f, -size.height * 0.05f),
                end = Offset(size.width * 0.84f, size.height * 1.05f),
                strokeWidth = majorRoadWidth,
            )

            if (route.size >= 2) {
                val minLatitude = route.minOf(GeoPointUi::latitude)
                val maxLatitude = route.maxOf(GeoPointUi::latitude)
                val minLongitude = route.minOf(GeoPointUi::longitude)
                val maxLongitude = route.maxOf(GeoPointUi::longitude)
                val latitudeRange = (maxLatitude - minLatitude).takeUnless { it == 0.0 } ?: 1.0
                val longitudeRange = (maxLongitude - minLongitude).takeUnless { it == 0.0 } ?: 1.0
                val padding = 22.dp.toPx().coerceAtMost(size.minDimension * 0.2f)
                val routeWidth = (size.width - padding * 2).coerceAtLeast(1f)
                val routeHeight = (size.height - padding * 2).coerceAtLeast(1f)

                fun offset(point: GeoPointUi): Offset {
                    val normalizedX = ((point.longitude - minLongitude) / longitudeRange).toFloat()
                    val normalizedY = ((point.latitude - minLatitude) / latitudeRange).toFloat()
                    return Offset(
                        x = padding + normalizedX * routeWidth,
                        y = padding + (1f - normalizedY) * routeHeight,
                    )
                }

                val path = Path()
                route.forEachIndexed { index, point ->
                    val position = offset(point)
                    if (index == 0) {
                        path.moveTo(position.x, position.y)
                    } else {
                        path.lineTo(position.x, position.y)
                    }
                }

                drawPath(
                    path = path,
                    color = Color.White,
                    style =
                        Stroke(
                            width = 7.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                )
                drawPath(
                    path = path,
                    color = RunpamineColors.Primary,
                    style =
                        Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                )

                val start = offset(route.first())
                val end = offset(route.last())
                drawCircle(Color.White, radius = 7.dp.toPx(), center = start)
                drawCircle(RouteColors.Start, radius = 5.dp.toPx(), center = start)
                drawCircle(Color.White, radius = 7.dp.toPx(), center = end)
                drawCircle(RunpamineColors.Primary, radius = 5.dp.toPx(), center = end)
            } else {
                drawCircle(
                    color = RouteColors.EmptyMarker,
                    radius = 5.dp.toPx(),
                    center = Offset(size.width / 2f, size.height / 2f),
                )
            }
        }
    }
}

internal fun formatDistanceKm(value: Double): String {
    val hundredths = (value.coerceAtLeast(0.0) * 100.0).roundToInt()
    val whole = hundredths / 100
    val fraction = (hundredths % 100).toString().padStart(2, '0')
    return "$whole.$fraction"
}

private object RouteColors {
    val Background = Color(0xFFEFF3F7)
    val MinorRoad = Color(0xFFDDE5ED)
    val MajorRoad = Color(0xFFFFFFFF)
    val Start = Color(0xFF00C27A)
    val EmptyMarker = Color(0xFFB9C5D2)
}
