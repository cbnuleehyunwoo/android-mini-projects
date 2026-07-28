package com.woowacourse.runpamine.data.run.tracker

import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.woowacourse.runpamine.domain.run.LocationTracker
import com.woowacourse.runpamine.domain.run.LocationTrackingMode
import com.woowacourse.runpamine.domain.run.RunPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant

class AndroidLocationTracker(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
) : LocationTracker {
    @SuppressLint("MissingPermission")
    override fun observeLocation(mode: LocationTrackingMode): Flow<RunPoint> =
        callbackFlow {
            val request =
                LocationRequest
                    .Builder(Priority.PRIORITY_HIGH_ACCURACY, mode.updateIntervalMillis)
                    .setMinUpdateIntervalMillis(mode.updateIntervalMillis)
                    .setMinUpdateDistanceMeters(mode.minimumUpdateDistanceMeters)
                    .build()

            val callback =
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.locations.forEach { location ->
                            if (!location.hasAccuracy() || location.accuracy > MAX_HORIZONTAL_ACCURACY_METERS) {
                                return@forEach
                            }
                            trySend(
                                RunPoint(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    recordedAt = Instant.ofEpochMilli(location.time),
                                    horizontalAccuracyMeters = location.accuracy,
                                ),
                            )
                        }
                    }
                }

            try {
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    callback,
                    Looper.getMainLooper(),
                )
            } catch (exception: SecurityException) {
                close(exception)
            }

            awaitClose {
                fusedLocationProviderClient.removeLocationUpdates(callback)
            }
        }

    private companion object {
        const val MAX_HORIZONTAL_ACCURACY_METERS = 30f
    }
}
