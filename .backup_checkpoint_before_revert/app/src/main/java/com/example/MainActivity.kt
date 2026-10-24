package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IncidentRepository
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.components.SosEmergencyDialog
import com.example.ui.screens.CitizenPortalScreen
import com.example.ui.screens.IncidentDetailsScreen
import com.example.ui.screens.OrganizationAdminPortalScreen
import com.example.ui.screens.PortalSelectionScreen
import com.example.ui.screens.RescueTeamPortalScreen
import com.example.ui.theme.StrayCareTheme

sealed class AppDestination {
    data object PortalRoot : AppDestination()
    data class IncidentDetails(val incidentId: String) : AppDestination()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Realtime Cloud Firestore sync across all portals
        com.example.data.FirebaseIncidentDataSource.initializeRealtimeSync(
            context = this,
            onIncidentsReceived = { list -> IncidentRepository.mergeRemoteIncidents(list) },
            onNotificationsReceived = { list -> IncidentRepository.mergeRemoteNotifications(list) },
            onSettingsReceived = { settings -> IncidentRepository.updateSystemSettings(settings) },
            onPreferencesReceived = { prefs -> IncidentRepository.updateCitizenPreferences(prefs) },
            onEquipmentReceived = { eq -> IncidentRepository.setVehicleEquipment(eq) },
            onRescueRequestsReceived = { list -> IncidentRepository.mergeRemoteRescueRequests(list) }
        )

        // Initialize persistent session manager, load saved accounts, and restore active portal
        IncidentRepository.initialize(this)

        setContent {
            StrayCareTheme {
                StrayCareApp()
            }
        }
    }
}

@Composable
fun StrayCareApp() {
    val currentSession by IncidentRepository.currentSession.collectAsState()
    val isSessionRestoring by IncidentRepository.isSessionRestoring.collectAsState()
    var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.PortalRoot) }
    var showSosDialog by remember { mutableStateOf(false) }

    // System SOS Emergency dialog
    if (showSosDialog) {
        SosEmergencyDialog(onDismiss = { showSosDialog = false })
    }

    // Back handling
    BackHandler(enabled = currentDestination !is AppDestination.PortalRoot) {
        currentDestination = AppDestination.PortalRoot
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isSessionRestoring) {
            // Elegant session restoration splash - prevents flashing login screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7FAF8)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pawrescue_official_logo),
                        contentDescription = "PawRescue Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    CircularProgressIndicator(
                        color = Color(0xFF00684A),
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = "Restoring secure session...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            val session = currentSession

            if (session == null) {
                // Requirement 1 & 2: FIRST APP OPEN & PORTAL SELECTION
                PortalSelectionScreen(
                    onLoginSuccess = { authorizedSession ->
                        currentDestination = AppDestination.PortalRoot
                    },
                    onSosClick = { showSosDialog = true }
                )
            } else {
                // Authenticated Session: STRICT PORTAL DATA & ROLE ISOLATION
                when (val dest = currentDestination) {
                    is AppDestination.IncidentDetails -> {
                        IncidentDetailsScreen(
                            incidentId = dest.incidentId,
                            onNavigateBack = { currentDestination = AppDestination.PortalRoot },
                            onNavigateToClinical = { currentDestination = AppDestination.PortalRoot },
                            onSosClick = { showSosDialog = true },
                            onProfileClick = { currentDestination = AppDestination.PortalRoot }
                        )
                    }

                    is AppDestination.PortalRoot -> {
                        when (session.role) {
                            UserRole.CITIZEN -> {
                                CitizenPortalScreen(
                                    session = session,
                                    onLogout = {
                                        IncidentRepository.logout()
                                        currentDestination = AppDestination.PortalRoot
                                    },
                                    onSosClick = { showSosDialog = true },
                                    onViewIncidentDetails = { incidentId ->
                                        currentDestination = AppDestination.IncidentDetails(incidentId)
                                    }
                                )
                            }

                            UserRole.RESCUE_TEAM -> {
                                RescueTeamPortalScreen(
                                    session = session,
                                    onLogout = {
                                        IncidentRepository.logout()
                                        currentDestination = AppDestination.PortalRoot
                                    },
                                    onSosClick = { showSosDialog = true },
                                    onViewIncidentDetails = { incidentId ->
                                        currentDestination = AppDestination.IncidentDetails(incidentId)
                                    }
                                )
                            }

                            UserRole.ORGANIZATION_ADMIN -> {
                                OrganizationAdminPortalScreen(
                                    session = session,
                                    onLogout = {
                                        IncidentRepository.logout()
                                        currentDestination = AppDestination.PortalRoot
                                    },
                                    onSosClick = { showSosDialog = true },
                                    onViewIncidentDetails = { incidentId ->
                                        currentDestination = AppDestination.IncidentDetails(incidentId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
