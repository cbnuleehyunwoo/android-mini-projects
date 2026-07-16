package com.woowacourse.runpamine.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import com.woowacourse.runpamine.shared.app.RunpamineController
import com.woowacourse.runpamine.shared.ui.model.RunpamineUiState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.UIKit.UIViewController

fun mainViewController(controller: RunpamineController): UIViewController =
    ComposeUIViewController {
        RunpamineApp(
            controller = controller,
            homeMapContent = { IosHomeMap(modifier = Modifier.fillMaxSize()) },
        )
    }

fun createIosRunpamineController(): RunpamineController =
    RunpamineController(
        RunpamineUiState(
            supportsAppleLogin = true,
            hasLocationPermission = true,
        ),
    )

fun mainViewController(): UIViewController = mainViewController(createIosRunpamineController())

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun IosHomeMap(modifier: Modifier = Modifier) {
    UIKitView(
        factory = {
            MKMapView().apply {
                showsUserLocation = true
                setRegion(
                    region =
                        MKCoordinateRegionMakeWithDistance(
                            centerCoordinate = CLLocationCoordinate2DMake(37.5665, 126.9780),
                            latitudinalMeters = 1_200.0,
                            longitudinalMeters = 1_200.0,
                        ),
                    animated = false,
                )
            }
        },
        modifier = modifier,
    )
}
