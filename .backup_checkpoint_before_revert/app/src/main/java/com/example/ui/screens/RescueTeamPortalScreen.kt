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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import com.example.model.EquipmentItem
import com.example.model.EquipmentStatus
import com.example.model.EquipmentInspectionRecord
import com.example.model.VehicleEquipmentRecord
import com.example.ui.components.CompactSignOutButton
import com.example.ui.components.SettingsActionRow
import com.example.ui.components.SettingsSectionCard
import com.example.ui.components.SettingsSwitchRow
import com.example.ui.components.StandardConfirmationDialog
import coil.compose.AsyncImage
import com.example.data.IncidentRepository
import com.example.model.Incident
import com.example.model.MissionStatus
import com.example.model.RescueReport
import com.example.model.RescueStage
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.components.AppCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StrayCareHeader
import com.example.ui.components.PawRescueMap
import com.example.model.MapMarkerItem
import com.example.model.MapMarkerType
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnPrimaryContainer
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

enum class RescueTeamTab(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.LocalShipping, "team_tab_dashboard"),
    ASSIGNED_MISSIONS("Missions", Icons.Default.Assignment, "team_tab_missions"),
    MAP_NAV("Map & Nav", Icons.Default.Explore, "team_tab_map"),
    REPORT_SUBMIT("Rescue Report", Icons.Default.NoteAdd, "team_tab_report"),
    PROFILE("Profile", Icons.Default.Person, "team_tab_profile")
}

@Composable
fun RescueTeamPortalScreen(
    session: UserSession,
    onLogout: () -> Unit,
    onSosClick: () -> Unit,
    onViewIncidentDetails: (String) -> Unit
) {
    // STRICT ROLE & STATUS GUARD: Access granted ONLY to approved, active RESCUE_TEAM members
    if (session.role != UserRole.RESCUE_TEAM || session.status != com.example.model.AccountStatus.ACTIVE) {
        val denialMessage = when (session.status) {
            com.example.model.AccountStatus.PENDING_APPROVAL ->
                "Your account is awaiting Organization Admin approval. You cannot log in until your registration is approved."
            com.example.model.AccountStatus.REJECTED ->
                "Your Rescue Team registration request has been rejected. Please contact the organization for further information."
            else ->
                "This area is restricted to approved Rescue Team personnel. Your account role is '${session.role.title}' and status is '${session.status.name}'."
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.Emergency, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(56.dp))
                Text("Access Denied", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    denialMessage,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)) {
                    Text("Return to Portal Selection")
                }
            }
        }
        return
    }

    var selectedTab by remember { mutableStateOf(RescueTeamTab.DASHBOARD) }
    var selectedMissionForReport by remember { mutableStateOf<Incident?>(null) }
    var showStatusUpdateDialogForId by remember { mutableStateOf<String?>(null) }

    val allIncidents by IncidentRepository.incidents.collectAsState()
    val isUnitOnDuty by IncidentRepository.isUnitOnDuty.collectAsState()

    // FILTER: Only missions assigned to THIS specific team ID
    val teamId = session.teamId
    val assignedMissions = remember(allIncidents, teamId) {
        if (teamId.isNullOrBlank()) emptyList()
        else allIncidents.filter { it.assignedTeamId == teamId }
    }

    val activeMission = assignedMissions.firstOrNull { it.missionStatus != MissionStatus.COMPLETED }
    val completedMissions = assignedMissions.filter { it.missionStatus == MissionStatus.COMPLETED }

    Scaffold(
        topBar = {
            StrayCareHeader(
                title = "PawRescue",
                subtitle = when (selectedTab) {
                    RescueTeamTab.DASHBOARD -> "${session.teamName ?: "Rescue Squad"} • Field Ops"
                    RescueTeamTab.ASSIGNED_MISSIONS -> "Assigned Missions (${assignedMissions.size})"
                    RescueTeamTab.MAP_NAV -> "Tactical GPS & Route Navigation"
                    RescueTeamTab.REPORT_SUBMIT -> "Post-Rescue Outcome Report"
                    RescueTeamTab.PROFILE -> "Squad Credentials & Status"
                },
                showBackButton = false,
                onSosClick = onSosClick,
                onProfileClick = { selectedTab = RescueTeamTab.PROFILE },
                onLogoutClick = onLogout
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceContainerLowest,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().testTag("rescue_bottom_nav")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(60.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RescueTeamTab.entries.forEach { tab ->
                        val isSelected = tab == selectedTab
                        val tint = if (isSelected) Color(0xFF0F766E) else SecondarySlate
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTab = tab }
                                .testTag(tab.tag)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(28.dp)
                                    .then(
                                        if (isSelected) {
                                            Modifier.background(
                                                color = Color(0xFF0F766E).copy(alpha = 0.14f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        } else Modifier
                                    )
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = tint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = tint,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                RescueTeamTab.DASHBOARD -> {
                    RescueDashboardContent(
                        session = session,
                        activeMission = activeMission,
                        assignedCount = assignedMissions.size,
                        completedCount = completedMissions.size,
                        isUnitOnDuty = isUnitOnDuty,
                        onToggleDuty = { IncidentRepository.toggleDuty() },
                        onNavigateToMissions = { selectedTab = RescueTeamTab.ASSIGNED_MISSIONS },
                        onNavigateToMap = { selectedTab = RescueTeamTab.MAP_NAV },
                        onOpenStatusUpdate = { id -> showStatusUpdateDialogForId = id },
                        onOpenReport = { mission ->
                            selectedMissionForReport = mission
                            selectedTab = RescueTeamTab.REPORT_SUBMIT
                        }
                    )
                }

                RescueTeamTab.ASSIGNED_MISSIONS -> {
                    RescueAssignedMissionsContent(
                        missions = assignedMissions,
                        onOpenStatusUpdate = { id -> showStatusUpdateDialogForId = id },
                        onOpenReport = { mission ->
                            selectedMissionForReport = mission
                            selectedTab = RescueTeamTab.REPORT_SUBMIT
                        },
                        onNavigateToMap = { selectedTab = RescueTeamTab.MAP_NAV }
                    )
                }

                RescueTeamTab.MAP_NAV -> {
                    FieldOperationsMapScreen(
                        onNavigateBack = { selectedTab = RescueTeamTab.DASHBOARD },
                        onSosClick = onSosClick,
                        onProfileClick = { selectedTab = RescueTeamTab.PROFILE }
                    )
                }

                RescueTeamTab.REPORT_SUBMIT -> {
                    RescueReportSubmissionContent(
                        activeMission = selectedMissionForReport ?: activeMission,
                        onSubmit = { report ->
                            IncidentRepository.submitRescueReport(report)
                            selectedTab = RescueTeamTab.ASSIGNED_MISSIONS
                        }
                    )
                }

                RescueTeamTab.PROFILE -> {
                    RescueProfileContent(
                        session = session,
                        isUnitOnDuty = isUnitOnDuty,
                        assignedCount = assignedMissions.size,
                        completedCount = completedMissions.size,
                        onNavigateToMissions = { selectedTab = RescueTeamTab.ASSIGNED_MISSIONS },
                        onLogout = onLogout
                    )
                }
            }

            // Mission Status Pipeline Transition Dialog
            showStatusUpdateDialogForId?.let { incidentId ->
                val incident = allIncidents.find { it.id == incidentId }
                if (incident != null) {
                    MissionStatusUpdateDialog(
                        incident = incident,
                        onDismiss = { showStatusUpdateDialogForId = null },
                        onSelectStatus = { newStatus ->
                            IncidentRepository.updateMissionStatus(incidentId, newStatus)
                            showStatusUpdateDialogForId = null
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// RESCUE TEAM DASHBOARD
// ---------------------------------------------------------------------------

@Composable
private fun RescueDashboardContent(
    session: UserSession,
    activeMission: Incident?,
    assignedCount: Int,
    completedCount: Int,
    isUnitOnDuty: Boolean,
    onToggleDuty: () -> Unit,
    onNavigateToMissions: () -> Unit,
    onNavigateToMap: () -> Unit,
    onOpenStatusUpdate: (String) -> Unit,
    onOpenReport: (Incident) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Squad Status Ribbon
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F766E).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(26.dp))
                        }
                        Column {
                            Text(session.teamName ?: "Rescue Squad", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(if (isUnitOnDuty) "On-Duty • Vehicle Telemetry Paired" else "Off-Duty Standby", fontSize = 13.sp, color = SecondarySlate)
                        }
                    }

                    Switch(
                        checked = isUnitOnDuty,
                        onCheckedChange = { onToggleDuty() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0F766E))
                    )
                }
            }
        }

        // Mission Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    modifier = Modifier.weight(1f).clickable(onClick = onNavigateToMissions)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Assigned Missions", fontSize = 13.sp, color = SecondarySlate, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$assignedCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Active tasks", fontSize = 12.sp, color = SecondarySlate)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Completed Rescues", fontSize = 13.sp, color = SecondarySlate, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$completedCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Logged by squad", fontSize = 12.sp, color = SecondarySlate)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Operations", fontSize = 13.sp, color = SecondarySlate, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(if (activeMission != null) "1" else "0", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Dispatches", fontSize = 12.sp, color = SecondarySlate)
                    }
                }
            }
        }

        // Active Rescue Hero Card
        item {
            Text("Active Rescue Mission", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (activeMission == null) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.CheckCircle,
                    title = "No Assigned Missions",
                    description = "No active rescue operations have been assigned to your team."
                )
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF0F766E).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(activeMission.id, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ErrorContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(activeMission.urgency.badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerHigh)
                            ) {
                                AsyncImage(
                                    model = activeMission.imageUrl,
                                    contentDescription = activeMission.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(activeMission.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Location: ${activeMission.address}", fontSize = 14.sp, color = SecondarySlate)
                                Text("Reporter: ${activeMission.reporterName}", fontSize = 14.sp, color = SecondarySlate)
                            }
                        }

                        // Special instructions
                        Surface(
                            color = SurfaceContainerHigh,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Special Notes: ${activeMission.notes}",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(10.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Live GPS Map Preview
                        val incLat = activeMission.latitude ?: 32.7157
                        val incLon = activeMission.longitude ?: -117.1611
                        val missionMarkers = remember(incLat, incLon, activeMission) {
                            listOf(
                                MapMarkerItem(
                                    id = "mission_${activeMission.id}",
                                    title = activeMission.title,
                                    snippet = activeMission.address,
                                    latitude = incLat,
                                    longitude = incLon,
                                    type = MapMarkerType.INCIDENT,
                                    urgency = activeMission.urgency
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigateToMap() }
                        ) {
                            PawRescueMap(
                                modifier = Modifier.fillMaxSize(),
                                centerLatitude = incLat,
                                centerLongitude = incLon,
                                initialZoom = 14.5,
                                markers = missionMarkers,
                                isInteractive = false,
                                showControls = false
                            )
                            Surface(
                                color = Color(0xDD1E293B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(14.dp))
                                    Text("Full Screen Map ›", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Status Progression Ribbon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Current Mission Status:", fontSize = 13.sp, color = SecondarySlate)
                                Text(activeMission.missionStatus.label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                            }
                            Button(
                                onClick = { onOpenStatusUpdate(activeMission.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("update_mission_status_btn")
                            ) {
                                Text("Update Status", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Quick action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToMap,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Navigate", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { onOpenReport(activeMission) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rescue Report", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ---------------------------------------------------------------------------
// ASSIGNED MISSIONS CONTENT (Strictly filtered to this team)
// ---------------------------------------------------------------------------

@Composable
private fun RescueAssignedMissionsContent(
    missions: List<Incident>,
    onOpenStatusUpdate: (String) -> Unit,
    onOpenReport: (Incident) -> Unit,
    onNavigateToMap: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Text("Assigned Rescue Missions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Filtered solely for your authorized unit credentials", fontSize = 12.sp, color = SecondarySlate)
        }

        if (missions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.CheckCircle,
                    title = "No Assigned Missions",
                    description = "No active rescue operations have been assigned to your team.",
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(missions) { mission ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("team_mission_${mission.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(mission.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F766E))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (mission.missionStatus == MissionStatus.COMPLETED) PrimaryFixed else SurfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(mission.missionStatus.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (mission.missionStatus == MissionStatus.COMPLETED) Color(0xFF002113) else Color(0xFF0F766E))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerHigh)
                        ) {
                            AsyncImage(
                                model = mission.imageUrl,
                                contentDescription = mission.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(mission.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Location: ${mission.address}", fontSize = 14.sp, color = SecondarySlate)
                            Text("Urgency: ${mission.urgency.label}", fontSize = 13.sp, color = ErrorRed, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Text("Description: ${mission.notes}", fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenStatusUpdate(mission.id) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Update Status", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { onOpenReport(mission) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit Report", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ---------------------------------------------------------------------------
// MISSION STATUS PIPELINE DIALOG
// Assigned -> Accepted -> En Route -> Arrived -> Rescue In Progress -> Animal Rescued -> Transported -> Completed
// ---------------------------------------------------------------------------

@Composable
private fun MissionStatusUpdateDialog(
    incident: Incident,
    onDismiss: () -> Unit,
    onSelectStatus: (MissionStatus) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Advance Mission Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text("Incident: ${incident.id} (${incident.title})", fontSize = 12.sp, color = SecondarySlate)

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MissionStatus.entries) { status ->
                        val isCurrent = status == incident.missionStatus
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) Color(0xFF0F766E).copy(alpha = 0.15f) else SurfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectStatus(status) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrent) Color(0xFF0F766E) else SurfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${status.stepNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) Color.White else SecondarySlate)
                                    }
                                    Text(status.label, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                                }
                                if (isCurrent) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// RESCUE REPORT SUBMISSION CONTENT
// ---------------------------------------------------------------------------

@Composable
private fun RescueReportSubmissionContent(
    activeMission: Incident?,
    onSubmit: (RescueReport) -> Unit
) {
    var outcome by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var actionTaken by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var shelter by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Text("Rescue Outcome Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                if (activeMission != null) "Submitting field report for Case ${activeMission.id}" else "Select a mission to report",
                fontSize = 12.sp,
                color = SecondarySlate
            )
        }

        if (activeMission == null) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Info,
                    title = "No Mission Selected",
                    description = "Please select an active mission from the Missions tab to submit a rescue report.",
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            item {
                OutlinedTextField(
                    value = outcome,
                    onValueChange = { outcome = it },
                    label = { Text("Rescue Outcome") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Animal Condition Post-Rescue") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = actionTaken,
                    onValueChange = { actionTaken = it },
                    label = { Text("Action Taken in Field") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            item {
                OutlinedTextField(
                    value = shelter,
                    onValueChange = { shelter = it },
                    label = { Text("Receiving Treatment Facility / Shelter") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Rescue Notes & Evidence Logs") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            item {
                Button(
                    onClick = {
                        onSubmit(
                            RescueReport(
                                incidentId = activeMission.id,
                                outcome = outcome,
                                animalCondition = condition,
                                actionTaken = actionTaken,
                                rescueNotes = notes,
                                rescuePhotos = listOf(activeMission.beforeRescuePhotoUrl, activeMission.afterRescuePhotoUrl),
                                treatmentShelter = shelter
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Final Rescue Report", fontWeight = FontWeight.Bold)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ---------------------------------------------------------------------------
// RESCUE TEAM PROFILE & OPERATIONAL READINESS (Redesigned & Fully Functional)
// ---------------------------------------------------------------------------

@Composable
private fun RescueProfileContent(
    session: UserSession,
    isUnitOnDuty: Boolean,
    assignedCount: Int,
    completedCount: Int,
    onNavigateToMissions: () -> Unit,
    onLogout: () -> Unit
) {
    val vehicleRecord by IncidentRepository.vehicleEquipment.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showReportIssueDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showInspectionHistoryDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var dutyStatusError by remember { mutableStateOf<String?>(null) }
    var reportingItemId by remember { mutableStateOf<String?>(null) }

    // Equipment categories expandable state
    val categories = remember {
        listOf(
            "Medical Equipment",
            "Animal Transport Equipment",
            "Safety Equipment",
            "Communication Equipment"
        )
    }
    var expandedCategories by remember {
        mutableStateOf(setOf("Medical Equipment", "Animal Transport Equipment", "Safety Equipment", "Communication Equipment"))
    }

    // Ready count calculation
    val totalEquipment = vehicleRecord.equipmentList.size
    val readyEquipment = vehicleRecord.equipmentList.count { it.status == EquipmentStatus.READY }
    val issuesReported = vehicleRecord.equipmentList.count { it.status == EquipmentStatus.REPORTED_ISSUE }

    // 1. Logout confirmation dialog
    if (showLogoutDialog) {
        StandardConfirmationDialog(
            title = "Sign Out from Rescue Team Portal?",
            message = "You will be disconnected from the active squad field terminal. Saved missions, telemetry checkpoints, equipment checklists, and submitted triage reports remain securely stored in the system.",
            confirmLabel = "Sign Out",
            cancelLabel = "Cancel",
            isDestructive = true,
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    // 2. Dedicated Team Contact Details Dialog
    if (showContactDialog) {
        Dialog(onDismissRequest = { showContactDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Squad Contact Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showContactDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0FDFA))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(20.dp))
                            Text(session.teamName ?: session.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                        }
                        Text("Unit Identification: ${session.teamId ?: session.userId}", fontSize = 13.sp, color = SecondarySlate)
                        Text("Designated Responders: ${session.name}", fontSize = 13.sp, color = SecondarySlate)
                        Text("Official Dispatch Email: ${session.email}", fontSize = 13.sp, color = SecondarySlate)
                        Text("Direct Mobile Line: ${if (session.phone.isNotBlank()) session.phone else "Not configured"}", fontSize = 13.sp, color = SecondarySlate)
                        Text("VHF Tactical Radio: Frequency VHF Channel 4 (Encrypted)", fontSize = 13.sp, color = SecondarySlate)
                    }

                    Button(
                        onClick = { showContactDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 3. Inspection History Dialog
    if (showInspectionHistoryDialog) {
        Dialog(onDismissRequest = { showInspectionHistoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Equipment Inspection Log", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showInspectionHistoryDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    if (vehicleRecord.inspectionHistory.isEmpty()) {
                        Text("No inspection entries logged for this shift.", fontSize = 13.sp, color = SecondarySlate)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(vehicleRecord.inspectionHistory) { record ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(record.itemName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(record.status.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(record.status.badgeColor))
                                    }
                                    Text(record.notes, fontSize = 12.sp, color = SecondarySlate)
                                    Text("Logged by ${record.inspectorName} • ${record.timestamp}", fontSize = 11.sp, color = SecondarySlate)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showInspectionHistoryDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 4. Change Password Dialog
    if (showChangePasswordDialog) {
        Dialog(onDismissRequest = { showChangePasswordDialog = false }) {
            var currPass by remember { mutableStateOf("") }
            var newPass by remember { mutableStateOf("") }
            var error by remember { mutableStateOf<String?>(null) }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Change Squad Password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showChangePasswordDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    if (error != null) {
                        Text(error ?: "", fontSize = 13.sp, color = ErrorRed)
                    }

                    OutlinedTextField(
                        value = currPass,
                        onValueChange = { currPass = it; error = null },
                        label = { Text("Current Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it; error = null },
                        label = { Text("New Password (min 6 chars)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showChangePasswordDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val res = IncidentRepository.changePassword(session.userId, currPass, newPass)
                                if (res.isSuccess) {
                                    showChangePasswordDialog = false
                                    infoMessage = "Team password changed successfully."
                                } else {
                                    error = res.exceptionOrNull()?.message ?: "Failed to change password"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 5. Report Equipment Issue Dialog
    if (showReportIssueDialog) {
        Dialog(onDismissRequest = { showReportIssueDialog = false; reportingItemId = null }) {
            var issueNote by remember { mutableStateOf("") }
            var selectedItem by remember { mutableStateOf(reportingItemId ?: vehicleRecord.equipmentList.firstOrNull()?.id ?: "") }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Report Damaged / Missing Equipment", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showReportIssueDialog = false; reportingItemId = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text("Select equipment item to flag for immediate HQ maintenance inspection or replacement:", fontSize = 13.sp, color = SecondarySlate)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        vehicleRecord.equipmentList.take(5).forEach { item ->
                            val isChosen = selectedItem == item.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) Color(0xFFCCFBF1) else Color(0xFFF8FAFC))
                                    .clickable { selectedItem = item.id }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, fontSize = 13.sp, fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium)
                                if (isChosen) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = issueNote,
                        onValueChange = { issueNote = it },
                        label = { Text("Issue Description / Damage Note") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (issueNote.isNotBlank() && selectedItem.isNotBlank()) {
                                IncidentRepository.reportEquipmentIssue(
                                    itemId = selectedItem,
                                    issueDescription = issueNote,
                                    reporterName = session.name
                                )
                                showReportIssueDialog = false
                                reportingItemId = null
                                infoMessage = "Equipment damage ticket recorded persistently in system log."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Equipment Issue", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (infoMessage != null) {
        AlertDialog(
            onDismissRequest = { infoMessage = null },
            title = { Text("Terminal Notice", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Text(infoMessage ?: "", fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = { infoMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                ) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = SurfaceContainerLowest
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // -------------------------------------------------------------------
        // A. SQUAD IDENTITY (Compact Top Card)
        // -------------------------------------------------------------------
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("rescue_team_profile_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFCCFBF1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = Color(0xFF0F766E),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = session.teamName ?: session.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Team ID: ${session.teamId ?: session.userId}",
                                    fontSize = 13.sp,
                                    color = SecondarySlate,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Duty Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isUnitOnDuty) Color(0xFFDCFCE7) else Color(0xFFF3F4F6))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isUnitOnDuty) "ON DUTY" else "OFF DUTY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnitOnDuty) Color(0xFF166534) else SecondarySlate
                            )
                        }
                    }

                    // Metadata row: Assigned Vehicle & Contact Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("Assigned Vehicle", fontSize = 11.sp, color = SecondarySlate)
                            Text(vehicleRecord.vehicleId, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showContactDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF0F766E))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Team Contacts", fontSize = 12.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------
        // B. OPERATIONAL READINESS (Three Compact Status Cards)
        // -------------------------------------------------------------------
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Operational Readiness",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Card 1: Assigned Missions
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToMissions() }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(18.dp))
                                Text("View ›", fontSize = 10.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "$assignedCount",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E)
                            )
                            Text(
                                text = "Assigned Missions",
                                fontSize = 12.sp,
                                color = SecondarySlate,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Card 2: Equipment Readiness
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = if (issuesReported == 0) Color(0xFF166534) else Color(0xFFD97706), modifier = Modifier.size(18.dp))
                            Text(
                                text = "$readyEquipment/$totalEquipment",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (issuesReported == 0) Color(0xFF166534) else Color(0xFFD97706)
                            )
                            Text(
                                text = if (issuesReported == 0) "All Ready" else "$issuesReported Flagged",
                                fontSize = 12.sp,
                                color = SecondarySlate,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Card 3: Vehicle / Telemetry Status (Accurate state, never fabricate live GPS)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(18.dp))
                            Text(
                                text = vehicleRecord.readinessStatus,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "Telemetry: Unavailable",
                                fontSize = 11.sp,
                                color = SecondarySlate,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------
        // C. DUTY STATUS CONTROL (Functional On / Off Duty with Validation)
        // -------------------------------------------------------------------
        item {
            SettingsSectionCard(
                title = "Duty Status Control",
                subtitle = "Manage active field availability with Central Operations Desk",
                titleColor = Color(0xFF0F766E)
            ) {
                if (dutyStatusError != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dutyStatusError ?: "",
                            fontSize = 13.sp,
                            color = ErrorRed,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                SettingsSwitchRow(
                    title = if (isUnitOnDuty) "On Duty (Active Dispatch)" else "Off Duty (Unavailable)",
                    subtitle = if (isUnitOnDuty) "Squad is actively routed to nearby distress calls" else "Squad is off-shift; automatic dispatches are paused",
                    checked = isUnitOnDuty,
                    onCheckedChange = { nextDuty ->
                        dutyStatusError = null
                        val result = IncidentRepository.setRescueTeamDuty(nextDuty)
                        if (result.isFailure) {
                            dutyStatusError = result.exceptionOrNull()?.message
                        } else {
                            infoMessage = result.getOrNull()
                        }
                    },
                    icon = Icons.Default.HealthAndSafety,
                    iconTint = Color(0xFF0F766E)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Lifetime Missions Completed", fontSize = 14.sp, color = SecondarySlate)
                    Text("$completedCount Rescues", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                }
            }
        }

        // -------------------------------------------------------------------
        // D. VEHICLE & EQUIPMENT (Expandable Categories & Inspection Actions)
        // -------------------------------------------------------------------
        item {
            SettingsSectionCard(
                title = "Vehicle & Equipment",
                subtitle = "Triage gear, transport containment, and safety inventory",
                titleColor = Color(0xFF0F766E),
                action = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { showInspectionHistoryDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF0F766E))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("History", fontSize = 11.sp, color = Color(0xFF0F766E))
                        }
                        OutlinedButton(
                            onClick = { showReportIssueDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(13.dp), tint = ErrorRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report Issue", fontSize = 11.sp, color = ErrorRed)
                        }
                    }
                }
            ) {
                // Vehicle Details Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(vehicleRecord.modelDescription, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(vehicleRecord.registrationPlate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SecondarySlate)
                    }
                    Text("Last Inspection: ${vehicleRecord.inspectionDate} • Readiness: ${vehicleRecord.readinessStatus}", fontSize = 12.sp, color = SecondarySlate)
                }

                Text("Equipment Categories", fontSize = 15.sp, fontWeight = FontWeight.Bold)

                // Expandable category sections
                categories.forEach { category ->
                    val isExpanded = expandedCategories.contains(category)
                    val itemsInCategory = vehicleRecord.equipmentList.filter { it.category == category }
                    val allReady = itemsInCategory.all { it.status == EquipmentStatus.READY }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedCategories = if (isExpanded) {
                                        expandedCategories - category
                                    } else {
                                        expandedCategories + category
                                    }
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (allReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (allReady) Color(0xFF166534) else Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(category, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("(${itemsInCategory.size})", fontSize = 12.sp, color = SecondarySlate)
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = SecondarySlate
                            )
                        }

                        // Category items when expanded
                        if (isExpanded) {
                            itemsInCategory.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .clickable { IncidentRepository.toggleEquipmentItem(item.id) }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                            contentDescription = null,
                                            tint = if (item.isChecked) Color(0xFF0F766E) else SecondarySlate,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                            Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            Text(item.notes, fontSize = 11.sp, color = SecondarySlate)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(item.status.badgeColor).copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = item.status.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(item.status.badgeColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------
        // E. ORGANIZATION SUPPORT (Four Compact, Functional Menu Rows)
        // -------------------------------------------------------------------
        item {
            SettingsSectionCard(
                title = "Organization Support",
                subtitle = "Central dispatch escalation, SOP protocols, and credentials",
                titleColor = Color(0xFF0F766E)
            ) {
                // 1. Contact Organization Admin
                SettingsActionRow(
                    title = "Contact Organization Admin",
                    subtitle = "Call or message Central Operations Dispatch Desk",
                    onClick = {
                        infoMessage = "Central Dispatch Desk: +1 (800) 555-PAWS (Ext 1) • Operations: admin@pawrescue.org • Priority VHF Channel 4."
                    },
                    icon = Icons.Default.Phone,
                    iconTint = Color(0xFF0F766E),
                    trailingText = "Call ›"
                )

                // 2. View Emergency Protocols
                SettingsActionRow(
                    title = "View Emergency Protocols",
                    subtitle = "Field stabilization procedures for trauma, rabies, and raptors",
                    onClick = {
                        infoMessage = "Emergency Field SOP: 1. Ensure scene perimeter safety. 2. Apply gentle catchpole or towel wrap. 3. Immediate pressure bandaging for active arterial bleeds. 4. Transport directly to County Shelter Triage."
                    },
                    icon = Icons.Default.Shield,
                    iconTint = Color(0xFF0F766E),
                    trailingText = "View ›"
                )

                // 3. Change Password
                SettingsActionRow(
                    title = "Change Password",
                    subtitle = "Update security password for this mobile field responder unit",
                    onClick = { showChangePasswordDialog = true },
                    icon = Icons.Default.Lock,
                    iconTint = Color(0xFF0F766E),
                    trailingText = "Update ›"
                )

                // 4. Account & Security
                SettingsActionRow(
                    title = "Account & Security",
                    subtitle = "Review session token, encryption cipher, and responder clearances",
                    onClick = {
                        infoMessage = "Account Clearance: Operational Rescue Field Responder (Level 2). Session encryption: TLS 1.3 / AES-256 enabled."
                    },
                    icon = Icons.Default.Security,
                    iconTint = Color(0xFF0F766E),
                    trailingText = "Review ›"
                )
            }
        }

        // -------------------------------------------------------------------
        // F. RESCUE TEAM SIGN OUT BUTTON (Compact & Centered)
        // -------------------------------------------------------------------
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CompactSignOutButton(
                    onClick = { showLogoutDialog = true },
                    label = "Sign Out",
                    testTag = "rescue_team_logout_button"
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
