package com.woowacourse.runpamine.presentation.running

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.presentation.component.RunpamineConfirmationDialog
import com.woowacourse.runpamine.presentation.running.components.RunningScreenContent
import com.woowacourse.runpamine.presentation.running.viewmodel.RunTrackingViewModel
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningScreen(
    modifier: Modifier = Modifier,
    onStopCompleted: () -> Unit = {},
) {
    val context = LocalContext.current
    val container = context.runpamineContainer
    val viewModel: RunTrackingViewModel =
        viewModel(
            factory =
                RunTrackingViewModel.Factory(
                    application = context.applicationContext as Application,
                    runTrackingRepository = container.runTrackingRepository,
                    runSyncRepository = container.runSyncRepository,
                ),
        )
    val state by viewModel.currentRunState.collectAsStateWithLifecycle()
    var completedSession by remember { mutableStateOf<RunSession?>(null) }
    var completedRoutePoints by remember { mutableStateOf(emptyList<RunPoint>()) }
    var showStopDialog by rememberSaveable { mutableStateOf(false) }
    var hasStoppedRun by rememberSaveable { mutableStateOf(false) }
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            if (permissions.hasLocationPermission()) {
                viewModel.startRun()
            }
        }

    LaunchedEffect(Unit) {
        if (hasStoppedRun) {
            onStopCompleted()
        } else if (context.hasLocationPermission()) {
            viewModel.startRun()
        } else {
            locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    BackHandler(enabled = state.isRunning && completedSession == null) {
        showStopDialog = true
    }

    completedSession?.let { session ->
        RunningCompleteScreen(
            distance = session.distanceText(),
            time = session.durationSeconds.elapsedTimeText(),
            pace = session.paceText(),
            calories = session.calories.toString(),
            routePoints = completedRoutePoints,
            onCompleteClick = onStopCompleted,
            modifier = modifier,
        )
    } ?: RunningScreenContent(
        session = state.session,
        elapsedSeconds = state.elapsedSeconds,
        isPaused = state.isPaused,
        modifier = modifier,
        onPauseClick = viewModel::togglePause,
        onStopClick = {
            showStopDialog = true
        },
    )

    if (showStopDialog) {
        RunpamineConfirmationDialog(
            title = stringResource(R.string.running_stop_title),
            message = stringResource(R.string.running_stop_confirmation),
            dismissText = stringResource(R.string.cancel),
            confirmText = stringResource(R.string.running_stop),
            onDismiss = { showStopDialog = false },
            onConfirm = {
                showStopDialog = false
                viewModel.stopRun { session ->
                    hasStoppedRun = true
                    completedSession = session
                    completedRoutePoints = state.routePoints
                }
            },
        )
    }
}

private val LOCATION_PERMISSIONS =
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

private fun Context.hasLocationPermission(): Boolean =
    LOCATION_PERMISSIONS.any { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

private fun Map<String, Boolean>.hasLocationPermission(): Boolean =
    LOCATION_PERMISSIONS.any { permission ->
        this[permission] == true
    }

private fun RunSession.distanceText(): String {
    val distanceKm = distanceMeters / METERS_PER_KILOMETER
    return String.format("%.2f", distanceKm)
}

private fun Long.elapsedTimeText(): String {
    val minutes = this / SECONDS_PER_MINUTE
    val seconds = this % SECONDS_PER_MINUTE
    return "%02d:%02d".format(minutes, seconds)
}

private fun RunSession.paceText(): String {
    if (averagePaceSecondsPerKm <= 0) return "0'00\""

    val minutes = averagePaceSecondsPerKm / SECONDS_PER_MINUTE
    val seconds = averagePaceSecondsPerKm % SECONDS_PER_MINUTE
    return "%d'%02d\"".format(minutes, seconds)
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun RunningScreenPreview() {
    RunpamineTheme {
        RunningScreenContent(
            session =
                RunSession(
                    id = "preview",
                    startedAt = java.time.Instant.now(),
                    distanceMeters = 5_200,
                    durationSeconds = 1_725,
                    calories = 505,
                ),
            elapsedSeconds = 1_725,
        )
    }
}

private const val METERS_PER_KILOMETER = 1_000.0
private const val SECONDS_PER_MINUTE = 60
