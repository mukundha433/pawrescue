package com.example.service

import android.location.Location
import org.osmdroid.util.GeoPoint
import java.util.Locale
import kotlin.math.roundToInt

data class RouteCalculationResult(
    val distanceKm: Double,
    val etaMinutes: Int,
    val formattedDistance: String,
    val formattedEta: String,
    val points: List<GeoPoint>
)

interface RouteService {
    fun calculateDistanceKm(startLat: Double, startLon: Double, endLat: Double, endLon: Double): Double
    fun calculateEtaMinutes(distanceKm: Double, averageSpeedKmh: Double = 35.0): Int
    fun formatDistance(distanceKm: Double): String
    fun formatEta(etaMinutes: Int): String
    fun buildRoute(startLat: Double, startLon: Double, endLat: Double, endLon: Double, numWaypoints: Int = 8): RouteCalculationResult
}

class DefaultRouteService : RouteService {

    override fun calculateDistanceKm(startLat: Double, startLon: Double, endLat: Double, endLon: Double): Double {
        if (!startLat.isFinite() || !startLon.isFinite() || !endLat.isFinite() || !endLon.isFinite()) {
            return 0.0
        }
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLon, endLat, endLon, results)
        return (results[0] / 1000.0)
    }

    override fun calculateEtaMinutes(distanceKm: Double, averageSpeedKmh: Double): Int {
        if (distanceKm <= 0.0) return 1
        val hours = distanceKm / averageSpeedKmh
        return (hours * 60.0).roundToInt().coerceAtLeast(1)
    }

    override fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            val meters = (distanceKm * 1000).roundToInt()
            "$meters m"
        } else {
            String.format(Locale.US, "%.1f km", distanceKm)
        }
    }

    override fun formatEta(etaMinutes: Int): String {
        return when {
            etaMinutes <= 1 -> "1 min"
            etaMinutes < 60 -> "$etaMinutes mins"
            else -> {
                val hrs = etaMinutes / 60
                val mins = etaMinutes % 60
                "${hrs}h ${mins}m"
            }
        }
    }

    override fun buildRoute(startLat: Double, startLon: Double, endLat: Double, endLon: Double, numWaypoints: Int): RouteCalculationResult {
        val distance = calculateDistanceKm(startLat, startLon, endLat, endLon)
        val eta = calculateEtaMinutes(distance)

        val points = mutableListOf<GeoPoint>()
        points.add(GeoPoint(startLat, startLon))

        if (numWaypoints > 1 && distance > 0.05) {
            // Realistic city grid road interpolation (step-like L shape or intermediate street bends)
            val midFraction = 0.5
            val midLat = startLat + (endLat - startLat) * midFraction
            val midLon = startLon + (endLon - startLon) * midFraction

            // Slight dogleg simulation matching urban traffic grid
            val deltaLat = endLat - startLat
            val deltaLon = endLon - startLon
            val bendLat = startLat + deltaLat * 0.45 + (deltaLon * 0.08)
            val bendLon = startLon + deltaLon * 0.55 - (deltaLat * 0.08)

            points.add(GeoPoint(bendLat, bendLon))
            points.add(GeoPoint(midLat, midLon))
        }

        points.add(GeoPoint(endLat, endLon))

        return RouteCalculationResult(
            distanceKm = distance,
            etaMinutes = eta,
            formattedDistance = formatDistance(distance),
            formattedEta = formatEta(eta),
            points = points
        )
    }
}
