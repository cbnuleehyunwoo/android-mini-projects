package com.woowacourse.runpamine.presentation.running.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.ui.theme.Blue40

private val DEFAULT_LOCATION = LatLng(37.5663, 126.9779)
private const val DEFAULT_ZOOM = 15f
private const val ROUTE_PADDING = 110
private const val SINGLE_POINT_ZOOM = 17f

@Composable
fun RunningRouteMap(
    points: List<RunPoint>,
    modifier: Modifier = Modifier,
    showMarkers: Boolean = false,
) {
    val route = remember(points) { points.sortedBy { it.sequence }.map { LatLng(it.latitude, it.longitude) } }
    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(route.lastOrNull() ?: DEFAULT_LOCATION, DEFAULT_ZOOM)
        }

    LaunchedEffect(route) {
        when (route.size) {
            0 -> Unit
            1 ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(route.first(), SINGLE_POINT_ZOOM),
                )

            else ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(route.toBounds(), ROUTE_PADDING),
                )
        }
    }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF3F4F6)),
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings =
                MapUiSettings(
                    compassEnabled = false,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                ),
        ) {
            if (route.size >= 2) {
                Polyline(
                    points = route,
                    color = Blue40,
                    width = 12f,
                )
            }
            if (showMarkers) {
                route.firstOrNull()?.let { start ->
                    Marker(
                        state = MarkerState(start),
                        title = "시작",
                    )
                }
                route.lastOrNull()?.let { end ->
                    Marker(
                        state = MarkerState(end),
                        title = "도착",
                    )
                }
            }
        }
        if (route.isEmpty()) {
            Text(
                text = "경로 정보가 없어요",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

private fun List<LatLng>.toBounds(): LatLngBounds {
    val builder = LatLngBounds.builder()
    forEach(builder::include)
    return builder.build()
}
