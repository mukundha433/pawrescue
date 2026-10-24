package com.example.ui.components

import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.R
import com.example.model.MapMarkerItem
import com.example.model.MapMarkerType
import com.example.model.MapRoute
import com.example.model.UrgencyLevel
import com.example.service.DeviceLocation
import com.example.service.MapConfiguration
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.SecondarySlate
import com.example.util.CrashLogger
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Universal, Reusable Interactive Map for PawRescue / StrayCare.
 * Used across Citizen Portal, Rescue Team Portal, and Organization Admin Portal.
 * Uses 100% free OpenStreetMap standard HTTPS tiles with zero paid API keys.
 */
@Composable
fun PawRescueMap(
    modifier: Modifier = Modifier,
    centerLatitude: Double? = null,
    centerLongitude: Double? = null,
    initialZoom: Double = 15.0,
    markers: List<MapMarkerItem> = emptyList(),
    routes: List<MapRoute> = emptyList(),
    userLocation: DeviceLocation? = null,
    isInteractive: Boolean = true,
    showControls: Boolean = true,
    showAttribution: Boolean = true,
    showUserLocation: Boolean = true,
    isSelectableLocation: Boolean = false,
    instructionBannerText: String? = null,
    onLocationSelected: ((Double, Double, Boolean) -> Unit)? = null,
    onMarkerClick: ((MapMarkerItem) -> Unit)? = null,
    onRecenter: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize OSM Configuration with proper User-Agent & HTTPS cache
    remember {
        MapConfiguration.initialize(context)
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var currentZoom by remember { mutableDoubleStateOf(initialZoom) }
    var isMapReady by remember { mutableStateOf(false) }
    var hasMapError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val latestOnLocationSelected by rememberUpdatedState(onLocationSelected)
    val latestOnMarkerClick by rememberUpdatedState(onMarkerClick)

    // Manage MapView Android lifecycle (resume, pause, destroy)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onDetach()
        }
    }

    // Camera animation when center coordinates change
    LaunchedEffect(centerLatitude, centerLongitude, isMapReady) {
        val mv = mapViewRef
        if (mv != null && isMapReady && centerLatitude != null && centerLongitude != null && centerLatitude.isFinite() && centerLongitude.isFinite()) {
            val target = GeoPoint(centerLatitude, centerLongitude)
            mv.controller.animateTo(target, currentZoom, 500L)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE2E8F0))
            .testTag("pawrescue_map_container")
    ) {
        if (!hasMapError) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("interactive_osm_map_view"),
                factory = { ctx ->
                    MapView(ctx).apply {
                        try {
                            setTileSource(MapConfiguration.getDefaultTileSource())
                            setMultiTouchControls(isInteractive)
                            isTilesScaledToDpi = true
                            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                            minZoomLevel = 3.0
                            maxZoomLevel = 20.0

                            val startLat = if (centerLatitude != null && centerLatitude.isFinite()) centerLatitude else 32.7157
                            val startLon = if (centerLongitude != null && centerLongitude.isFinite()) centerLongitude else -117.1611
                            val startPoint = GeoPoint(startLat, startLon)

                            controller.setZoom(initialZoom)
                            controller.setCenter(startPoint)

                            // 1. Tap-to-set Location Event Overlay
                            if (isInteractive && (isSelectableLocation || latestOnLocationSelected != null)) {
                                val tapOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                        if (p.latitude.isFinite() && p.longitude.isFinite()) {
                                            latestOnLocationSelected?.invoke(p.latitude, p.longitude, false)
                                            controller.animateTo(p)
                                            return true
                                        }
                                        return false
                                    }

                                    override fun longPressHelper(p: GeoPoint): Boolean = false
                                })
                                overlays.add(tapOverlay)
                            }

                            mapViewRef = this
                            isMapReady = true
                        } catch (t: Throwable) {
                            CrashLogger.logError("PawRescueMap", "initMapView", t)
                            hasMapError = true
                            errorMessage = "Map initialization error: ${t.localizedMessage}"
                        }
                    }
                },
                update = { mv ->
                    try {
                        // 1. Remove existing markers and polylines (retain MapEventsOverlay if present)
                        val overlaysToRetain = mv.overlays.filterIsInstance<MapEventsOverlay>()
                        mv.overlays.clear()
                        mv.overlays.addAll(overlaysToRetain)

                        // 2. Render Real Routes / Polylines
                        for (route in routes) {
                            if (route.points.size >= 2) {
                                val polyline = Polyline(mv).apply {
                                    id = route.id
                                    setPoints(route.points)
                                    outlinePaint.color = route.color.toArgb()
                                    outlinePaint.strokeWidth = route.strokeWidth * context.resources.displayMetrics.density
                                    outlinePaint.strokeCap = Paint.Cap.ROUND
                                    outlinePaint.strokeJoin = Paint.Join.ROUND
                                    if (route.title != null) {
                                        title = route.title
                                    }
                                }
                                mv.overlays.add(polyline)
                            }
                        }

                        // 3. Render Markers
                        for (item in markers) {
                            if (!item.latitude.isFinite() || !item.longitude.isFinite()) continue

                            val marker = Marker(mv).apply {
                                id = item.id
                                position = GeoPoint(item.latitude, item.longitude)
                                title = item.title
                                if (item.snippet != null) {
                                    snippet = item.snippet
                                }

                                val iconDrawable: Drawable? = when (item.type) {
                                    MapMarkerType.RESCUE_TEAM -> ContextCompat.getDrawable(context, R.drawable.ic_map_rescue_team)
                                    MapMarkerType.HOSPITAL -> ContextCompat.getDrawable(context, R.drawable.ic_map_hospital)
                                    MapMarkerType.USER_LOCATION -> ContextCompat.getDrawable(context, R.drawable.ic_map_user_location)
                                    MapMarkerType.INCIDENT -> {
                                        ContextCompat.getDrawable(context, R.drawable.ic_map_rescue_pin)
                                    }
                                }

                                if (iconDrawable != null) {
                                    icon = iconDrawable
                                }

                                when (item.type) {
                                    MapMarkerType.USER_LOCATION -> setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                    else -> setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }

                                isDraggable = isInteractive && item.isDraggable
                                if (isDraggable) {
                                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                        override fun onMarkerDrag(m: Marker) {}
                                        override fun onMarkerDragStart(m: Marker) {}
                                        override fun onMarkerDragEnd(m: Marker) {
                                            val pos = m.position
                                            if (pos != null && pos.latitude.isFinite() && pos.longitude.isFinite()) {
                                                latestOnLocationSelected?.invoke(pos.latitude, pos.longitude, true)
                                            }
                                        }
                                    })
                                }

                                setOnMarkerClickListener { m, _ ->
                                    latestOnMarkerClick?.invoke(item)
                                    m.showInfoWindow()
                                    true
                                }
                            }
                            mv.overlays.add(marker)
                        }

                        // 4. Render User GPS Current Location Overlay
                        if (showUserLocation && userLocation != null && userLocation.latitude.isFinite() && userLocation.longitude.isFinite()) {
                            val userGeo = GeoPoint(userLocation.latitude, userLocation.longitude)
                            val userMarker = Marker(mv).apply {
                                id = "user_gps_location_dot"
                                position = userGeo
                                title = "Your Location"
                                snippet = "Accuracy ±${userLocation.accuracyMeters.toInt()}m"
                                val userDrawable = ContextCompat.getDrawable(context, R.drawable.ic_map_user_location)
                                if (userDrawable != null) {
                                    icon = userDrawable
                                }
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                isDraggable = false
                            }
                            mv.overlays.add(userMarker)
                        }

                        mv.invalidate()
                    } catch (t: Throwable) {
                        CrashLogger.logWarning("PawRescueMap", "updateMapView", t.localizedMessage ?: "Update error")
                    }
                }
            )
        } else {
            // Error / Fallback State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Map rendering temporarily unavailable", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(errorMessage ?: "Check network connection", fontSize = 11.sp, color = SecondarySlate)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        hasMapError = false
                        errorMessage = null
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Retry Map Loading", fontSize = 11.sp)
                }
            }
        }

        // Loading overlay while initial tiles establish
        if (!isMapReady && !hasMapError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD9F1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = PrimaryEmerald,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                    Text("Loading OpenStreetMap...", fontSize = 11.sp, color = SecondarySlate, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Mandatory OpenStreetMap Visible Attribution Badge
        if (showAttribution) {
            Surface(
                color = Color(0xCC0F172A),
                shape = RoundedCornerShape(topStart = 8.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .testTag("osm_attribution_badge")
            ) {
                Text(
                    text = MapConfiguration.OSM_ATTRIBUTION,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Instruction Banner (Bottom Left)
        if (!instructionBannerText.isNullOrBlank() && isMapReady) {
            Surface(
                color = Color(0xDD0F172A),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = instructionBannerText,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }

        // Map Controls (Top Right: Zoom +, Zoom -, Re-center)
        if (isInteractive && showControls && isMapReady) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Zoom In Button (+)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .shadow(3.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            val mv = mapViewRef ?: return@clickable
                            currentZoom = (mv.zoomLevelDouble + 1.0).coerceAtMost(20.0)
                            mv.controller.zoomTo(currentZoom, 300L)
                        }
                        .testTag("map_control_zoom_in"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Zoom Out Button (-)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .shadow(3.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            val mv = mapViewRef ?: return@clickable
                            currentZoom = (mv.zoomLevelDouble - 1.0).coerceAtLeast(3.0)
                            mv.controller.zoomTo(currentZoom, 300L)
                        }
                        .testTag("map_control_zoom_out"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Re-center on Marker / Current Location
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .shadow(3.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            val mv = mapViewRef ?: return@clickable
                            if (centerLatitude != null && centerLongitude != null && centerLatitude.isFinite() && centerLongitude.isFinite()) {
                                mv.controller.animateTo(GeoPoint(centerLatitude, centerLongitude), 16.0, 400L)
                            } else if (userLocation != null) {
                                mv.controller.animateTo(GeoPoint(userLocation.latitude, userLocation.longitude), 16.0, 400L)
                            }
                            onRecenter?.invoke()
                        }
                        .testTag("map_control_recenter"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Center on Location",
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
