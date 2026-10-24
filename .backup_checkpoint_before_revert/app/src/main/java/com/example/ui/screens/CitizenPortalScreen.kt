package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.core.content.ContextCompat
import com.example.data.IncidentRepository
import com.example.model.CitizenFeedback
import com.example.model.CitizenNotification
import com.example.model.CitizenPreferences
import com.example.ui.components.CompactSignOutButton
import com.example.ui.components.SettingsActionRow
import com.example.ui.components.SettingsSectionCard
import com.example.ui.components.SettingsSwitchRow
import com.example.ui.components.StandardConfirmationDialog
import com.example.model.ImageHotlinks
import com.example.model.Incident
import com.example.model.RescueStage
import com.example.model.UrgencyLevel
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.components.AppCard
import com.example.ui.components.AppStatCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StrayCareHeader
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnPrimaryContainer
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.PrimaryFixedDim
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SecondarySlate
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TertiaryBlue

enum class CitizenTab(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Radar, "citizen_tab_dashboard"),
    REPORT_ANIMAL("Report", Icons.Default.AddAPhoto, "citizen_tab_report"),
    MY_REPORTS("My Reports", Icons.Default.Description, "citizen_tab_my_reports"),
    NOTIFICATIONS("Alerts", Icons.Default.Notifications, "citizen_tab_notifications"),
    PROFILE("Profile", Icons.Default.Person, "citizen_tab_profile")
}

@Composable
fun CitizenPortalScreen(
    session: UserSession,
    onLogout: () -> Unit,
    onSosClick: () -> Unit,
    onViewIncidentDetails: (String) -> Unit
) {
    // STRICT ROLE GUARD: Access granted ONLY to CITIZEN role
    if (session.role != UserRole.CITIZEN) {
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
                    "This area is restricted to Citizens. Your account role is '${session.role.title}'.",
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

    var selectedTab by remember { mutableStateOf(CitizenTab.DASHBOARD) }
    var showFeedbackDialogForId by remember { mutableStateOf<String?>(null) }
    val allIncidents by IncidentRepository.incidents.collectAsState()
    val notifications by IncidentRepository.notifications.collectAsState()

    // Citizen sees strictly their own reports in "My Reports"
    val myReports = remember(allIncidents, session.userId) {
        allIncidents.filter { it.reporterId == session.userId }
    }

    val myNotifications = remember(notifications, session.userId) {
        notifications.filter { it.userId == session.userId || myReports.any { r -> r.id == it.incidentId } }
    }

    Scaffold(
        topBar = {
            StrayCareHeader(
                title = "PawRescue",
                subtitle = when (selectedTab) {
                    CitizenTab.DASHBOARD -> "Community Rescue Stream"
                    CitizenTab.REPORT_ANIMAL -> "Report Animal Distress"
                    CitizenTab.MY_REPORTS -> "My Submitted Reports (${myReports.size})"
                    CitizenTab.NOTIFICATIONS -> "Status Updates & Alerts"
                    CitizenTab.PROFILE -> "Account & Settings"
                },
                showBackButton = false,
                onSosClick = onSosClick,
                onProfileClick = { selectedTab = CitizenTab.PROFILE },
                onLogoutClick = onLogout
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceContainerLowest,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().testTag("citizen_bottom_nav")
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
                    CitizenTab.entries.forEach { tab ->
                        val isSelected = tab == selectedTab
                        val tint = if (isSelected) PrimaryEmerald else SecondarySlate
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
                                                color = PrimaryEmerald.copy(alpha = 0.12f),
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
                CitizenTab.DASHBOARD -> {
                    CitizenDashboardContent(
                        session = session,
                        myReports = myReports,
                        allIncidents = allIncidents,
                        onReportAnimalClick = { selectedTab = CitizenTab.REPORT_ANIMAL },
                        onViewMyReportsClick = { selectedTab = CitizenTab.MY_REPORTS },
                        onViewIncident = onViewIncidentDetails,
                        onSosClick = onSosClick
                    )
                }

                CitizenTab.REPORT_ANIMAL -> {
                    NewIncidentIntakeScreen(
                        onNavigateBack = { selectedTab = CitizenTab.DASHBOARD },
                        onSubmitSuccess = { newId ->
                            selectedTab = CitizenTab.MY_REPORTS
                        },
                        onSosClick = onSosClick,
                        onProfileClick = { selectedTab = CitizenTab.PROFILE }
                    )
                }

                CitizenTab.MY_REPORTS -> {
                    CitizenMyReportsContent(
                        myReports = myReports,
                        onViewDetails = onViewIncidentDetails,
                        onOpenFeedback = { id -> showFeedbackDialogForId = id },
                        onNewReportClick = { selectedTab = CitizenTab.REPORT_ANIMAL }
                    )
                }

                CitizenTab.NOTIFICATIONS -> {
                    CitizenNotificationsContent(notifications = myNotifications, onViewIncident = onViewIncidentDetails)
                }

                CitizenTab.PROFILE -> {
                    CitizenProfileContent(
                        session = session,
                        onNavigateToMyReports = { selectedTab = CitizenTab.MY_REPORTS },
                        onLogout = onLogout
                    )
                }
            }

            // Feedback Dialog for Completed Reports
            showFeedbackDialogForId?.let { incidentId ->
                FeedbackSubmissionDialog(
                    incidentId = incidentId,
                    onDismiss = { showFeedbackDialogForId = null },
                    onSubmit = { rating, comment ->
                        IncidentRepository.submitCitizenFeedback(
                            CitizenFeedback(
                                incidentId = incidentId,
                                rating = rating,
                                comment = comment,
                                submittedAt = "Just now"
                            )
                        )
                        showFeedbackDialogForId = null
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 0: CITIZEN DASHBOARD CONTENT
// ---------------------------------------------------------------------------

@Composable
private fun CitizenDashboardContent(
    session: UserSession,
    myReports: List<Incident>,
    allIncidents: List<Incident>,
    onReportAnimalClick: () -> Unit,
    onViewMyReportsClick: () -> Unit,
    onViewIncident: (String) -> Unit,
    onSosClick: () -> Unit
) {
    val myActiveRescue = myReports.firstOrNull { it.currentStage != RescueStage.RESOLVED }
    val activeReportsCount = myReports.count { it.currentStage != RescueStage.RESOLVED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Citizen Welcome Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Welcome, ${session.name}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnPrimaryContainer)
                    Text("Role: Citizen • Verified Reporter", fontSize = 14.sp, color = OnPrimaryContainer.copy(alpha = 0.85f))
                }
            }
        }

        // Action Hero: Report Animal CTA (Primary action)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, PrimaryEmerald.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable(onClick = onReportAnimalClick)
                    .testTag("citizen_report_animal_cta")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Report Animal", tint = OnPrimaryContainer, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Report Stray Animal", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ErrorContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("FAST DISPATCH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                        }
                        Text("Upload photos with instant AI verification & live telemetry tracking", fontSize = 14.sp, color = SecondarySlate)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PrimaryEmerald)
                }
            }
        }

        // Stats Glance (True counts based on actual database records)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    modifier = Modifier.weight(1f).clickable(onClick = onViewMyReportsClick)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("My Reports", fontSize = 13.sp, color = SecondarySlate, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${myReports.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Submitted by you", fontSize = 12.sp, color = SecondarySlate)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    modifier = Modifier.weight(1f).clickable(onClick = onViewMyReportsClick)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Reports", fontSize = 13.sp, color = SecondarySlate, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$activeReportsCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (activeReportsCount > 0) ErrorRed else PrimaryEmerald)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("In progress", fontSize = 12.sp, color = SecondarySlate)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Recent Reports", fontSize = 13.sp, color = SecondarySlate, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${allIncidents.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Area records", fontSize = 12.sp, color = SecondarySlate)
                    }
                }
            }
        }

        // Empty Hero State if user has zero reports
        if (myReports.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Pets,
                    title = "No Reports Yet",
                    description = "You haven't submitted an animal rescue report yet. Spot an injured animal to dispatch help.",
                    actionButtonText = "Report Stray Animal",
                    actionButtonIcon = Icons.Default.Add,
                    onActionClick = onReportAnimalClick,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Live Rescue Updates Tracker Card
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Live Rescue Tracking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (myActiveRescue != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(SurfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE MISSION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
                        }
                    }
                }

                if (myActiveRescue == null) {
                    EmptyStateCard(
                        icon = Icons.Default.CheckCircle,
                        title = "No Active Rescue Mission",
                        description = "You currently have no active rescue operations in progress."
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViewIncident(myActiveRescue.id) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SurfaceContainerHigh)
                                    ) {
                                        if (myActiveRescue.imageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = myActiveRescue.imageUrl,
                                                contentDescription = myActiveRescue.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(Icons.Default.Pets, contentDescription = null, tint = SecondarySlate, modifier = Modifier.align(Alignment.Center).size(24.dp))
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(myActiveRescue.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Text("${myActiveRescue.species} • ${myActiveRescue.address}", fontSize = 14.sp, color = SecondarySlate)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ErrorContainer)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(myActiveRescue.urgency.badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                                }
                            }

                            // Progress Step indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val currentStep = myActiveRescue.currentStage.stepNumber
                                (1..9).forEach { step ->
                                    val isDone = step <= currentStep
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isDone) PrimaryEmerald else SurfaceContainerHigh)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: ${myActiveRescue.currentStage.title}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryEmerald
                                )
                                Text(
                                    text = "Assigned: ${myActiveRescue.assignedUnit}",
                                    fontSize = 14.sp,
                                    color = SecondarySlate
                                )
                            }
                        }
                    }
                }
            }
        }

        // Community Recent Reports stream
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recent Area Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (allIncidents.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Default.LocationOn,
                        title = "No Recent Reports Available",
                        description = "No stray animal rescue reports have been submitted in this area yet."
                    )
                } else {
                    allIncidents.take(3).forEach { inc ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewIncident(inc.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceContainerHigh)
                                ) {
                                    if (inc.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = inc.imageUrl,
                                            contentDescription = inc.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Pets, contentDescription = null, tint = SecondarySlate, modifier = Modifier.align(Alignment.Center).size(20.dp))
                                    }
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(inc.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("${inc.address} • ${inc.timestamp}", fontSize = 14.sp, color = SecondarySlate)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceContainerHigh)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(inc.currentStage.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
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
// TAB 2: CITIZEN MY REPORTS (Own reports only)
// ---------------------------------------------------------------------------

@Composable
private fun CitizenMyReportsContent(
    myReports: List<Incident>,
    onViewDetails: (String) -> Unit,
    onOpenFeedback: (String) -> Unit,
    onNewReportClick: () -> Unit
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
                    Text("My Rescue Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Tracking ${myReports.size} reports logged by your account", fontSize = 14.sp, color = SecondarySlate)
                }
                Button(
                    onClick = onNewReportClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (myReports.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Pets,
                    title = "No Reports Submitted Yet",
                    description = "Spot an injured stray animal? Report it immediately to dispatch our closest rescue ambulance.",
                    actionButtonText = "Report Animal Distress",
                    actionButtonIcon = Icons.Default.Add,
                    onActionClick = onNewReportClick,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(myReports) { incident ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewDetails(incident.id) }
                        .testTag("citizen_report_${incident.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(incident.id, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
                            Text(incident.timestamp, fontSize = 13.sp, color = SecondarySlate)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerHigh)
                            ) {
                                AsyncImage(
                                    model = incident.imageUrl,
                                    contentDescription = incident.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(incident.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("${incident.species} • ${incident.address}", fontSize = 14.sp, color = SecondarySlate)
                                Text("Assigned: ${incident.assignedUnit}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Current Stage Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (incident.currentStage == RescueStage.RESOLVED) PrimaryFixed else SurfaceContainerHigh)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Status: ${incident.currentStage.title}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (incident.currentStage == RescueStage.RESOLVED) Color(0xFF002113) else PrimaryEmerald
                                )
                            }

                            // If resolved, allow feedback
                            if (incident.currentStage == RescueStage.RESOLVED) {
                                if (incident.feedback != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Rated: ", fontSize = 11.sp, color = SecondarySlate)
                                        (1..incident.feedback.rating).forEach {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onOpenFeedback(incident.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryEmerald)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Submit Feedback", fontSize = 11.sp, color = PrimaryEmerald)
                                    }
                                }
                            } else {
                                TextButton(onClick = { onViewDetails(incident.id) }) {
                                    Text("View Live Timeline →", fontSize = 11.sp, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
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
// TAB 3: CITIZEN NOTIFICATIONS
// ---------------------------------------------------------------------------

@Composable
private fun CitizenNotificationsContent(
    notifications: List<CitizenNotification>,
    onViewIncident: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item {
            Text("Rescue Status Updates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Live notifications regarding your reports and community alerts", fontSize = 14.sp, color = SecondarySlate)
        }

        if (notifications.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Notifications,
                    title = "No Notifications Yet",
                    description = "You have no status updates or alerts at this time.",
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(notifications) { notif ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            notif.incidentId?.let { onViewIncident(it) }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(notif.time, fontSize = 12.sp, color = SecondarySlate)
                            }
                            Text(notif.message, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (notif.incidentId != null) {
                                Text("Case Ref: ${notif.incidentId} • Tap to view", fontSize = 13.sp, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
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
// TAB 4: CITIZEN PROFILE & SETTINGS (Redesigned, Clean & Compact)
// ---------------------------------------------------------------------------

@Composable
private fun CitizenProfileContent(
    session: UserSession,
    onNavigateToMyReports: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val savedPreferences by IncidentRepository.citizenPreferences.collectAsState()
    val allIncidents by IncidentRepository.incidents.collectAsState()

    // Real citizen reporting metrics
    val myReports = remember(allIncidents, session.userId, session.name) {
        allIncidents.filter {
            it.reporterId == session.userId ||
                (it.reporterName.isNotBlank() && it.reporterName.equals(session.name, ignoreCase = true))
        }
    }
    val totalSubmitted = myReports.size
    val activeResponses = myReports.count { it.currentStage != RescueStage.RESOLVED }
    val animalsRescued = myReports.count { it.currentStage == RescueStage.RESOLVED }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // Real device location permission check
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // 1. Edit Profile Dialog
    if (showEditProfileDialog) {
        EditCitizenProfileDialog(
            currentName = session.name,
            currentEmail = session.email,
            currentPhone = session.phone,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName, newEmail, newPhone ->
                val success = IncidentRepository.updateUserProfile(session.userId, newName, newEmail, newPhone)
                if (success) {
                    showEditProfileDialog = false
                    infoMessage = "Profile updated successfully."
                } else {
                    infoMessage = "Failed to update profile. Please verify your details."
                }
            }
        )
    }

    // 2. Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            userId = session.userId,
            onDismiss = { showChangePasswordDialog = false },
            onSuccess = { msg ->
                showChangePasswordDialog = false
                infoMessage = msg
            }
        )
    }

    // 3. Privacy Policy Dialog
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    // 4. Terms of Service Dialog
    if (showTermsDialog) {
        TermsOfServiceDialog(onDismiss = { showTermsDialog = false })
    }

    // 5. Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        StandardConfirmationDialog(
            title = "Delete Citizen Account?",
            message = "This action will permanently remove your login credentials from PawRescue. Any previous rescue emergency reports you authored will remain archived in the municipal safety database for veterinary audits.",
            confirmLabel = "Delete My Account",
            cancelLabel = "Keep Account",
            isDestructive = true,
            onConfirm = {
                showDeleteAccountDialog = false
                IncidentRepository.deleteCitizenAccount(session.userId)
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    // 6. Logout Confirmation Dialog
    if (showLogoutDialog) {
        StandardConfirmationDialog(
            title = "Sign Out from Citizen Portal?",
            message = "You will be signed out of your account on this device. You will need to log in again to submit distress reports or monitor live response units.",
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

    // Notice alert dialog
    if (infoMessage != null) {
        AlertDialog(
            onDismissRequest = { infoMessage = null },
            title = { Text("Account Notice", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Text(infoMessage ?: "", fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = { infoMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // -------------------------------------------------------------------
        // A. COMPACT PROFILE IDENTITY CARD
        // -------------------------------------------------------------------
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("citizen_profile_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top Row: Avatar + Name/Badges + Edit Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Moderate-sized avatar with initials (54.dp)
                            val initials = session.name.split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .map { it.first().uppercaseChar() }
                                .joinToString("")
                                .ifEmpty { "CR" }

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryFixed.copy(alpha = 0.5f))
                                    .border(1.5.dp, PrimaryEmerald.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryEmerald
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = session.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Role badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PrimaryFixedDim.copy(alpha = 0.35f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "CITIZEN RESPONDER",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryEmerald
                                        )
                                    }

                                    // Status badge
                                    val isActive = session.status == com.example.model.AccountStatus.ACTIVE
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isActive) "ACTIVE" else session.status.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) Color(0xFF166534) else ErrorRed
                                        )
                                    }

                                    // Verified status indicator
                                    if (isActive) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Account",
                                            tint = PrimaryEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Compact Edit Profile Button (min touch target 48dp)
                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PrimaryEmerald),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("edit_citizen_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                modifier = Modifier.size(15.dp),
                                tint = PrimaryEmerald
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryEmerald
                            )
                        }
                    }

                    // Contact details container (Email & Phone in a clean light-grey container)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Email Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = SecondarySlate,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = session.email,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "Primary Login",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TertiaryBlue
                            )
                        }

                        // Phone Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = SecondarySlate,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (session.phone.isNotBlank()) session.phone else "No mobile phone added",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = if (session.phone.isNotBlank()) MaterialTheme.colorScheme.onSurface else SecondarySlate
                                )
                            }
                            if (session.phone.isBlank()) {
                                Text(
                                    text = "Tap Edit to Add",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SecondarySlate
                                )
                            } else {
                                Text(
                                    text = "Dispatch Contact",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------------
        // B. COMMUNITY IMPACT & REPORTING SUMMARY
        // -------------------------------------------------------------------
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Community Impact",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Distress reports authored and animal lives saved",
                                fontSize = 13.sp,
                                color = SecondarySlate
                            )
                        }

                        TextButton(
                            onClick = onNavigateToMyReports,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = "View Reports",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryEmerald
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = PrimaryEmerald
                            )
                        }
                    }

                    // 3 Aligned Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CitizenMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Submitted",
                            count = totalSubmitted.toString(),
                            icon = Icons.Default.Description,
                            accentColor = TertiaryBlue
                        )

                        CitizenMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "In Action",
                            count = activeResponses.toString(),
                            icon = Icons.Default.Radar,
                            accentColor = Color(0xFFD97706)
                        )

                        CitizenMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Rescued",
                            count = animalsRescued.toString(),
                            icon = Icons.Default.Pets,
                            accentColor = Color(0xFF166534)
                        )
                    }

                    if (totalSubmitted == 0) {
                        Text(
                            text = "No emergency rescue reports submitted yet. When you report an injured animal in the field, live triage milestones will appear here.",
                            fontSize = 12.sp,
                            color = SecondarySlate,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------------
        // C. DEVICE PERMISSIONS & DISPATCH TELEMETRY
        // -------------------------------------------------------------------
        item {
            SettingsSectionCard(
                title = "Device Permissions & Alerts",
                subtitle = "Control emergency GPS telemetry and live dispatch notification triggers"
            ) {
                // Location Permission Real Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (hasLocationPermission) PrimaryFixed.copy(alpha = 0.5f) else Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (hasLocationPermission) PrimaryEmerald else ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Emergency Scene GPS Access", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (hasLocationPermission) {
                                    "Granted • Automatically pinpoints rescue scenes with exact GPS coordinates"
                                } else {
                                    "Permission needed to automatically detect distress scene coordinates"
                                },
                                fontSize = 13.sp,
                                color = SecondarySlate,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                    if (!hasLocationPermission) {
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Grant Access", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
                    }
                }

                SettingsSwitchRow(
                    title = "Rescue Request Status Updates",
                    subtitle = "Receive live alerts when animal case changes triage stage",
                    checked = savedPreferences.notifyStatusUpdates,
                    onCheckedChange = { checked ->
                        IncidentRepository.updateCitizenPreferences(savedPreferences.copy(notifyStatusUpdates = checked))
                    },
                    icon = Icons.Default.Notifications,
                    iconTint = PrimaryEmerald
                )

                SettingsSwitchRow(
                    title = "Ambulance Team Dispatched",
                    subtitle = "Notifies you when an active response vehicle is assigned and en route",
                    checked = savedPreferences.notifyTeamAssigned,
                    onCheckedChange = { checked ->
                        IncidentRepository.updateCitizenPreferences(savedPreferences.copy(notifyTeamAssigned = checked))
                    },
                    icon = Icons.Default.LocalShipping,
                    iconTint = PrimaryEmerald
                )

                SettingsSwitchRow(
                    title = "Rescue Secured & Sheltered",
                    subtitle = "Detailed notification once the animal is safely in veterinary care",
                    checked = savedPreferences.notifyRescueCompleted,
                    onCheckedChange = { checked ->
                        IncidentRepository.updateCitizenPreferences(savedPreferences.copy(notifyRescueCompleted = checked))
                    },
                    icon = Icons.Default.CheckCircle,
                    iconTint = PrimaryEmerald
                )

                SettingsSwitchRow(
                    title = "Municipal Welfare Bulletins",
                    subtitle = "Rabies vaccination campaigns, pet welfare drives, and stray censuses",
                    checked = savedPreferences.notifyServiceAnnouncements,
                    onCheckedChange = { checked ->
                        IncidentRepository.updateCitizenPreferences(savedPreferences.copy(notifyServiceAnnouncements = checked))
                    },
                    icon = Icons.Default.Info,
                    iconTint = PrimaryEmerald
                )
            }
        }

        // -------------------------------------------------------------------
        // D. ACCOUNT SECURITY & PRIVACY
        // -------------------------------------------------------------------
        item {
            SettingsSectionCard(
                title = "Account Security & Privacy",
                subtitle = "Manage security credentials, legal terms, and data retention"
            ) {
                SettingsActionRow(
                    title = "Change Password",
                    subtitle = "Update your portal authentication password securely",
                    onClick = { showChangePasswordDialog = true },
                    icon = Icons.Default.Lock,
                    iconTint = PrimaryEmerald,
                    trailingText = "Update ›"
                )

                SettingsActionRow(
                    title = "Privacy & Location Retention",
                    subtitle = "PawRescue field data protection and GPS telemetry policies",
                    onClick = { showPrivacyDialog = true },
                    icon = Icons.Default.Policy,
                    iconTint = PrimaryEmerald,
                    trailingText = "View ›"
                )

                SettingsActionRow(
                    title = "Reporting Guidelines & Terms",
                    subtitle = "Community responsibilities, distress triage ethics, and false alarms",
                    onClick = { showTermsDialog = true },
                    icon = Icons.Default.Description,
                    iconTint = PrimaryEmerald,
                    trailingText = "View ›"
                )

                SettingsActionRow(
                    title = "Delete Citizen Account",
                    subtitle = "Permanently remove credentials while retaining municipal emergency logs",
                    onClick = { showDeleteAccountDialog = true },
                    icon = Icons.Default.Delete,
                    isDestructive = true,
                    trailingText = "Delete ›"
                )
            }
        }

        // -------------------------------------------------------------------
        // E. CITIZEN SIGN OUT BUTTON (Compact & Centered)
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
                    testTag = "citizen_logout_button"
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * Metric Card for Citizen Impact Overview
 */
@Composable
private fun CitizenMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    icon: ImageVector,
    accentColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondarySlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = count,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Privacy Policy Information Dialog
 */
@Composable
private fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Privacy & Location Data", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerLow)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PawRescue field data protection and location retention policy:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Telemetry Protection: Reporter names, mobile numbers, and GPS coordinates are collected solely to triage emergency animal distress cases and guide field ambulance teams to the scene.\n\n• Identity Safety: Contact numbers are only shared with the assigned rescue ambulance squad on scene for locating the animal.\n\n• Municipal Compliance: Incident records are archived in strict accordance with municipal animal welfare safety mandates.",
                        fontSize = 13.sp,
                        color = SecondarySlate,
                        lineHeight = 18.sp
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Terms of Service & Guidelines Dialog
 */
@Composable
private fun TermsOfServiceDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reporting Ethics & Terms", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerLow)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PawRescue is a dedicated animal search, rescue, and veterinary response service:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Responsible Dispatch: Submitting intentional false distress reports, fake photos, or abusive incident dispatches will lead to immediate account suspension and municipal notification.\n\n• Responder Safety: Do not endanger yourself or the distressed animal while taking intake photographs.\n\n• Prioritization: Emergency reports are triaged via automated computer vision and verified by Operations Dispatchers.",
                        fontSize = 13.sp,
                        color = SecondarySlate,
                        lineHeight = 18.sp
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("I Agree", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Edit Citizen Profile Form Dialog
 */
@Composable
private fun EditCitizenProfileDialog(
    currentName: String,
    currentEmail: String,
    currentPhone: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var phone by remember { mutableStateOf(currentPhone) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit Personal Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, enabled = !isSaving, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 13.sp,
                            color = ErrorRed,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Full Name", fontSize = 14.sp) },
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email Address", fontSize = 14.sp) },
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; errorMessage = null },
                    label = { Text("Mobile Phone Number", fontSize = 14.sp) },
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.trim().length < 2) {
                                errorMessage = "Please enter a valid full name."
                                return@Button
                            }
                            if (!email.contains("@") || !email.contains(".")) {
                                errorMessage = "Please enter a valid email address."
                                return@Button
                            }
                            if (phone.isNotBlank() && phone.trim().length < 7) {
                                errorMessage = "Please enter a valid phone number (at least 7 digits)."
                                return@Button
                            }
                            isSaving = true
                            onSave(name.trim(), email.trim(), phone.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Change Password Dialog
 */
@Composable
private fun ChangePasswordDialog(
    userId: String,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Change Account Password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (error != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error ?: "",
                            fontSize = 13.sp,
                            color = ErrorRed,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = currentPass,
                    onValueChange = { currentPass = it; error = null },
                    label = { Text("Current Password", fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it; error = null },
                    label = { Text("New Password (min 6 chars)", fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it; error = null },
                    label = { Text("Confirm New Password", fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (newPass != confirmPass) {
                                error = "New passwords do not match."
                                return@Button
                            }
                            val res = IncidentRepository.changePassword(userId, currentPass, newPass)
                            if (res.isSuccess) {
                                onSuccess("Your password has been changed successfully.")
                            } else {
                                error = res.exceptionOrNull()?.message ?: "Failed to change password."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Update", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FEEDBACK SUBMISSION DIALOG (For completed rescues)
// ---------------------------------------------------------------------------

@Composable
private fun FeedbackSubmissionDialog(
    incidentId: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("Rescue team arrived quickly and stabilized the animal gently! Thank you!") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rescue Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text("How was the response for case $incidentId?", fontSize = 12.sp, color = SecondarySlate)

                // Star rating selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star stars",
                                tint = if (star <= rating) Color(0xFFF59E0B) else SecondarySlate,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comments / Feedback") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Button(
                    onClick = { onSubmit(rating, comment) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Submit Rescue Feedback", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
