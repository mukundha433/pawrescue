package com.example.model

import androidx.compose.ui.graphics.Color
import org.osmdroid.util.GeoPoint

enum class MapMarkerType {
    INCIDENT,
    RESCUE_TEAM,
    HOSPITAL,
    USER_LOCATION
}

data class MapMarkerItem(
    val id: String,
    val title: String,
    val snippet: String? = null,
    val latitude: Double,
    val longitude: Double,
    val type: MapMarkerType = MapMarkerType.INCIDENT,
    val urgency: UrgencyLevel? = null,
    val status: String? = null,
    val isDraggable: Boolean = false,
    val tag: String? = null,
    val rawIncident: Incident? = null
)

data class MapRoute(
    val id: String,
    val points: List<GeoPoint>,
    val color: Color,
    val strokeWidth: Float = 5f,
    val title: String? = null,
    val isDashed: Boolean = false
)

data class MapFilterState(
    val showCritical: Boolean = true,
    val showHigh: Boolean = true,
    val showModerate: Boolean = true,
    val showRoutine: Boolean = true,
    val showRescueTeams: Boolean = true,
    val showHospitals: Boolean = true,
    val showRoutes: Boolean = true
) {
    fun matchesUrgency(urgency: UrgencyLevel?): Boolean = when (urgency) {
        UrgencyLevel.CRITICAL -> showCritical
        UrgencyLevel.HIGH -> showHigh
        UrgencyLevel.MEDIUM -> showModerate
        UrgencyLevel.LOW -> showRoutine
        null -> true
    }
}
