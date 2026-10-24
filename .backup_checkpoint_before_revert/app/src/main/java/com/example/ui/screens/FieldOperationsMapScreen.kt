package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DensityMedium
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.IncidentRepository
import com.example.model.ImageHotlinks
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.InverseOnSurface
import com.example.ui.theme.InverseSurface
import com.example.ui.theme.OnError
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.OnPrimaryContainer
import com.example.ui.theme.OnPrimaryFixed
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SecondarySlate
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TertiaryBlue
import com.example.ui.theme.TertiaryContainer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.components.PawRescueMap
import com.example.model.MapMarkerItem
import com.example.model.MapMarkerType
import com.example.model.MapRoute
import com.example.service.DefaultLocationService
import com.example.service.DefaultRouteService
import com.example.service.DeviceLocation
import com.example.service.LocationState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FieldOperationsMapScreen(
    onNavigateBack: () -> Unit = {},
    onSosClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeIncident by IncidentRepository.activeIncident.collectAsState()
    val routeService = remember { DefaultRouteService() }
    val locationService = remember { DefaultLocationService() }

    var userLocation by remember { mutableStateOf<DeviceLocation?>(null) }
    var selectedFilterCriticalOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (locationService.hasPermission(context)) {
            val locState = locationService.getCurrentLocation(context)
            if (locState is LocationState.Success) {
                userLocation = locState.location
            }
        }
    }

    var showHeatmap by remember { mutableStateOf(false) }
    var showGeofence by remember { mutableStateOf(true) }
    var zoomLevel by remember { mutableFloatStateOf(1f) }
    var sendSmsNotification by remember { mutableStateOf(true) }

    var isResolving by remember { mutableStateOf(false) }
    var caseResolved by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val currentInc = activeIncident
    val incLat = currentInc?.latitude ?: 32.7157
    val incLon = currentInc?.longitude ?: -117.1611

    val tacticalMarkers = remember(incLat, incLon, selectedFilterCriticalOnly, currentInc) {
        val list = mutableListOf<MapMarkerItem>()
        if (currentInc != null) {
            list.add(
                MapMarkerItem(
                    id = "tactical_incident_${currentInc.id}",
                    title = "#${currentInc.id} (${currentInc.species})",
                    snippet = "Urgency: ${currentInc.urgency.label} • ${currentInc.address}",
                    latitude = incLat,
                    longitude = incLon,
                    type = MapMarkerType.INCIDENT,
                    urgency = currentInc.urgency
                )
            )
        }
        list
    }

    val tacticalRoutes = remember(incLat, incLon, showGeofence, currentInc) {
        emptyList<MapRoute>()
    }

    val infinitePulse = rememberInfiniteTransition(label = "pulse")
    val redPingScale by infinitePulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "redPing"
    )

    BackHandler {
        onNavigateBack()
    }

    if (currentInc == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No Active Incident Selected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Select an assigned mission or incident from the dashboard to view field coordinates and operational telemetry.", style = MaterialTheme.typography.bodyMedium, color = SecondarySlate, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.Button(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Return to Dashboard")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Live GPS Stream Active Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InverseSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryFixed)
                )
                Text(
                    text = "LIVE GPS STREAM ACTIVE",
                    color = PrimaryFixed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.SatelliteAlt, contentDescription = null, tint = TertiaryContainer, modifier = Modifier.size(14.dp))
                Text("Operational Map", color = InverseOnSurface, fontSize = 11.sp)
            }
        }

        // Notification toast banner
        AnimatedVisibility(visible = toastMessage != null) {
            toastMessage?.let { msg ->
                Surface(
                    color = InverseSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryFixed)
                            Text(msg, color = InverseOnSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { toastMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = InverseOnSurface)
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // SECTION 1: Tactical Map Viewport (Universal PawRescueMap)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
                    PawRescueMap(
                        modifier = Modifier.fillMaxSize(),
                        centerLatitude = incLat,
                        centerLongitude = incLon,
                        initialZoom = 14.5,
                        markers = tacticalMarkers,
                        routes = tacticalRoutes,
                        userLocation = userLocation,
                        isInteractive = true,
                        showControls = true,
                        instructionBannerText = if (showHeatmap) "🔥 Distress Heatmap Active: High Density Sector 4" else null,
                        onMarkerClick = { marker ->
                            toastMessage = "${marker.title}: ${marker.snippet ?: "Selected"}"
                        }
                    )

                    // Tactical Control Buttons Bar on Top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Severity filter button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedFilterCriticalOnly) ErrorRed else Color.White.copy(alpha = 0.95f))
                                    .clickable {
                                        selectedFilterCriticalOnly = !selectedFilterCriticalOnly
                                        toastMessage = if (selectedFilterCriticalOnly) "Filtered by Critical Trauma (1 active)" else "Showing all tactical units & facilities"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = if (selectedFilterCriticalOnly) Color.White else ErrorRed, modifier = Modifier.size(14.dp))
                                    Text("Severity", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedFilterCriticalOnly) Color.White else MaterialTheme.colorScheme.onSurface)
                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(if (selectedFilterCriticalOnly) Color.White else ErrorRed))
                                }
                            }

                            // Geofence / Route toggle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (showGeofence) PrimaryContainer else Color.White.copy(alpha = 0.95f))
                                    .clickable {
                                        showGeofence = !showGeofence
                                        toastMessage = if (showGeofence) "Tactical route corridors enabled" else "Route corridors hidden"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Polyline, contentDescription = null, tint = if (showGeofence) Color.White else PrimaryEmerald, modifier = Modifier.size(14.dp))
                                    Text("Geo-Fence", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (showGeofence) Color.White else MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            // Heatmap toggle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (showHeatmap) TertiaryBlue else Color.White.copy(alpha = 0.95f))
                                    .clickable {
                                        showHeatmap = !showHeatmap
                                        toastMessage = if (showHeatmap) "Distress heatmap layer enabled" else "Heatmap layer disabled"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.DensityMedium, contentDescription = null, tint = if (showHeatmap) Color.White else TertiaryBlue, modifier = Modifier.size(14.dp))
                                    Text("Heatmap", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (showHeatmap) Color.White else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: Bottom Tactical Review Sheet
            item {
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-12).dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Case Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryContainer))
                                Text(currentInc.id, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SecondarySlate)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(if (caseResolved) PrimaryContainer else SecondaryContainer)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (caseResolved) "CASE RESOLVED & FILED" else "STAGE: ${currentInc.currentStage.name}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (caseResolved) OnPrimaryContainer else Color(0xFF131B2E)
                                )
                            }
                        }

                        Text(
                            text = "${currentInc.species} • ${currentInc.situation}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(16.dp))
                            Text(
                                text = currentInc.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondarySlate
                            )
                        }

                        // Rescuer Field Summary Bento
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceContainerLow)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Field Operatives
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("ASSIGNED SQUAD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondarySlate)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(16.dp))
                                        Text(currentInc.assignedTeamId ?: "Unassigned Squad", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }

                                // Urgency & Triage
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("URGENCY LEVEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondarySlate)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Emergency, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                        Text(currentInc.urgency.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }

                        // Notes if present
                        if (currentInc.notes.isNotBlank()) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("FIELD NOTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondarySlate)
                                    Text(currentInc.notes, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }

                        // Administrative Controls
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // SMS Notification Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerLow)
                                    .clickable { sendSmsNotification = !sendSmsNotification }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Sms, contentDescription = null, tint = TertiaryBlue)
                                    Column {
                                        Text("Send Citizen Resolution SMS & Ping", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Text("Alerts citizen reporter of safe resolution", style = MaterialTheme.typography.bodySmall, color = SecondarySlate, fontSize = 11.sp)
                                    }
                                }
                                Checkbox(
                                    checked = sendSmsNotification,
                                    onCheckedChange = { sendSmsNotification = it },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryEmerald)
                                )
                            }

                            // Primary CTA: Approve Rescue
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (caseResolved) PrimaryEmerald else PrimaryContainer)
                                    .clickable(enabled = !isResolving) {
                                        isResolving = true
                                        coroutineScope.launch {
                                            delay(700)
                                            IncidentRepository.markActiveIncidentResolved()
                                            isResolving = false
                                            caseResolved = true
                                            toastMessage = "Case #${currentInc.id} Closed • Audit recorded & Citizen SMS queued"
                                        }
                                    }
                                    .testTag("approve_rescue_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isResolving) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Text("Committing Resolution to Ledger...", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else if (caseResolved) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                        Text("Case Successfully Resolved", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = OnPrimaryContainer)
                                        Text("Approve Rescue & Mark Case Resolved", color = OnPrimaryContainer, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Secondary CTA: Request Rescuer Clarification
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerHigh)
                                    .clickable {
                                        toastMessage = "Clarification dispatch sent to assigned response team"
                                    }
                                    .testTag("request_clarification_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(16.dp))
                                    Text("Request Rescuer Correction / Clarification", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
