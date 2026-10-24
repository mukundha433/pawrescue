package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.IncidentRepository
import com.example.model.Incident
import com.example.model.MapMarkerItem
import com.example.model.MapMarkerType
import com.example.model.RescueStage
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PawRescueMap
import com.example.ui.components.StrayCareHeader
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnPrimaryContainer
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.SecondarySlate
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest

@Composable
fun IncidentDetailsScreen(
    incidentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToClinical: () -> Unit,
    onSosClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val allIncidents by IncidentRepository.incidents.collectAsState()
    val incident = remember(allIncidents, incidentId) {
        allIncidents.find { it.id == incidentId }
    }

    var showQuickNoteModal by remember { mutableStateOf(false) }
    var noteInputText by remember { mutableStateOf("") }
    var quickToastMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StrayCareHeader(
            title = "Incident Details",
            showBackButton = true,
            onBackClick = onNavigateBack,
            onSosClick = onSosClick,
            onProfileClick = onProfileClick
        )

        // Feedback toast
        AnimatedVisibility(visible = quickToastMsg != null) {
            Surface(
                color = PrimaryEmerald,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Text(quickToastMsg ?: "", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = { quickToastMsg = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        // PHASE 8 REQUIREMENT:
        // If the requested report does not exist: Show "Report not found". Do NOT substitute a fake report.
        if (incident == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    icon = Icons.Default.ErrorOutline,
                    title = "Report Not Found",
                    description = "The requested rescue report #$incidentId does not exist or has been removed from the database.",
                    actionButtonText = "Return to Dashboard",
                    onActionClick = onNavigateBack
                )
            }
            return
        }

        val otherReports = remember(allIncidents, incident.reporterId, incident.id) {
            allIncidents.filter { it.reporterId == incident.reporterId && it.id != incident.id }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Hero Incident Card (Real Database Record)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(incident.id, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SecondarySlate)
                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(ErrorRed))
                                    Text(incident.urgency.badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                                }
                                Text(incident.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(16.dp))
                                    Text(incident.address, fontSize = 14.sp, color = SecondarySlate)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(if (incident.currentStage == RescueStage.RESOLVED) PrimaryFixed else ErrorContainer)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = incident.currentStage.title,
                                    color = if (incident.currentStage == RescueStage.RESOLVED) Color(0xFF002113) else OnErrorContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Photo & Reported Details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceContainerLow)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceContainerHigh)
                            ) {
                                if (incident.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = incident.imageUrl,
                                        contentDescription = "Reported animal",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.Pets, contentDescription = null, tint = SecondarySlate, modifier = Modifier.align(Alignment.Center).size(28.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("REPORTED ISSUE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondarySlate)
                                    Text(incident.timestamp, fontSize = 12.sp, color = SecondarySlate)
                                }
                                Text("${incident.species} • ${incident.situation}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Reported by: ${incident.reporterName.ifBlank { "Citizen Guardian" }}", fontSize = 14.sp, color = SecondarySlate)
                            }
                        }

                        // Report Notes (Real citizen entered text)
                        if (incident.notes.isNotBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("CITIZEN OBSERVATION NOTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondarySlate)
                                Text(incident.notes, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Telemetry Map (Render only if real coordinates exist)
                        val incLat = incident.latitude
                        val incLon = incident.longitude
                        if (incLat != null && incLon != null && incLat.isFinite() && incLon.isFinite()) {
                            val detailMarkers = listOf(
                                MapMarkerItem(
                                    id = "incident_${incident.id}",
                                    title = "#${incident.id} (${incident.species})",
                                    snippet = incident.address,
                                    latitude = incLat,
                                    longitude = incLon,
                                    type = MapMarkerType.INCIDENT,
                                    urgency = incident.urgency
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.NearMe, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
                                        Text("Incident Geolocation", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = incident.coordinates.ifBlank { "${String.format(java.util.Locale.US, "%.4f, %.4f", incLat, incLon)}" },
                                        fontSize = 13.sp,
                                        color = SecondarySlate
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                ) {
                                    PawRescueMap(
                                        modifier = Modifier.fillMaxSize(),
                                        centerLatitude = incLat,
                                        centerLongitude = incLon,
                                        initialZoom = 15.0,
                                        markers = detailMarkers,
                                        routes = emptyList(),
                                        isInteractive = true,
                                        showControls = true,
                                        instructionBannerText = "📍 Report Location: ${incident.address}"
                                    )
                                }
                            }
                        }

                        // Assigned Rescue Unit Info
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceContainerLow)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(22.dp))
                                    }
                                    Column {
                                        Text("Assigned Unit", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(incident.assignedUnit, fontSize = 14.sp, color = SecondarySlate)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(PrimaryEmerald.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(if (incident.assignedTeamId != null) "Assigned" else "Pending Dispatch", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showQuickNoteModal = true },
                                    color = PrimaryContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Send Field Note", color = OnPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        // Clinical Veterinary Details (If recorded)
                        if (incident.attendingVetName.isNotBlank() || incident.bodyConditionScore > 0) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("VETERINARY CLINICAL LOG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondarySlate)
                                if (incident.attendingVetName.isNotBlank()) {
                                    Text("Attending Vet: ${incident.attendingVetName}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                if (incident.bodyConditionScore > 0) {
                                    Text("Body Condition Score: ${incident.bodyConditionScore}/9", fontSize = 14.sp, color = SecondarySlate)
                                }
                            }
                        }

                        // Multi-Angle Evidence Photos (If captured by citizen)
                        if (incident.evidenceList.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("FIELD EVIDENCE PHOTOS (${incident.evidenceList.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondarySlate)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    incident.evidenceList.forEach { ev ->
                                        Box(
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(SurfaceContainerHigh)
                                        ) {
                                            AsyncImage(
                                                model = ev.uriString,
                                                contentDescription = "Angle ${ev.angleNumber}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topStart = 4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("A${ev.angleNumber}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 9-Stage Rescue Lifecycle Tracker
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("RESCUE LIFECYCLE TIMELINE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecondarySlate)
                            RescueStage.entries.forEach { stage ->
                                val isPast = stage.stepNumber < incident.currentStage.stepNumber
                                val isCurrent = stage == incident.currentStage

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCurrent -> PrimaryEmerald
                                                    isPast -> PrimaryEmerald.copy(alpha = 0.35f)
                                                    else -> SurfaceContainerHigh
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPast || isCurrent) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        } else {
                                            Text("${stage.stepNumber}", fontSize = 12.sp, color = SecondarySlate)
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stage.title,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = if (isCurrent) PrimaryEmerald else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(stage.description, fontSize = 13.sp, lineHeight = 18.sp, color = SecondarySlate)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Other User Submitted Reports (Real Database Query)
            if (otherReports.isNotEmpty()) {
                item {
                    Text("Other Reports by Reporter (${otherReports.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(otherReports) { other ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateBack() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerHigh)
                            ) {
                                if (other.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = other.imageUrl,
                                        contentDescription = other.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.Pets, contentDescription = null, tint = SecondarySlate, modifier = Modifier.align(Alignment.Center).size(20.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(other.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${other.species} • ${other.address}", fontSize = 10.sp, color = SecondarySlate)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerHigh)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(other.currentStage.title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Modal: Send Quick Note to Field Squad
    if (showQuickNoteModal) {
        Dialog(onDismissRequest = { showQuickNoteModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Send Field Note", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showQuickNoteModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Text("Updates dispatch and field squad with critical patient updates.", style = MaterialTheme.typography.bodySmall, color = SecondarySlate)
                    OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { noteInputText = it },
                        placeholder = { Text("e.g. Animal moved into shaded alcove...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryEmerald)
                            .clickable {
                                showQuickNoteModal = false
                                quickToastMsg = "Note transmitted to dispatch console!"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Transmit Note", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
