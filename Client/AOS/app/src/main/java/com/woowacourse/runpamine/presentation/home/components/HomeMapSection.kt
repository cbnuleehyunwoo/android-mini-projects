package com.woowacourse.runpamine.presentation.home.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private val LOCATION_PERMISSIONS =
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

// 위치를 아직 가져오지 못했을 때 보여줄 기본 위치 (서울 시청)
private val DEFAULT_LOCATION = LatLng(37.5663, 126.9779)
private const val DEFAULT_ZOOM = 15f
private const val MY_LOCATION_ZOOM = 16f

@Composable
fun HomeMapSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(context.hasLocationPermission())
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { result ->
            hasLocationPermission = result.values.any { it }
        }

    if (hasLocationPermission) {
        LocationMap(modifier = modifier)
    } else {
        LocationPermissionRequest(
            onRequestPermission = { permissionLauncher.launch(LOCATION_PERMISSIONS) },
        )
    }
}

@Composable
private fun LocationMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM)
        }

    LaunchedEffect(Unit) {
        val location = context.awaitCurrentLocation()
        if (location != null) {
            currentLocation = location
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(location, MY_LOCATION_ZOOM)
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true),
        uiSettings =
            MapUiSettings(
                myLocationButtonEnabled = true,
                zoomControlsEnabled = false,
            ),
    ) {
        currentLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = stringResource(R.string.map_my_location),
            )
        }
    }
}

@Composable
private fun LocationPermissionRequest(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .background(Color(0xFFEAF4FF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.no_gps),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Blue40),
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = stringResource(R.string.map_permission_title),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 18.sp,
                    ),
                color = Color(0xFF111827),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.map_permission_rationale),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                    ),
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Blue40,
                        contentColor = Color.White,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.map_permission_request),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPermissionRequestPreview() {
    RunpamineTheme {
        LocationPermissionRequest(
            onRequestPermission = {},
        )
    }
}

private fun Context.hasLocationPermission(): Boolean =
    LOCATION_PERMISSIONS.any { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

@SuppressLint("MissingPermission")
private suspend fun Context.awaitCurrentLocation(): LatLng? =
    suspendCancellableCoroutine { continuation ->
        val client = LocationServices.getFusedLocationProviderClient(this)
        val cancellationSource = CancellationTokenSource()

        client
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationSource.token)
            .addOnSuccessListener { location ->
                continuation.resume(location?.let { LatLng(it.latitude, it.longitude) })
            }.addOnFailureListener {
                continuation.resume(null)
            }

        continuation.invokeOnCancellation { cancellationSource.cancel() }
    }

@Preview
@Composable
private fun HomeMapSectionPreview() {
    RunpamineTheme {
        HomeMapSection(
            modifier =
                Modifier
                    .fillMaxSize()
                    .height(300.dp),
        )
    }
}
