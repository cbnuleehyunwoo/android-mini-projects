package com.woowacourse.runpamine.presentation.running

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.presentation.running.components.RunningScreenContent
import com.woowacourse.runpamine.presentation.running.viewmodel.RunTrackingViewModel
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningScreen(
    modifier: Modifier = Modifier,
    onPauseClick: () -> Unit = {},
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
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            if (permissions.hasLocationPermission()) {
                viewModel.startRun()
            }
        }

    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            viewModel.startRun()
        } else {
            locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    RunningScreenContent(
        session = state.session,
        elapsedSeconds = state.elapsedSeconds,
        modifier = modifier,
        onPauseClick = onPauseClick,
        onStopClick = {
            viewModel.stopRun(onStopped = onStopCompleted)
        },
    )
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
