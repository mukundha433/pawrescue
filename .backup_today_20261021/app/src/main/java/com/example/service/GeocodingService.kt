package com.example.service

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.LruCache
import com.example.util.CrashLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GeocodingResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val area: String = "",
    val city: String = ""
)

interface GeocodingService {
    suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String
    suspend fun searchLocations(context: Context, query: String, limit: Int = 5): List<GeocodingResult>
}

class DefaultGeocodingService(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
) : GeocodingService {

    // In-memory cache for reverse geocoded coordinates (~10 meter grid resolution)
    private val reverseCache = LruCache<String, String>(100)
    // In-memory cache for search queries
    private val searchCache = LruCache<String, List<GeocodingResult>>(50)

    private fun cacheKeyForCoordinates(lat: Double, lon: Double): String {
        return String.format(Locale.US, "%.4f,%.4f", lat, lon)
    }

    override suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        if (!latitude.isFinite() || !longitude.isFinite() || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            return@withContext "Invalid Coordinates"
        }

        val cacheKey = cacheKeyForCoordinates(latitude, longitude)
        reverseCache.get(cacheKey)?.let { cached ->
            return@withContext cached
        }

        // 1. Attempt using Android's native on-device Geocoder
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val thoroughfare = addr.thoroughfare ?: addr.featureName
                    val locality = addr.locality ?: addr.subLocality ?: addr.adminArea
                    val formatted = when {
                        !thoroughfare.isNullOrBlank() && !locality.isNullOrBlank() -> "$thoroughfare, $locality"
                        !thoroughfare.isNullOrBlank() -> thoroughfare
                        !locality.isNullOrBlank() -> locality
                        else -> addr.getAddressLine(0)
                    }

                    if (!formatted.isNullOrBlank()) {
                        reverseCache.put(cacheKey, formatted)
                        return@withContext formatted
                    }
                }
            }
        } catch (t: Throwable) {
            CrashLogger.logWarning("GeocodingService", "nativeReverseGeocode", "Native geocoder fallback: ${t.localizedMessage}")
        }

        // 2. OpenStreetMap Nominatim reverse geocode fallback (free, HTTPS, respectful rate)
        try {
            val url = String.format(
                Locale.US,
                "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=%.6f&lon=%.6f&zoom=18&addressdetails=1",
                latitude,
                longitude
            )
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", MapConfiguration.USER_AGENT)
                .header("Accept-Language", Locale.getDefault().language)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val json = org.json.JSONObject(bodyString)
                        val addressObj = json.optJSONObject("address")
                        val road = addressObj?.optString("road") ?: addressObj?.optString("pedestrian") ?: ""
                        val suburb = addressObj?.optString("suburb") ?: addressObj?.optString("neighbourhood") ?: ""
                        val city = addressObj?.optString("city") ?: addressObj?.optString("town") ?: addressObj?.optString("county") ?: ""

                        val clean = when {
                            road.isNotBlank() && (suburb.isNotBlank() || city.isNotBlank()) -> {
                                val secondPart = if (suburb.isNotBlank()) suburb else city
                                "$road, $secondPart"
                            }
                            road.isNotBlank() -> road
                            json.has("display_name") -> {
                                val full = json.getString("display_name")
                                full.split(",").take(2).joinToString(",").trim()
                            }
                            else -> null
                        }

                        if (!clean.isNullOrBlank()) {
                            reverseCache.put(cacheKey, clean)
                            return@withContext clean
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            CrashLogger.logWarning("GeocodingService", "osmNominatimReverse", "Nominatim fallback: ${t.localizedMessage}")
        }

        // 3. Coordinate-based fallback
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        val fallback = String.format(Locale.US, "%.4f° %s, %.4f° %s", Math.abs(latitude), latDir, Math.abs(longitude), lonDir)
        reverseCache.put(cacheKey, fallback)
        fallback
    }

    override suspend fun searchLocations(context: Context, query: String, limit: Int): List<GeocodingResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 3) return@withContext emptyList()

        searchCache.get(trimmed.lowercase())?.let { cached ->
            return@withContext cached
        }

        val results = mutableListOf<GeocodingResult>()

        // 1. Try Android Native Geocoder
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(trimmed, limit)
                if (!addresses.isNullOrEmpty()) {
                    for (addr in addresses) {
                        val name = addr.featureName ?: addr.thoroughfare ?: addr.locality ?: trimmed
                        val city = addr.locality ?: addr.adminArea ?: ""
                        val fullLine = addr.getAddressLine(0) ?: "$name, $city"
                        results.add(
                            GeocodingResult(
                                displayName = fullLine,
                                latitude = addr.latitude,
                                longitude = addr.longitude,
                                area = addr.subLocality ?: addr.thoroughfare ?: "",
                                city = city
                            )
                        )
                    }
                }
            }
        } catch (t: Throwable) {
            CrashLogger.logWarning("GeocodingService", "nativeSearch", "Native geocoder search: ${t.localizedMessage}")
        }

        // 2. Fallback to OpenStreetMap Nominatim Search API if native returns no results
        if (results.isEmpty()) {
            try {
                val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
                val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=$limit&addressdetails=1"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", MapConfiguration.USER_AGENT)
                    .header("Accept-Language", Locale.getDefault().language)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrBlank()) {
                            val array = JSONArray(bodyString)
                            for (i in 0 until array.length()) {
                                val item = array.getJSONObject(i)
                                val lat = item.getDouble("lat")
                                val lon = item.getDouble("lon")
                                val displayName = item.getString("display_name")
                                // Clean up display name (e.g. keep first 3 parts)
                                val shortName = displayName.split(",").take(3).joinToString(",").trim()
                                results.add(
                                    GeocodingResult(
                                        displayName = shortName,
                                        latitude = lat,
                                        longitude = lon
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                CrashLogger.logWarning("GeocodingService", "nominatimSearch", "Nominatim search fallback: ${t.localizedMessage}")
            }
        }

        if (results.isNotEmpty()) {
            searchCache.put(trimmed.lowercase(), results)
        }
        results
    }
}
