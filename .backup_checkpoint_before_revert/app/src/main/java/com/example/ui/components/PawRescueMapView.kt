package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.MapMarkerItem
import com.example.model.MapMarkerType
import com.example.service.DeviceLocation

/**
 * Backward-compatible wrapper delegating to universal PawRescueMap.
 */
@Composable
fun PawRescueMapView(
    modifier: Modifier = Modifier,
    latitude: Double?,
    longitude: Double?,
    userLocation: DeviceLocation? = null,
    isInteractive: Boolean = true,
    initialZoom: Double = 16.0,
    onLocationSelected: (Double, Double, Boolean) -> Unit = { _, _, _ -> },
    onRecenterRequested: (() -> Unit)? = null
) {
    val rescueMarker = if (latitude != null && longitude != null && latitude.isFinite() && longitude.isFinite()) {
        listOf(
            MapMarkerItem(
                id = "rescue_pin",
                title = "Rescue Location",
                snippet = "Drag pin to exact animal location",
                latitude = latitude,
                longitude = longitude,
                type = MapMarkerType.INCIDENT,
                isDraggable = isInteractive
            )
        )
    } else emptyList()

    PawRescueMap(
        modifier = modifier,
        centerLatitude = latitude,
        centerLongitude = longitude,
        initialZoom = initialZoom,
        markers = rescueMarker,
        userLocation = userLocation,
        isInteractive = isInteractive,
        isSelectableLocation = isInteractive,
        instructionBannerText = if (isInteractive) "📍 Tap map or drag red pin to set exact location" else null,
        onLocationSelected = onLocationSelected,
        onRecenter = onRecenterRequested
    )
}
