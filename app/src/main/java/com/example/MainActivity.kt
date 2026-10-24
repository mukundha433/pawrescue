package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
            onEquipmentReceived = { eq -> IncidentRepository.setVehicleEquipment(eq) }
        )

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
        val session = currentSession

        if (session == null) {
            // Requirement 1 & 2: FIRST APP OPEN & PORTAL SELECTION
            // App starts on Portal Selection Screen ("Welcome to PawRescue", "Choose your portal to continue")
            // Selecting portal presents login screen; access granted ONLY after valid role authentication.
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
