package com.example.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.util.CrashLogger
import java.util.Locale

data class FormattedLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val displayCoordinates: String,
    val accuracyLabel: String
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
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
            CrashLogger.logError("LocationHelper", "hasLocationPermission", t)
            false
        }
    }

    fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null
        val locationManager = try {
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        } catch (t: Throwable) {
            CrashLogger.logError("LocationHelper", "getSystemService", t)
            null
        } ?: return null

        return try {
            var bestLocation: Location? = null
            val providers = locationManager.getProviders(true) ?: emptyList()
            for (provider in providers) {
                if (provider.isNullOrBlank()) continue
                val loc = try {
                    locationManager.getLastKnownLocation(provider)
                } catch (_: SecurityException) {
                    null
                } catch (t: Throwable) {
                    CrashLogger.logError("LocationHelper", "getLastKnownLocation($provider)", t)
                    null
                } ?: continue

                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }
            bestLocation
        } catch (t: Throwable) {
            CrashLogger.logError("LocationHelper", "getCurrentLocation", t)
            null
        }
    }

    fun formatLocation(location: Location): FormattedLocation {
        val lat = if (location.latitude.isFinite()) location.latitude else 0.0
        val lon = if (location.longitude.isFinite()) location.longitude else 0.0
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lon >= 0) "E" else "W"
        val latAbs = Math.abs(lat)
        val lonAbs = Math.abs(lon)

        val coords = String.format(Locale.US, "%.4f° %s, %.4f° %s", latAbs, latDir, lonAbs, lonDir)
        val acc = String.format(Locale.US, "±%.0fm Precision", location.accuracy.coerceAtLeast(1f))

        return FormattedLocation(
            latitude = lat,
            longitude = lon,
            accuracyMeters = location.accuracy,
            displayCoordinates = coords,
            accuracyLabel = acc
        )
    }

    fun getAddressFromCoordinates(context: Context, latitude: Double, longitude: Double): String {
        if (!latitude.isFinite() || !longitude.isFinite() || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            return "Rescue Location (Pending Coordinates)"
        }

        return try {
            if (!Geocoder.isPresent()) {
                return String.format(Locale.US, "%.4f° N, %.4f° W (Municipal Sector)", Math.abs(latitude), Math.abs(longitude))
            }
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val thoroughfare = addr.thoroughfare ?: addr.featureName
                val locality = addr.locality ?: addr.subLocality ?: addr.adminArea
                if (!thoroughfare.isNullOrBlank() && !locality.isNullOrBlank()) {
                    "$thoroughfare, $locality"
                } else {
                    addr.getAddressLine(0) ?: String.format(Locale.US, "%.4f° N, %.4f° W", Math.abs(latitude), Math.abs(longitude))
                }
            } else {
                String.format(Locale.US, "%.4f° N, %.4f° W (Municipal Zone)", Math.abs(latitude), Math.abs(longitude))
            }
        } catch (t: Throwable) {
            CrashLogger.logWarning("LocationHelper", "getAddressFromCoordinates", "Geocoder lookup failed: ${t.localizedMessage}")
            String.format(Locale.US, "%.4f° N, %.4f° W (GPS Location)", Math.abs(latitude), Math.abs(longitude))
        }
    }
}
