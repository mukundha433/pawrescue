package com.example.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.util.CrashLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long = System.currentTimeMillis(),
    val provider: String = "gps"
) {
    val accuracyTier: LocationAccuracyTier
        get() = when {
            accuracyMeters <= 15.0f -> LocationAccuracyTier.VERIFIED
            accuracyMeters <= 40.0f -> LocationAccuracyTier.MODERATE
            else -> LocationAccuracyTier.LOW
        }
}

enum class LocationAccuracyTier(val badgeLabel: String, val advisory: String?) {
    VERIFIED("GPS VERIFIED", null),
    MODERATE("GPS ACCURACY MODERATE", "Adequate accuracy for street triage"),
    LOW("GPS ACCURACY LOW", "Move closer to an open area for better accuracy")
}

sealed class LocationState {
    object Idle : LocationState()
    object Locating : LocationState()
    data class Success(val location: DeviceLocation) : LocationState()
    data class Error(val message: String, val canRetry: Boolean = true) : LocationState()
    object PermissionRequired : LocationState()
    object GpsDisabled : LocationState()
}

interface LocationService {
    fun hasPermission(context: Context): Boolean
    fun isGpsEnabled(context: Context): Boolean
    suspend fun getCurrentLocation(context: Context): LocationState
    fun startLocationUpdates(context: Context, minTimeMs: Long = 5000L, minDistanceM: Float = 5f, onLocationChanged: (DeviceLocation) -> Unit)
    fun stopLocationUpdates()
}

class DefaultLocationService : LocationService {

    private var activeLocationManager: LocationManager? = null
    private var activeListener: LocationListener? = null

    override fun hasPermission(context: Context): Boolean {
        return try {
            val fine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            fine || coarse
        } catch (t: Throwable) {
            CrashLogger.logError("LocationService", "hasPermission", t)
            false
        }
    }

    override fun isGpsEnabled(context: Context): Boolean {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (t: Throwable) {
            CrashLogger.logError("LocationService", "isGpsEnabled", t)
            false
        }
    }

    override suspend fun getCurrentLocation(context: Context): LocationState = withContext(Dispatchers.Main) {
        if (!hasPermission(context)) {
            return@withContext LocationState.PermissionRequired
        }

        val locationManager = try {
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        } catch (t: Throwable) {
            CrashLogger.logError("LocationService", "getLocationManager", t)
            null
        } ?: return@withContext LocationState.Error("Location services are unavailable on this device.")

        if (!isGpsEnabled(context)) {
            return@withContext LocationState.GpsDisabled
        }

        // 1. First check best recent cached location (< 60 seconds old)
        try {
            var bestRecent: Location? = null
            val providers = locationManager.getProviders(true) ?: emptyList()
            val now = System.currentTimeMillis()
            for (p in providers) {
                if (p.isNullOrBlank()) continue
                val loc = try {
                    locationManager.getLastKnownLocation(p)
                } catch (_: SecurityException) {
                    null
                } catch (t: Throwable) {
                    null
                } ?: continue

                // Check age
                if (now - loc.time < 60_000L) {
                    if (bestRecent == null || loc.accuracy < bestRecent.accuracy) {
                        bestRecent = loc
                    }
                }
            }

            if (bestRecent != null && bestRecent.latitude.isFinite() && bestRecent.longitude.isFinite()) {
                return@withContext LocationState.Success(
                    DeviceLocation(
                        latitude = bestRecent.latitude,
                        longitude = bestRecent.longitude,
                        accuracyMeters = bestRecent.accuracy,
                        timestampMillis = bestRecent.time,
                        provider = bestRecent.provider ?: "cached"
                    )
                )
            }
        } catch (t: Throwable) {
            CrashLogger.logWarning("LocationService", "lastKnownCheck", "Cached check failed: ${t.localizedMessage}")
        }

        // 2. Request a fresh single-shot location update with a 6-second timeout
        val freshLocation = withTimeoutOrNull(6000L) {
            suspendCancellableCoroutine<Location?> { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                continuation.invokeOnCancellation {
                    try {
                        locationManager.removeUpdates(listener)
                    } catch (_: Throwable) {}
                }

                try {
                    val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                    if (hasGps) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0L,
                            0f,
                            listener,
                            Looper.getMainLooper()
                        )
                    } else if (hasNetwork) {
                        locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0L,
                            0f,
                            listener,
                            Looper.getMainLooper()
                        )
                    } else {
                        continuation.resume(null)
                    }
                } catch (t: Throwable) {
                    CrashLogger.logError("LocationService", "requestLocationUpdates", t)
                    continuation.resume(null)
                }
            }
        }

        if (freshLocation != null && freshLocation.latitude.isFinite() && freshLocation.longitude.isFinite()) {
            return@withContext LocationState.Success(
                DeviceLocation(
                    latitude = freshLocation.latitude,
                    longitude = freshLocation.longitude,
                    accuracyMeters = freshLocation.accuracy,
                    timestampMillis = freshLocation.time,
                    provider = freshLocation.provider ?: "gps"
                )
            )
        }

        // 3. Fallback to any last known location regardless of age
        try {
            var fallbackLoc: Location? = null
            val providers = locationManager.getProviders(true) ?: emptyList()
            for (p in providers) {
                val loc = try {
                    locationManager.getLastKnownLocation(p)
                } catch (_: Throwable) {
                    null
                } ?: continue

                if (fallbackLoc == null || loc.accuracy < fallbackLoc.accuracy) {
                    fallbackLoc = loc
                }
            }

            if (fallbackLoc != null && fallbackLoc.latitude.isFinite() && fallbackLoc.longitude.isFinite()) {
                return@withContext LocationState.Success(
                    DeviceLocation(
                        latitude = fallbackLoc.latitude,
                        longitude = fallbackLoc.longitude,
                        accuracyMeters = fallbackLoc.accuracy,
                        timestampMillis = fallbackLoc.time,
                        provider = fallbackLoc.provider ?: "last_known"
                    )
                )
            }
        } catch (_: Throwable) {}

        LocationState.Error("Unable to acquire current GPS location. Please select the location directly on the map.")
    }

    override fun startLocationUpdates(
        context: Context,
        minTimeMs: Long,
        minDistanceM: Float,
        onLocationChanged: (DeviceLocation) -> Unit
    ) {
        stopLocationUpdates()
        if (!hasPermission(context)) return

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        activeLocationManager = lm

        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                if (loc.latitude.isFinite() && loc.longitude.isFinite()) {
                    onLocationChanged(
                        DeviceLocation(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracyMeters = loc.accuracy,
                            timestampMillis = loc.time,
                            provider = loc.provider ?: "gps_stream"
                        )
                    )
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        activeListener = listener

        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM, listener, Looper.getMainLooper())
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeMs, minDistanceM, listener, Looper.getMainLooper())
            }
        } catch (t: Throwable) {
            CrashLogger.logError("LocationService", "startLocationUpdates", t)
        }
    }

    override fun stopLocationUpdates() {
        try {
            val lm = activeLocationManager
            val listener = activeListener
            if (lm != null && listener != null) {
                lm.removeUpdates(listener)
            }
        } catch (_: Throwable) {
        } finally {
            activeLocationManager = null
            activeListener = null
        }
    }
}
