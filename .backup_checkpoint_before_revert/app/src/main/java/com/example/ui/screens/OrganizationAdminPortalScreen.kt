package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Layers
import com.example.ui.components.CompactSignOutButton
import com.example.ui.components.SettingsActionRow
import com.example.ui.components.SettingsSectionCard
import com.example.ui.components.CollapsibleSettingsSectionCard
import com.example.ui.components.SettingsSwitchRow
import com.example.ui.components.StandardConfirmationDialog
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.HorizontalDivider
import com.example.data.FirebaseIncidentDataSource
import com.example.data.IncidentRepository
import com.example.model.AccountStatus
import com.example.model.Incident
import com.example.model.RegistrationApprovalStatus
import com.example.model.RescueRegistrationRequest
import com.example.model.RescueStage
import com.example.model.RescueTeamMember
import com.example.model.UrgencyLevel
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

enum class AdminTab(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("HQ KPI", Icons.Default.Dashboard, "admin_tab_dashboard"),
    COMPLAINTS("Complaints", Icons.Default.FilterList, "admin_tab_complaints"),
    LIVE_MAP("Live Map", Icons.Default.Map, "admin_tab_map"),
    TEAMS("Teams", Icons.Default.Groups, "admin_tab_teams"),
    USERS("Citizens", Icons.Default.Person, "admin_tab_users"),
    ANALYTICS("Analytics", Icons.Default.Analytics, "admin_tab_analytics"),
    SETTINGS("Settings", Icons.Default.Settings, "admin_tab_settings")
}

@Composable
fun OrganizationAdminPortalScreen(
    session: UserSession,
    onLogout: () -> Unit,
    onSosClick: () -> Unit,
    onViewIncidentDetails: (String) -> Unit
) {
    // STRICT ROLE GUARD: Access granted ONLY to ORGANIZATION_ADMIN role
    if (session.role != UserRole.ORGANIZATION_ADMIN) {
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
                    "This area is restricted to Organization Administrators. Your account role is '${session.role.title}'.",
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

    var selectedTab by remember { mutableStateOf(AdminTab.DASHBOARD) }
    var assigningIncidentId by remember { mutableStateOf<String?>(null) }
    var showAddTeamMemberDialog by remember { mutableStateOf(false) }

    val allIncidents by IncidentRepository.incidents.collectAsState()
    val teamMembers by IncidentRepository.teamMembers.collectAsState()
    val citizenUsers by IncidentRepository.citizenUsers.collectAsState()
    val rescueRequests by IncidentRepository.rescueRequests.collectAsState()
    val systemSettings by IncidentRepository.systemSettings.collectAsState()

    Scaffold(
        topBar = {
            StrayCareHeader(
                title = "PawRescue",
                subtitle = when (selectedTab) {
                    AdminTab.DASHBOARD -> "Operations Command KPI"
                    AdminTab.COMPLAINTS -> "Complaint Review & Triage"
                    AdminTab.LIVE_MAP -> "Live Operational Map"
                    AdminTab.TEAMS -> "Rescue Fleet Squads"
                    AdminTab.USERS -> "Citizen Accounts"
                    AdminTab.ANALYTICS -> "SAR Metrics & Analytics"
                    AdminTab.SETTINGS -> "System Settings"
                },
                showBackButton = false,
                onSosClick = onSosClick,
                onProfileClick = { selectedTab = AdminTab.SETTINGS },
                onLogoutClick = onLogout
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceContainerLowest,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().testTag("admin_bottom_nav")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(60.dp)
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdminTab.entries.forEach { tab ->
                        val isSelected = tab == selectedTab
                        val tint = if (isSelected) TertiaryBlue else SecondarySlate
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
                                    .size(26.dp)
                                    .then(
                                        if (isSelected) {
                                            Modifier.background(
                                                color = TertiaryBlue.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        } else Modifier
                                    )
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = tint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
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
                AdminTab.DASHBOARD -> {
                    AdminDashboardContent(
                        allIncidents = allIncidents,
                        teamMembers = teamMembers,
                        onViewComplaints = { selectedTab = AdminTab.COMPLAINTS },
                        onViewMap = { selectedTab = AdminTab.LIVE_MAP },
                        onViewTeams = { selectedTab = AdminTab.TEAMS }
                    )
                }

                AdminTab.COMPLAINTS -> {
                    AdminComplaintManagementContent(
                        incidents = allIncidents,
                        onVerify = { id -> IncidentRepository.adminVerifyComplaint(id) },
                        onReject = { id -> IncidentRepository.adminRejectComplaint(id, "Rejected by Admin") },
                        onSetPriority = { id, urgency -> IncidentRepository.adminSetPriority(id, urgency) },
                        onAssignTeam = { id -> assigningIncidentId = id },
                        onViewDetails = onViewIncidentDetails
                    )
                }

                AdminTab.LIVE_MAP -> {
                    FieldOperationsMapScreen(
                        onNavigateBack = { selectedTab = AdminTab.DASHBOARD },
                        onSosClick = onSosClick,
                        onProfileClick = { selectedTab = AdminTab.SETTINGS }
                    )
                }

                AdminTab.TEAMS -> {
                    AdminTeamManagementContent(
                        teams = teamMembers,
                        onAddMember = { showAddTeamMemberDialog = true },
                        onApprove = { id -> IncidentRepository.approveTeamMember(id) },
                        onToggleStatus = { id -> IncidentRepository.toggleTeamMemberStatus(id) }
                    )
                }

                AdminTab.USERS -> {
                    AdminUserManagementContent(
                        users = citizenUsers,
                        rescueRequests = rescueRequests,
                        adminSession = session,
                        onToggleBan = { id -> IncidentRepository.toggleCitizenBan(id) },
                        onApproveRequest = { req ->
                            FirebaseIncidentDataSource.approveRescueRegistrationRequest(req, session) { _, _ -> }
                        },
                        onRejectRequest = { req, reason ->
                            FirebaseIncidentDataSource.rejectRescueRegistrationRequest(req, reason, session) { _, _ -> }
                        }
                    )
                }

                AdminTab.ANALYTICS -> {
                    AdminAnalyticsContent(allIncidents = allIncidents)
                }

                AdminTab.SETTINGS -> {
                    AdminSettingsContent(
                        settings = systemSettings,
                        session = session,
                        onNavigateToTeams = { selectedTab = AdminTab.TEAMS },
                        onNavigateToUsers = { selectedTab = AdminTab.USERS },
                        onLogout = onLogout
                    )
                }
            }

            // Assign Rescue Team Dialog
            assigningIncidentId?.let { incId ->
                AssignRescueTeamDialog(
                    incidentId = incId,
                    availableTeams = teamMembers.filter { it.status == AccountStatus.ACTIVE },
                    onDismiss = { assigningIncidentId = null },
                    onAssign = { teamId, teamName ->
                        IncidentRepository.adminAssignTeam(incId, teamId, teamName)
                        assigningIncidentId = null
                    }
                )
            }

            // Add Rescue Team Member Dialog
            if (showAddTeamMemberDialog) {
                AddTeamMemberDialog(
                    onDismiss = { showAddTeamMemberDialog = false },
                    onAdd = { name, email, phone, teamName ->
                        IncidentRepository.addRescueTeamMember(name, email, phone, teamName)
                        showAddTeamMemberDialog = false
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 0: ADMIN KPI DASHBOARD
// Total complaints, Pending, Verified, Critical, Active Rescues, Completed, Available teams, Suspicious
// ---------------------------------------------------------------------------

@Composable
private fun AdminDashboardContent(
    allIncidents: List<Incident>,
    teamMembers: List<RescueTeamMember>,
    onViewComplaints: () -> Unit,
    onViewMap: () -> Unit,
    onViewTeams: () -> Unit
) {
    val totalComplaints = allIncidents.size
    val pendingComplaints = allIncidents.count { it.currentStage == RescueStage.SUBMITTED }
    val verifiedComplaints = allIncidents.count { it.currentStage >= RescueStage.AI_VERIFIED }
    val criticalComplaints = allIncidents.count { it.urgency == UrgencyLevel.CRITICAL }
    val activeRescues = allIncidents.count { it.currentStage >= RescueStage.TEAM_ASSIGNED && it.currentStage < RescueStage.RESOLVED }
    val completedRescues = allIncidents.count { it.currentStage == RescueStage.RESOLVED }
    val availableTeams = teamMembers.count { it.status == AccountStatus.ACTIVE }
    val suspiciousComplaints = allIncidents.count { it.isSuspectedDuplicate }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // HQ Overview Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = TertiaryBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PawRescue Operations Command", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Real-time automated incident correlation, fleet tracking, and clinical triage disposition.", fontSize = 14.sp, lineHeight = 20.sp, color = Color.White.copy(alpha = 0.9f))
                }
            }
        }

        // 8 Key KPI Cards in Grid
        item {
            Text("Operational Telemetry KPI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiStatCard("Total Complaints", "$totalComplaints", PrimaryEmerald, Modifier.weight(1f), onClick = onViewComplaints)
                    KpiStatCard("Pending Triage", "$pendingComplaints", ErrorRed, Modifier.weight(1f), onClick = onViewComplaints)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiStatCard("AI Verified", "$verifiedComplaints", Color(0xFF0F766E), Modifier.weight(1f), onClick = onViewComplaints)
                    KpiStatCard("Critical P-1", "$criticalComplaints", ErrorRed, Modifier.weight(1f), onClick = onViewComplaints)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiStatCard("Active Rescues", "$activeRescues", TertiaryBlue, Modifier.weight(1f), onClick = onViewMap)
                    KpiStatCard("Completed Rescues", "$completedRescues", PrimaryEmerald, Modifier.weight(1f), onClick = onViewComplaints)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiStatCard("Available Teams", "$availableTeams", Color(0xFF0F766E), Modifier.weight(1f), onClick = onViewTeams)
                    KpiStatCard("Suspicious Dups", "$suspiciousComplaints", Color(0xFFF59E0B), Modifier.weight(1f), onClick = onViewComplaints)
                }
            }
        }

        // Live Operations Fleet Overview Map Card
        item {
            val adminMarkers = remember(allIncidents) {
                allIncidents.take(10).mapNotNull { inc ->
                    val lat = inc.latitude
                    val lon = inc.longitude
                    if (lat != null && lon != null) {
                        MapMarkerItem(
                            id = "admin_map_${inc.id}",
                            title = "#${inc.id} (${inc.species})",
                            snippet = "${inc.address} • ${inc.urgency.label}",
                            latitude = lat,
                            longitude = lon,
                            type = MapMarkerType.INCIDENT,
                            urgency = inc.urgency
                        )
                    } else null
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewMap() }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
                            Text("Live Operations Fleet Map", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("Open Full Map ›", color = PrimaryEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        PawRescueMap(
                            modifier = Modifier.fillMaxSize(),
                            centerLatitude = 32.7157,
                            centerLongitude = -117.1611,
                            initialZoom = 13.5,
                            markers = adminMarkers,
                            isInteractive = false,
                            showControls = false
                        )
                    }
                }
            }
        }

        // Quick Command Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewComplaints,
                    colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Triage Complaints", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onViewMap,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Live Fleet Map", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 14.sp, color = SecondarySlate, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp, color = accentColor)
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 1: COMPLAINT MANAGEMENT
// View, Search, Filter, Review AI, Verify, Reject, Request Info, Priority, Assign Team
// ---------------------------------------------------------------------------

@Composable
private fun AdminComplaintManagementContent(
    incidents: List<Incident>,
    onVerify: (String) -> Unit,
    onReject: (String) -> Unit,
    onSetPriority: (String, UrgencyLevel) -> Unit,
    onAssignTeam: (String) -> Unit,
    onViewDetails: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filtered = remember(incidents, searchQuery, selectedFilter) {
        incidents.filter { inc ->
            val matchesQuery = searchQuery.isEmpty() ||
                inc.id.contains(searchQuery, ignoreCase = true) ||
                inc.title.contains(searchQuery, ignoreCase = true) ||
                inc.species.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "CRITICAL" -> inc.urgency == UrgencyLevel.CRITICAL
                "PENDING" -> inc.currentStage == RescueStage.SUBMITTED
                "VERIFIED" -> inc.currentStage >= RescueStage.AI_VERIFIED
                "DUPLICATES" -> inc.isSuspectedDuplicate
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Text("Complaint Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Review community reports, AI confidence, verify authenticity, and assign squads", fontSize = 14.sp, color = SecondarySlate)
        }

        // Search Input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by ID, Animal, or Location", fontSize = 14.sp) },
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 16.sp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_search_complaints")
            )
        }

        // Filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "CRITICAL", "PENDING", "VERIFIED", "DUPLICATES").forEach { f ->
                    val isSel = f == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isSel) TertiaryBlue else SurfaceContainerHigh)
                            .clickable { selectedFilter = f }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(f, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else SecondarySlate)
                    }
                }
            }
        }

        // Incidents list
        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Search,
                    title = "No Complaints Found",
                    description = if (searchQuery.isNotEmpty()) "No incident reports match your search query." else "There are no complaints matching the selected filter.",
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(filtered) { inc ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_complaint_card_${inc.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(inc.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TertiaryBlue)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (inc.urgency == UrgencyLevel.CRITICAL) ErrorContainer else SurfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(inc.urgency.badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (inc.urgency == UrgencyLevel.CRITICAL) ErrorRed else SecondarySlate)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerHigh)
                        ) {
                            AsyncImage(
                                model = inc.imageUrl,
                                contentDescription = inc.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(inc.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Reporter: ${inc.reporterName}", fontSize = 14.sp, color = SecondarySlate)
                            Text("Address: ${inc.address}", fontSize = 14.sp, color = SecondarySlate)
                            Text("Assigned: ${inc.assignedUnit}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F766E))
                        }
                    }

                    // AI Verification Banner
                    Surface(
                        color = SurfaceContainerLow,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
                                Text("AI Vision Confidence: 98.4% (Canine Validated)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Clean Metadata", fontSize = 12.sp, color = SecondarySlate)
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onVerify(inc.id) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Verify", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryEmerald)
                        }

                        Button(
                            onClick = { onAssignTeam(inc.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Assign Team", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { onReject(inc.id) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
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
// TAB 3: RESCUE TEAM MANAGEMENT
// Add member, approve accounts, activate/deactivate, team load
// ---------------------------------------------------------------------------

@Composable
private fun AdminTeamManagementContent(
    teams: List<RescueTeamMember>,
    onAddMember: () -> Unit,
    onApprove: (String) -> Unit,
    onToggleStatus: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Rescue Team Fleet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Manage certified ambulances and rescue squads", fontSize = 14.sp, color = SecondarySlate)
                }
                Button(
                    onClick = onAddMember,
                    colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Team", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (teams.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.LocalShipping,
                    title = "No Teams Registered",
                    description = "No rescue squads or ambulances are currently enrolled in the fleet.",
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(teams) { member ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(member.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (member.status == AccountStatus.ACTIVE) PrimaryFixed else ErrorContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                member.status.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (member.status == AccountStatus.ACTIVE) Color(0xFF002113) else ErrorRed
                            )
                        }
                    }

                    Text("Squad: ${member.teamName} (${member.vehicleId})", fontSize = 14.sp, color = SecondarySlate)
                    Text("Contact: ${member.email} • ${member.phone}", fontSize = 14.sp, color = SecondarySlate)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (member.status != AccountStatus.ACTIVE) {
                            Button(
                                onClick = { onApprove(member.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Approve & Activate", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onToggleStatus(member.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Deactivate Account", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
                            }
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
// TAB 4: USER MANAGEMENT (Citizen Accounts & Abuse Moderation)
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// TAB 4: USER MANAGEMENT (Citizen Accounts & Rescue Team Registration Requests)
// ---------------------------------------------------------------------------

private enum class UserManagementSection {
    CITIZEN_ACCOUNTS,
    RESCUE_REQUESTS
}

private enum class RequestFilterTab(val label: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    ALL("All")
}

@Composable
private fun AdminUserManagementContent(
    users: List<com.example.model.CitizenUserRecord>,
    rescueRequests: List<RescueRegistrationRequest>,
    adminSession: UserSession,
    onToggleBan: (String) -> Unit,
    onApproveRequest: (RescueRegistrationRequest) -> Unit,
    onRejectRequest: (RescueRegistrationRequest, String?) -> Unit
) {
    var selectedSection by remember { mutableStateOf(UserManagementSection.RESCUE_REQUESTS) }
    var selectedFilter by remember { mutableStateOf(RequestFilterTab.PENDING) }

    var approveTarget by remember { mutableStateOf<RescueRegistrationRequest?>(null) }
    var rejectTarget by remember { mutableStateOf<RescueRegistrationRequest?>(null) }
    var rejectReasonInput by remember { mutableStateOf("") }

    val pendingCount = rescueRequests.count { it.status == RegistrationApprovalStatus.PENDING }
    val approvedCount = rescueRequests.count { it.status == RegistrationApprovalStatus.APPROVED }
    val rejectedCount = rescueRequests.count { it.status == RegistrationApprovalStatus.REJECTED }

    // Dialog for Approve Confirmation
    if (approveTarget != null) {
        val target = approveTarget!!
        AlertDialog(
            onDismissRequest = { approveTarget = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F6ED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Approve Rescue Team Application",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Confirm approval for ${target.fullName} (${target.email})?\n\nThey will be assigned to Unit Beta-3 (Rapid Van), granted active Rescue Team privileges, and permitted to log into the Rescue Team portal.",
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApproveRequest(target)
                        approveTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_approve_dialog_btn")
                ) {
                    Text("Approve & Activate", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { approveTarget = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // Dialog for Reject Confirmation
    if (rejectTarget != null) {
        val target = rejectTarget!!
        AlertDialog(
            onDismissRequest = {
                rejectTarget = null
                rejectReasonInput = ""
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Reject Rescue Team Application",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Are you sure you want to decline the Rescue Team registration request for ${target.fullName} (${target.email})?",
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )
                    OutlinedTextField(
                        value = rejectReasonInput,
                        onValueChange = { rejectReasonInput = it },
                        label = { Text("Rejection reason (optional)") },
                        placeholder = { Text("e.g. Does not meet squad criteria") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reject_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectRequest(target, rejectReasonInput.ifBlank { null })
                        rejectTarget = null
                        rejectReasonInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_reject_dialog_btn")
                ) {
                    Text("Confirm Rejection", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        rejectTarget = null
                        rejectReasonInput = ""
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Section Tabs: Citizen User Accounts vs Rescue Team Registration Requests
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Citizen Accounts tab
                    Surface(
                        color = if (selectedSection == UserManagementSection.CITIZEN_ACCOUNTS) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        shadowElevation = if (selectedSection == UserManagementSection.CITIZEN_ACCOUNTS) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSection = UserManagementSection.CITIZEN_ACCOUNTS }
                            .testTag("tab_citizen_accounts")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Citizen Accounts (${users.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedSection == UserManagementSection.CITIZEN_ACCOUNTS) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSection == UserManagementSection.CITIZEN_ACCOUNTS) PrimaryEmerald else SecondarySlate
                            )
                        }
                    }

                    // Rescue Team Registration Requests tab
                    Surface(
                        color = if (selectedSection == UserManagementSection.RESCUE_REQUESTS) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        shadowElevation = if (selectedSection == UserManagementSection.RESCUE_REQUESTS) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable { selectedSection = UserManagementSection.RESCUE_REQUESTS }
                            .testTag("tab_rescue_requests")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rescue Team Requests",
                                fontSize = 13.sp,
                                fontWeight = if (selectedSection == UserManagementSection.RESCUE_REQUESTS) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSection == UserManagementSection.RESCUE_REQUESTS) PrimaryEmerald else SecondarySlate
                            )
                            if (pendingCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$pendingCount",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Branch by subsection
        if (selectedSection == UserManagementSection.CITIZEN_ACCOUNTS) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Citizen User Accounts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Track reporter behavior, verified submissions, and moderate abusive accounts", fontSize = 14.sp, color = SecondarySlate)
                }
            }

            if (users.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Person,
                        title = "No Citizen Users Found",
                        description = "There are no registered citizen user profiles in the database.",
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(users) { user ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text(user.email, fontSize = 14.sp, color = SecondarySlate)
                                Text("Submissions: ${user.reportsSubmitted} • Status: ${user.status.name}", fontSize = 13.sp, color = if (user.isBanned) ErrorRed else PrimaryEmerald)
                            }

                            Button(
                                onClick = { onToggleBan(user.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (user.isBanned) PrimaryEmerald else ErrorRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (user.isBanned) "Unban" else "Ban User", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        } else {
            // RESCUE TEAM REGISTRATION REQUESTS SECTION
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Rescue Team Registration Requests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Review and verify frontline field search and rescue volunteer registration applications.", fontSize = 14.sp, color = SecondarySlate)
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RequestFilterTab.entries.forEach { filterTab ->
                        val count = when (filterTab) {
                            RequestFilterTab.PENDING -> pendingCount
                            RequestFilterTab.APPROVED -> approvedCount
                            RequestFilterTab.REJECTED -> rejectedCount
                            RequestFilterTab.ALL -> rescueRequests.size
                        }
                        val isSelected = selectedFilter == filterTab
                        Surface(
                            color = if (isSelected) PrimaryEmerald else SurfaceContainerLow,
                            shape = RoundedCornerShape(20.dp),
                            border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
                            modifier = Modifier
                                .clickable { selectedFilter = filterTab }
                                .testTag("filter_${filterTab.name.lowercase()}")
                        ) {
                            Text(
                                text = "${filterTab.label} ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            val filteredRequests = rescueRequests.filter { req ->
                when (selectedFilter) {
                    RequestFilterTab.PENDING -> req.status == RegistrationApprovalStatus.PENDING
                    RequestFilterTab.APPROVED -> req.status == RegistrationApprovalStatus.APPROVED
                    RequestFilterTab.REJECTED -> req.status == RegistrationApprovalStatus.REJECTED
                    RequestFilterTab.ALL -> true
                }
            }

            if (filteredRequests.isEmpty()) {
                item {
                    val filterName = selectedFilter.label.lowercase()
                    EmptyStateCard(
                        icon = Icons.Default.Groups,
                        title = "No $filterName Requests",
                        description = if (selectedFilter == RequestFilterTab.PENDING) "All Rescue Team applicant requests have been reviewed." else "No requests found matching the '$filterName' filter.",
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(filteredRequests) { req ->
                    RescueRequestCard(
                        request = req,
                        onApprove = { approveTarget = req },
                        onReject = {
                            rejectTarget = req
                            rejectReasonInput = ""
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun RescueRequestCard(
    request: RescueRegistrationRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
            .testTag("rescue_request_card_${request.requestId}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Request ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = request.requestId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Approval Status Badge
                val (statusBg, statusFg, statusLabel) = when (request.status) {
                    RegistrationApprovalStatus.PENDING -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "PENDING REVIEW")
                    RegistrationApprovalStatus.APPROVED -> Triple(Color(0xFFE8F6ED), Color(0xFF00684A), "APPROVED")
                    RegistrationApprovalStatus.REJECTED -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), "REJECTED")
                }
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Applicant Full Name
            Text(
                text = request.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF0F172A)
            )

            // Contact & Details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = SecondarySlate,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = request.email,
                        fontSize = 13.sp,
                        color = Color(0xFF334155)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = SecondarySlate,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = request.phoneNumber.ifBlank { "Not provided" },
                        fontSize = 13.sp,
                        color = Color(0xFF334155)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SecondarySlate,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Submitted: ${request.createdAt}",
                        fontSize = 12.sp,
                        color = SecondarySlate
                    )
                }
            }

            // Review history if reviewed
            if (request.status == RegistrationApprovalStatus.APPROVED && request.reviewedBy != null) {
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Approved by ${request.reviewedBy} • ${request.reviewedAt ?: "Recently"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }

            if (request.status == RegistrationApprovalStatus.REJECTED) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Declined by ${request.reviewedBy ?: "Admin"} • ${request.reviewedAt ?: "Recently"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF991B1B)
                        )
                        if (!request.rejectionReason.isNullOrBlank()) {
                            Text(
                                text = "Reason: ${request.rejectionReason}",
                                fontSize = 12.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            }

            // Actions for Pending Requests
            if (request.status == RegistrationApprovalStatus.PENDING) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Approve Button
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("approve_rescue_btn_${request.requestId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Approve",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Reject Button
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        border = BorderStroke(1.dp, ErrorRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("reject_rescue_btn_${request.requestId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reject",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 5: REPORTS & ANALYTICS
// ---------------------------------------------------------------------------

@Composable
private fun AdminAnalyticsContent(
    allIncidents: List<Incident>
) {
    val totalCount = allIncidents.size
    val resolvedCount = allIncidents.count { it.currentStage == RescueStage.RESOLVED }
    val resolutionRate = if (totalCount > 0) ((resolvedCount.toFloat() / totalCount) * 100).toInt() else 0

    val canineCount = allIncidents.count { it.species.contains("Dog", ignoreCase = true) }
    val felineCount = allIncidents.count { it.species.contains("Cat", ignoreCase = true) }
    val avianCount = allIncidents.count { it.species.contains("Bird", ignoreCase = true) }

    val caninePct = if (totalCount > 0) ((canineCount.toFloat() / totalCount) * 100).toInt() else 0
    val felinePct = if (totalCount > 0) ((felineCount.toFloat() / totalCount) * 100).toInt() else 0
    val avianPct = if (totalCount > 0) ((avianCount.toFloat() / totalCount) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Text("Rescue Analytics & Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Operational efficiency metrics across municipal sectors", fontSize = 14.sp, color = SecondarySlate)
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Response Trends", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (totalCount == 0) {
                        Text("No incident records available to compute response statistics.", fontSize = 14.sp, color = SecondarySlate)
                    } else {
                        Text("Total Logged Reports: $totalCount Incidents", fontSize = 15.sp, color = SecondarySlate)
                        Text("Resolved Cases: $resolvedCount Rescues", fontSize = 15.sp, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                        Text("Resolution Rate: $resolutionRate%", fontSize = 15.sp, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Species Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (totalCount == 0) {
                        Text("No species data logged yet.", fontSize = 14.sp, color = SecondarySlate)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Canine: $caninePct% ($canineCount)", fontSize = 14.sp)
                            Text("Feline: $felinePct% ($felineCount)", fontSize = 14.sp)
                            Text("Avian: $avianPct% ($avianCount)", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
// TAB 6: SYSTEM SETTINGS (Compact & Clean Administration Console)
// ---------------------------------------------------------------------------

@Composable
private fun AdminSettingsContent(
    settings: com.example.model.AdminSystemSettings,
    session: UserSession,
    onNavigateToTeams: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onLogout: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val teamMembers by IncidentRepository.teamMembers.collectAsState()
    val citizenUsers by IncidentRepository.citizenUsers.collectAsState()

    var threshold by remember(settings.aiConfidenceThreshold) { mutableFloatStateOf(settings.aiConfidenceThreshold) }
    var autoVerify by remember(settings.autoVerifyCritical) { mutableStateOf(settings.autoVerifyCritical) }
    var dispatchRadius by remember(settings.dispatchRadiusKm) { mutableFloatStateOf(settings.dispatchRadiusKm.toFloat()) }
    var dupRadius by remember(settings.dispatchPolicy.duplicateRadiusMeters) { mutableFloatStateOf(settings.dispatchPolicy.duplicateRadiusMeters.toFloat()) }
    var dupWindow by remember(settings.dispatchPolicy.duplicateReviewWindowMinutes) { mutableFloatStateOf(settings.dispatchPolicy.duplicateReviewWindowMinutes.toFloat()) }

    // System Policies: 3 compact independently expandable rows
    var expandAi by remember { mutableStateOf(false) }
    var expandDispatch by remember { mutableStateOf(false) }
    var expandSchedule by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showAutoVerifyWarningDialog by remember { mutableStateOf(false) }
    var showAccountManageDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSecurityDetailsDialog by remember { mutableStateOf(false) }
    var showEditScheduleDialog by remember { mutableStateOf(false) }
    var showRolePermissionsDialog by remember { mutableStateOf(false) }
    var showAllAuditDialog by remember { mutableStateOf(false) }
    var showClearAuditDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var auditFilterAction by remember { mutableStateOf("ALL") }

    // Check if values differ from persisted settings
    val isDirty = threshold != settings.aiConfidenceThreshold ||
        autoVerify != settings.autoVerifyCritical ||
        dispatchRadius != settings.dispatchRadiusKm.toFloat() ||
        dupRadius != settings.dispatchPolicy.duplicateRadiusMeters.toFloat() ||
        dupWindow != settings.dispatchPolicy.duplicateReviewWindowMinutes.toFloat()

    // Confirmation for Auto-Verify Critical Trauma automation
    if (showAutoVerifyWarningDialog) {
        StandardConfirmationDialog(
            title = "Enable Automated Critical Trauma Verification?",
            message = "When active, distress reports identified by AI Vision with high severity will automatically enter the verified triage pipeline. Emergency dispatch assignments still require dispatcher review before squads are routed.",
            confirmLabel = "Enable Automation",
            cancelLabel = "Keep Manual Review",
            confirmColor = TertiaryBlue,
            onConfirm = {
                autoVerify = true
                showAutoVerifyWarningDialog = false
            },
            onDismiss = { showAutoVerifyWarningDialog = false }
        )
    }

    // Reset Defaults Confirmation Dialog
    if (showResetConfirmDialog) {
        StandardConfirmationDialog(
            title = "Reset System Settings to Defaults?",
            message = "This will revert the AI verification threshold to 85%, automated critical trauma triage to enabled, dispatch radius to 15km, duplicate detection radius to 200m, and review window to 30 minutes.",
            confirmLabel = "Reset to Defaults",
            cancelLabel = "Cancel",
            confirmColor = TertiaryBlue,
            onConfirm = {
                threshold = 0.85f
                autoVerify = true
                dispatchRadius = 15.0f
                dupRadius = 200.0f
                dupWindow = 30.0f
                showResetConfirmDialog = false
                IncidentRepository.updateSystemSettings(0.85f, true, 15.0, dupRadius = 200, reviewWindow = 30)
                infoMessage = "System settings reset to standard municipal SAR defaults."
            },
            onDismiss = { showResetConfirmDialog = false }
        )
    }

    // Clear Audit Log Confirmation Dialog
    if (showClearAuditDialog) {
        StandardConfirmationDialog(
            title = "Archive & Clear Audit Trail?",
            message = "Are you sure you want to purge recent administrative log entries? A new entry documenting this purge will be recorded to maintain forensic integrity.",
            confirmLabel = "Clear & Archive",
            cancelLabel = "Cancel",
            isDestructive = true,
            confirmColor = ErrorRed,
            onConfirm = {
                IncidentRepository.clearAuditLog()
                showClearAuditDialog = false
                infoMessage = "Audit log purged. Archive entry recorded."
            },
            onDismiss = { showClearAuditDialog = false }
        )
    }

    // Manage Account Modal Dialog (Unified, Clean Hub)
    if (showAccountManageDialog) {
        Dialog(onDismissRequest = { showAccountManageDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Manage Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAccountManageDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        "Manage administrator security credentials and contact settings.",
                        fontSize = 13.sp,
                        color = SecondarySlate
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerLow),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAccountManageDialog = false
                                    showPasswordDialog = true
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(18.dp))
                                Text("Change Password", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(12.dp))
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAccountManageDialog = false
                                    showEditProfileDialog = true
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(18.dp))
                                Text("Edit Profile & Phone", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(12.dp))
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAccountManageDialog = false
                                    showSecurityDetailsDialog = true
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(18.dp))
                                Text("Security & Clearance Details", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(12.dp))
                        }
                    }

                    Button(
                        onClick = { showAccountManageDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Edit Operating Schedule Dialog
    if (showEditScheduleDialog) {
        Dialog(onDismissRequest = { showEditScheduleDialog = false }) {
            var hoursText by remember { mutableStateOf(settings.dispatchPolicy.operatingHours) }
            var hotlineText by remember { mutableStateOf(settings.emergencyHotline) }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Edit Operating Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showEditScheduleDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text("Configure public emergency operating schedule and central dispatch hotline.", fontSize = 13.sp, color = SecondarySlate)

                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it },
                        label = { Text("Operating Schedule") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hotlineText,
                        onValueChange = { hotlineText = it },
                        label = { Text("Emergency Dispatch Hotline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showEditScheduleDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                IncidentRepository.updateOperatingSchedule(hoursText, hotlineText)
                                showEditScheduleDialog = false
                                infoMessage = "Operating schedule updated successfully."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
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

    // Role Permissions & Security Matrix Dialog
    if (showRolePermissionsDialog) {
        Dialog(onDismissRequest = { showRolePermissionsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Permissions & Access", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showRolePermissionsDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("Super Administrator (HQ Ops)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TertiaryBlue)
                            Text("Full privileges: Live dispatch override, AI triage criteria, responder provisioning, account moderation, and compliance auditing.", fontSize = 11.sp, color = SecondarySlate)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("Rescue Field Responder (Units)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            Text("Operational privileges: Telemetry reporting, incident status updates, medical equipment checkouts, and GPS ambulance tracking.", fontSize = 11.sp, color = SecondarySlate)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("Citizen Reporter (Public)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Public privileges: Animal distress intake, photo evidence upload, location tagging, status notifications, and personal profile preferences.", fontSize = 11.sp, color = SecondarySlate)
                        }
                    }

                    Button(
                        onClick = { showRolePermissionsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // View All Recent Audit Activity Dialog
    if (showAllAuditDialog) {
        val exportText = buildString {
            appendLine("=== PAWRESCUE ENTERPRISE SAR AUDIT TRAIL ===")
            appendLine("Organization: ${settings.organizationName}")
            appendLine("Exported By: ${session.name} (${session.role.title})")
            appendLine("Total Entries: ${settings.auditHistory.size}")
            appendLine("--------------------------------------------")
            settings.auditHistory.forEachIndexed { idx, item ->
                appendLine("[${idx + 1}] ${item.timestamp} | ${item.action}")
                appendLine("    Actor: ${item.adminName} | Target: ${item.targetRecord} | Result: ${item.outcome}")
                appendLine("    Detail: ${item.detail}")
            }
            appendLine("============================================")
        }

        Dialog(onDismissRequest = { showAllAuditDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Administrative Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAllAuditDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Action buttons (Copy / Clear)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(exportText))
                                infoMessage = "Audit log copied to clipboard."
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp), tint = TertiaryBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Log", fontSize = 11.sp, color = TertiaryBlue, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showClearAuditDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(13.dp), tint = ErrorRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear Log", fontSize = 11.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Filter chips
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL" to "All", "SETTINGS" to "Settings", "FLEET" to "Fleet", "POLICY" to "Policy").forEach { (filterKey, label) ->
                            val isSelected = auditFilterAction == filterKey
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) TertiaryBlue else SurfaceContainerLow)
                                    .clickable { auditFilterAction = filterKey }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    val filteredAudits = remember(settings.auditHistory, auditFilterAction) {
                        when (auditFilterAction) {
                            "SETTINGS" -> settings.auditHistory.filter { it.action.contains("Settings", ignoreCase = true) || it.action.contains("Threshold", ignoreCase = true) }
                            "FLEET" -> settings.auditHistory.filter { it.action.contains("Fleet", ignoreCase = true) || it.action.contains("Unit", ignoreCase = true) }
                            "POLICY" -> settings.auditHistory.filter { it.action.contains("Policy", ignoreCase = true) }
                            else -> settings.auditHistory
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (filteredAudits.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                                    Text("No audit events match selected filter.", fontSize = 12.sp, color = SecondarySlate)
                                }
                            }
                        } else {
                            items(filteredAudits.size) { index ->
                                val audit = filteredAudits[index]
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainerLow)
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(audit.action, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TertiaryBlue)
                                        Text(audit.timestamp, fontSize = 10.sp, color = SecondarySlate)
                                    }
                                    Text(audit.detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Actor: ${audit.adminName} • Outcome: ${audit.outcome}", fontSize = 10.sp, color = SecondarySlate)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showAllAuditDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Edit Admin Profile Dialog
    if (showEditProfileDialog) {
        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            var editName by remember { mutableStateOf(session.name) }
            var editPhone by remember { mutableStateOf(session.phone) }
            var formError by remember { mutableStateOf<String?>(null) }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Edit Admin Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showEditProfileDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text("Update administrator contact details. Role permissions and organization ownership are locked.", fontSize = 13.sp, color = SecondarySlate)

                    if (formError != null) {
                        Text(formError ?: "", fontSize = 12.sp, color = ErrorRed)
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it; formError = null },
                        label = { Text("Administrator Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = session.email,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Organization Email (Read-Only)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it; formError = null },
                        label = { Text("Contact Phone / Hotdesk") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showEditProfileDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (editName.isBlank()) {
                                    formError = "Name cannot be empty"
                                } else {
                                    val updated = IncidentRepository.updateUserProfile(
                                        userId = session.userId,
                                        newName = editName,
                                        newEmail = session.email,
                                        newPhone = editPhone
                                    )
                                    if (updated) {
                                        showEditProfileDialog = false
                                        infoMessage = "Administrator contact details updated successfully."
                                    } else {
                                        formError = "Failed to update profile record."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
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

    // Account Security & Cryptographic Details Dialog
    if (showSecurityDetailsDialog) {
        Dialog(onDismissRequest = { showSecurityDetailsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Security Clearance", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showSecurityDetailsDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(18.dp))
                            Text("Enterprise Clearance Active", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TertiaryBlue)
                        }
                        Text("Role Clearance: Super Administrator (${session.role.title})", fontSize = 12.sp, color = SecondarySlate)
                        Text("Network Scope: ${settings.organizationName}", fontSize = 12.sp, color = SecondarySlate)
                        Text("Transport Layer Security: TLS 1.3 / AES-256-GCM Encrypted", fontSize = 12.sp, color = SecondarySlate)
                    }

                    Button(
                        onClick = { showSecurityDetailsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Admin Password Dialog
    if (showPasswordDialog) {
        Dialog(onDismissRequest = { showPasswordDialog = false }) {
            var currPass by remember { mutableStateOf("") }
            var newPass by remember { mutableStateOf("") }
            var error by remember { mutableStateOf<String?>(null) }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Change Admin Password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showPasswordDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    if (error != null) {
                        Text(error ?: "", fontSize = 12.sp, color = ErrorRed)
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
                        OutlinedButton(onClick = { showPasswordDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val res = IncidentRepository.changePassword(session.userId, currPass, newPass)
                                if (res.isSuccess) {
                                    showPasswordDialog = false
                                    infoMessage = "Administrator password updated successfully."
                                } else {
                                    error = res.exceptionOrNull()?.message ?: "Failed to change password"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
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

    // Admin Logout confirmation dialog
    if (showLogoutDialog) {
        StandardConfirmationDialog(
            title = "Sign Out from Admin Console?",
            message = "You will be signed out from the Organization Admin console. Active incident triage and squad telemetry will continue operating on the network.",
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

    if (infoMessage != null) {
        AlertDialog(
            onDismissRequest = { infoMessage = null },
            title = { Text("System Notice", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Text(infoMessage ?: "", fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = { infoMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue)
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
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // -------------------------------------------------------------------
        // A. COMPACT ADMIN IDENTITY CARD (140–180dp tall, clean proportions)
        // -------------------------------------------------------------------
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_profile_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Admin Icon + Name/Org + Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = TertiaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = session.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = settings.organizationName,
                                    fontSize = 12.sp,
                                    color = TertiaryBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }

                        // Badges
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("SUPER ADMIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TertiaryBlue)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("STATUS: ${session.status.name}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            }
                        }
                    }

                    // Row 2: Email compact row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(14.dp))
                        Text(session.email, fontSize = 12.sp, color = SecondarySlate, fontWeight = FontWeight.Medium)
                        if (session.phone.isNotBlank()) {
                            Text("•", fontSize = 12.sp, color = SecondarySlate)
                            Text(session.phone, fontSize = 12.sp, color = SecondarySlate)
                        }
                    }

                    // Row 3: Manage Account Action Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showAccountManageDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(13.dp), tint = TertiaryBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manage Account", fontSize = 11.sp, color = TertiaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------
        // B. SYSTEM POLICIES (One Section Heading, 3 Expandable Rows)
        // -------------------------------------------------------------------
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "System Policies",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 2.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. AI Verification Row
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { expandAi = !expandAi }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEFF6FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(16.dp))
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                        Text("AI Verification", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("${(threshold * 100).toInt()}% threshold • Critical ${if (autoVerify) "Auto" else "Manual"}", fontSize = 13.sp, color = SecondarySlate)
                                    }
                                }

                                Icon(
                                    imageVector = if (expandAi) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = SecondarySlate,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            AnimatedVisibility(visible = expandAi) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Confidence Threshold", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text("${(threshold * 100).toInt()}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TertiaryBlue)
                                    }

                                    Slider(
                                        value = threshold,
                                        onValueChange = { threshold = it },
                                        valueRange = 0.50f..0.99f,
                                        colors = SliderDefaults.colors(thumbColor = TertiaryBlue, activeTrackColor = TertiaryBlue)
                                    )

                                    Text(
                                        "Distress reports scoring below this confidence value are held for manual dispatcher review.",
                                        fontSize = 12.sp,
                                        color = SecondarySlate
                                    )

                                    SettingsSwitchRow(
                                        title = "Critical Trauma Auto-Triage",
                                        subtitle = "Auto-elevates acute distress cases into active dispatch pipeline",
                                        checked = autoVerify,
                                        onCheckedChange = { nextVal ->
                                            if (nextVal) {
                                                showAutoVerifyWarningDialog = true
                                            } else {
                                                autoVerify = false
                                            }
                                        },
                                        icon = null
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // 2. Dispatch Rules Row
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { expandDispatch = !expandDispatch }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEFF6FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Explore, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(16.dp))
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                        Text("Dispatch Rules", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("${dispatchRadius.toInt()} km radius • ${dupRadius.toInt()}m duplicate alert", fontSize = 13.sp, color = SecondarySlate)
                                    }
                                }

                                Icon(
                                    imageVector = if (expandDispatch) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = SecondarySlate,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            AnimatedVisibility(visible = expandDispatch) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Max radius
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Max Dispatch Radius", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text("${dispatchRadius.toInt()} km", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TertiaryBlue)
                                    }
                                    Slider(
                                        value = dispatchRadius,
                                        onValueChange = { dispatchRadius = it },
                                        valueRange = 5f..50f,
                                        colors = SliderDefaults.colors(thumbColor = TertiaryBlue, activeTrackColor = TertiaryBlue)
                                    )

                                    // Dup radius
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Duplicate Detection Radius", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text("${dupRadius.toInt()} m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TertiaryBlue)
                                    }
                                    Slider(
                                        value = dupRadius,
                                        onValueChange = { dupRadius = it },
                                        valueRange = 50f..1000f,
                                        colors = SliderDefaults.colors(thumbColor = TertiaryBlue, activeTrackColor = TertiaryBlue)
                                    )

                                    // Dup window
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Duplicate Review Window", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text("${dupWindow.toInt()} min", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TertiaryBlue)
                                    }
                                    Slider(
                                        value = dupWindow,
                                        onValueChange = { dupWindow = it },
                                        valueRange = 10f..120f,
                                        colors = SliderDefaults.colors(thumbColor = TertiaryBlue, activeTrackColor = TertiaryBlue)
                                    )

                                    Text(
                                        "Triage Policy: ${settings.dispatchPolicy.priorityDefinition}",
                                        fontSize = 12.sp,
                                        color = SecondarySlate
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // 3. Operating Schedule Row
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { expandSchedule = !expandSchedule }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEFF6FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(16.dp))
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                        Text("Operating Schedule", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(settings.dispatchPolicy.operatingHours, fontSize = 13.sp, color = SecondarySlate)
                                    }
                                }

                                Icon(
                                    imageVector = if (expandSchedule) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = SecondarySlate,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            AnimatedVisibility(visible = expandSchedule) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Active Schedule: ${settings.dispatchPolicy.operatingHours}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Emergency Hotline: ${settings.emergencyHotline}", fontSize = 13.sp, color = SecondarySlate)

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        OutlinedButton(
                                            onClick = { showEditScheduleDialog = true },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = TertiaryBlue)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit Schedule", fontSize = 11.sp, color = TertiaryBlue, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------
        // C. TEAM & ACCOUNT MANAGEMENT (3 Clean Navigation Rows in 1 Card)
        // -------------------------------------------------------------------
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Team & Account Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 2.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Rescue Teams & Fleet
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToTeams() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(16.dp))
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text("Rescue Teams & Fleet", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Provision responders and ambulance units (${teamMembers.size} squads)", fontSize = 13.sp, color = SecondarySlate)
                                }
                            }

                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(14.dp))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // 2. Citizen Accounts
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToUsers() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(16.dp))
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text("Citizen Accounts", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Review reporter accounts and moderation (${citizenUsers.size} accounts)", fontSize = 13.sp, color = SecondarySlate)
                                }
                            }

                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(14.dp))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // 3. Permissions & Access
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showRolePermissionsDialog = true }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(16.dp))
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text("Permissions & Access", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Review role clearance matrix and network policies", fontSize = 13.sp, color = SecondarySlate)
                                }
                            }

                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------
        // D. CONFIGURATION STATUS (Compact Footer Card with Two Clean Buttons)
        // -------------------------------------------------------------------
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDirty) Color(0xFFEFF6FF) else SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isDirty) "Unsaved Changes" else "Configuration Saved",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDirty) TertiaryBlue else Color(0xFF166534)
                            )
                            Text(
                                text = if (isDirty) "Review modifications before persisting" else "Last updated: ${settings.lastUpdatedTimestamp}",
                                fontSize = 12.sp,
                                color = SecondarySlate
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDirty) Color(0xFFFEF3C7) else Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isDirty) "● Unsaved" else "● Up to date",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDirty) Color(0xFFB45309) else Color(0xFF166534)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Defaults", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                isSaving = true
                                IncidentRepository.updateSystemSettings(
                                    threshold = threshold,
                                    autoVerify = autoVerify,
                                    radius = dispatchRadius.toDouble(),
                                    dupRadius = dupRadius.toInt(),
                                    reviewWindow = dupWindow.toInt()
                                )
                                isSaving = false
                                infoMessage = "System settings saved successfully."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            enabled = isDirty && !isSaving,
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------
        // E. RECENT ADMINISTRATIVE ACTIVITY (Max 3 events, View All modal)
        // -------------------------------------------------------------------
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { showAllAuditDialog = true },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("View All ›", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TertiaryBlue)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (settings.auditHistory.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No recent administrative activity logged.", fontSize = 13.sp, color = SecondarySlate)
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val recentThree = settings.auditHistory.take(3)
                            recentThree.forEachIndexed { index, audit ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(audit.action, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(audit.timestamp, fontSize = 11.sp, color = SecondarySlate)
                                    }
                                    Text(audit.detail, fontSize = 12.sp, color = SecondarySlate, maxLines = 1)
                                }
                                if (index < recentThree.lastIndex) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------
        // F. ADMIN SIGN OUT BUTTON (Compact & Centered)
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
                    testTag = "admin_logout_button"
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ---------------------------------------------------------------------------
// ASSIGN RESCUE TEAM DIALOG
// ---------------------------------------------------------------------------

@Composable
private fun AssignRescueTeamDialog(
    incidentId: String,
    availableTeams: List<RescueTeamMember>,
    onDismiss: () -> Unit,
    onAssign: (String, String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Assign Rescue Squad", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text("Dispatching squad for incident $incidentId", fontSize = 14.sp, color = SecondarySlate)

                LazyColumn(modifier = Modifier.height(240.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableTeams) { team ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAssign(team.teamId, team.teamName) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(team.teamName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("${team.name} • ${team.vehicleId}", fontSize = 13.sp, color = SecondarySlate)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryFixed)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Assign", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002113))
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
// ADD TEAM MEMBER DIALOG
// ---------------------------------------------------------------------------

@Composable
private fun AddTeamMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("Unit Omega-7 (Mobile Vet)") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Provision Rescue Team Member", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Member Name", fontSize = 14.sp) },
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Official Email", fontSize = 14.sp) },
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontSize = 14.sp) },
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Squad Unit Name", fontSize = 14.sp) },
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (name.isNotEmpty() && email.isNotEmpty()) {
                            onAdd(name, email, phone, teamName)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TertiaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Create Team Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
