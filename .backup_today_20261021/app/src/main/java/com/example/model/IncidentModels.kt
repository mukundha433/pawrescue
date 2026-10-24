package com.example.model

enum class UrgencyLevel(val label: String, val badge: String) {
    CRITICAL("Critical Response", "P-1 CRITICAL"),
    HIGH("High Urgency", "P-1 HIGH"),
    MEDIUM("Review Required", "P-2 REVIEW"),
    LOW("Routine / Advisory", "P-3 ROUTINE")
}

enum class RescueStage(val stepNumber: Int, val title: String, val description: String) {
    SUBMITTED(1, "Case Submitted", "Citizen geo-located via mobile field app"),
    AI_VERIFIED(2, "AI Vision Verification", "Distress verified by automated vision scanner"),
    DISPATCHER_APPROVED(3, "Dispatcher Approved", "Verified by Operations Dispatcher"),
    TEAM_ASSIGNED(4, "Rescue Team Assigned", "Rescue Squad designated priority lead"),
    EN_ROUTE(5, "Team En Route", "Live beacon pinging en route to location"),
    ON_SCENE(6, "On Scene & Rescue", "Safe containment & roadside stabilizing"),
    ANIMAL_SECURED(7, "Secured & Clinical Triage", "Emergency vitals scan & care preparation"),
    CLINICAL_WARD(8, "Surgical / Shelter Ward", "Transferred to veterinary medical ward"),
    RESOLVED(9, "Case Resolved & Adoption", "Animal treated and safely sheltered or adopted")
}

enum class MissionStatus(val stepNumber: Int, val label: String) {
    ASSIGNED(1, "Assigned"),
    ACCEPTED(2, "Accepted"),
    EN_ROUTE(3, "En Route"),
    ARRIVED(4, "Arrived"),
    RESCUE_IN_PROGRESS(5, "Rescue In Progress"),
    ANIMAL_RESCUED(6, "Animal Rescued"),
    TRANSPORTED(7, "Transported"),
    COMPLETED(8, "Completed")
}

data class TriageChecklist(
    val animalVisibleConfidence: Float = 0f,
    val animalDetectedBreed: String = "",
    val distressConfidence: Float = 0f,
    val distressType: String = "",
    val imageAuthenticity: String = "",
    val duplicateMatches: Int = 0,
    val duplicateNote: String = ""
)

data class CaseDisposition(
    val id: String,
    val title: String,
    val badge: String? = null,
    val description: String
)

data class Incident(
    val id: String,
    val title: String,
    val species: String,
    val situation: String,
    val urgency: UrgencyLevel,
    val animalCount: Int = 1,
    val notes: String,
    val address: String,
    val sector: String = "Municipal Response Sector",
    val coordinates: String = "",
    val imageUrl: String = "",
    val currentStage: RescueStage = RescueStage.SUBMITTED,
    val missionStatus: MissionStatus = MissionStatus.ASSIGNED,
    val reporterId: String = "",
    val reporterName: String = "",
    val reporterPhone: String = "",
    val assignedTeamId: String? = null,
    val assignedUnit: String = "Pending Dispatch",
    val etaMinutes: Int = 0,
    val distanceKm: Double = 0.0,
    val timestamp: String = "Just now",
    val triageChecklist: TriageChecklist = TriageChecklist(),
    val isSuspectedDuplicate: Boolean = false,
    val duplicateOfId: String? = null,
    val beforeRescuePhotoUrl: String = "",
    val afterRescuePhotoUrl: String = "",
    val attendingVetName: String = "",
    val transferToken: String = "",
    val bodyConditionScore: Int = 0,
    val organizationId: String = "",
    val feedback: CitizenFeedback? = null,
    val evidenceList: List<EvidenceItem> = emptyList(),
    val rescueTeamEvidence: List<EvidenceItem> = emptyList(),
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING_REVIEW,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val locationData: IncidentLocationData? = null,
    val aiAnalysis: AIAnalysisResult? = null,
    val status: String = "Submitted",
    val createdAt: String = "Just now",
    val updatedAt: String = "Just now"
)

data class IncidentLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null,
    val address: String,
    val timestamp: String = "Just now",
    val locationSource: String = "device_gps", // "device_gps", "map_pin", "search"
    val locationConfirmed: Boolean = true
)

enum class UserRole(val code: String, val title: String, val emoji: String = "") {
    CITIZEN("CITIZEN", "Citizen Portal", ""),
    RESCUE_TEAM("RESCUE_TEAM", "Rescue Team Portal", ""),
    ORGANIZATION_ADMIN("ORGANIZATION_ADMIN", "Organization Admin Portal", "")
}

// Backward compatibility alias for UI references
typealias AppRole = UserRole

enum class AccountStatus {
    ACTIVE,
    SUSPENDED,
    PENDING_APPROVAL
}

data class UserAccount(
    val id: String,
    val name: String,
    val identifier: String, // email or member ID
    val phone: String,
    val passwordHash: String,
    val role: UserRole,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val teamId: String? = null,
    val teamName: String? = null,
    val organizationId: String? = null,
    val organizationName: String? = null,
    val avatarUrl: String = ImageHotlinks.VOLUNTEER_PRIYA
)

data class UserSession(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val status: AccountStatus,
    val teamId: String?,
    val teamName: String?,
    val organizationId: String?,
    val organizationName: String?,
    val authToken: String,
    val avatarUrl: String
)

sealed class AuthResult {
    data class Success(val session: UserSession) : AuthResult()
    data class WrongPortalError(
        val expectedRole: UserRole,
        val actualRole: UserRole,
        val message: String
    ) : AuthResult()
    data class AccountInactiveError(val message: String) : AuthResult()
    data class InvalidCredentialsError(val message: String) : AuthResult()
}

data class CitizenFeedback(
    val incidentId: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val submittedAt: String
)

data class RescueReport(
    val incidentId: String,
    val outcome: String,
    val animalCondition: String,
    val actionTaken: String,
    val rescueNotes: String,
    val rescuePhotos: List<String>,
    val treatmentShelter: String,
    val submittedAt: String = "Just now"
)

data class RescueTeam(
    val id: String,
    val name: String,
    val vehicle: String,
    val status: String,
    val members: List<String>,
    val activeMissionsCount: Int,
    val distanceKm: String
)

data class CitizenNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val incidentId: String? = null,
    val userId: String? = null,
    val isRead: Boolean = false
)

data class RescueTeamMember(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val teamId: String,
    val teamName: String,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val vehicleId: String = "VAN-04",
    val activeMissionsCount: Int = 0
)

data class CitizenUserRecord(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val reportsSubmitted: Int = 0,
    val isBanned: Boolean = false,
    val registeredDate: String = "2026-08-14"
)

data class CitizenPreferences(
    val notifyStatusUpdates: Boolean = true,
    val notifyTeamAssigned: Boolean = true,
    val notifyRescueCompleted: Boolean = true,
    val notifyServiceAnnouncements: Boolean = false,
    val preferredLanguage: String = "English"
)

enum class EquipmentStatus(val label: String, val badgeColor: Long) {
    READY("Ready", 0xFF166534),
    NEEDS_INSPECTION("Needs Inspection", 0xFFD97706),
    REPORTED_ISSUE("Reported Issue", 0xFFDC2626)
}

data class EquipmentItem(
    val id: String,
    val name: String,
    val category: String, // Medical Equipment, Animal Transport Equipment, Safety Equipment, Communication Equipment
    val status: EquipmentStatus = EquipmentStatus.READY,
    val isChecked: Boolean = true,
    val notes: String = "Ready for field deployment",
    val lastCheckedTimestamp: String = "Today, 08:00 AM",
    val lastCheckedBy: String = "Team Member"
)

data class EquipmentInspectionRecord(
    val timestamp: String,
    val itemId: String,
    val itemName: String,
    val status: EquipmentStatus,
    val inspectorName: String,
    val notes: String
)

data class VehicleEquipmentRecord(
    val vehicleId: String = "VAN-04 (Rapid Ambulance)",
    val modelDescription: String = "Mercedes-Benz Sprinter 4x4",
    val registrationPlate: String = "CA-SAR-9042",
    val inspectionDate: String = "2026-09-15",
    val readinessStatus: String = "Operational",
    val equipmentList: List<EquipmentItem> = listOf(
        EquipmentItem("eq-1", "Trauma Splints & Bandages Kit", "Medical Equipment", EquipmentStatus.READY, true, "Full sterile trauma kit ready", "Today, 08:00 AM"),
        EquipmentItem("eq-2", "Portable O2 Concentrator & Mask", "Medical Equipment", EquipmentStatus.READY, true, "Tank pressure: 2000 PSI", "Today, 08:00 AM"),
        EquipmentItem("eq-3", "Foldable Animal Transport Stretcher", "Animal Transport Equipment", EquipmentStatus.READY, true, "Load limit 120 lbs tested", "Today, 08:00 AM"),
        EquipmentItem("eq-4", "Heavy-Duty Crate & Containment Net", "Animal Transport Equipment", EquipmentStatus.READY, true, "Latch inspected and oiled", "Today, 08:00 AM"),
        EquipmentItem("eq-5", "Bite-Proof Kevlar Gloves & Ketch-All Pole", "Safety Equipment", EquipmentStatus.READY, true, "Sanitized and checked", "Today, 08:00 AM"),
        EquipmentItem("eq-6", "High-Visibility Safety Vests & Helmets", "Safety Equipment", EquipmentStatus.READY, true, "Class 3 reflective", "Today, 08:00 AM"),
        EquipmentItem("eq-7", "Encrypted VHF Tactical Radio", "Communication Equipment", EquipmentStatus.READY, true, "Channel 4 Primary frequency", "Today, 08:00 AM"),
        EquipmentItem("eq-8", "Field Cellular Relay & Tablet", "Communication Equipment", EquipmentStatus.READY, true, "Fully charged 98%", "Today, 08:00 AM")
    ),
    val inspectionHistory: List<EquipmentInspectionRecord> = listOf(
        EquipmentInspectionRecord("Today, 08:00 AM", "eq-1", "Trauma Splints Kit", EquipmentStatus.READY, "Team Leader", "Morning operational readiness check passed"),
        EquipmentInspectionRecord("Yesterday, 06:30 PM", "eq-2", "Portable O2 Concentrator", EquipmentStatus.READY, "Medic", "O2 refill completed post-mission"),
        EquipmentInspectionRecord("2026-10-07 09:00 AM", "eq-5", "Bite-Proof Kevlar Gloves", EquipmentStatus.READY, "Safety Officer", "Sanitization & tensile integrity verified")
    )
)

data class AuditLogEntry(
    val timestamp: String,
    val adminName: String,
    val action: String,
    val detail: String,
    val targetRecord: String = "System Configuration",
    val outcome: String = "Success"
)

data class DispatchPolicySettings(
    val priorityDefinition: String = "P-1 Critical: Immediate < 15 min dispatch",
    val autoDispatchStrategy: String = "Proximity & Specialization",
    val duplicateRadiusMeters: Int = 200,
    val duplicateReviewWindowMinutes: Int = 30,
    val operatingHours: String = "24/7 Metro Rapid Response"
)

data class AdminSystemSettings(
    val aiConfidenceThreshold: Float = 0.85f,
    val autoVerifyCritical: Boolean = true,
    val dispatchRadiusKm: Double = 15.0,
    val emergencyHotline: String = "+1 (800) 555-PAWS",
    val organizationName: String = "PawRescue Metro SAR Network",
    val notificationsEnabled: Boolean = true,
    val dispatchPolicy: DispatchPolicySettings = DispatchPolicySettings(),
    val lastUpdatedTimestamp: String = "2026-10-09 08:30 AM",
    val auditHistory: List<AuditLogEntry> = listOf(
        AuditLogEntry("2026-10-09 08:30 AM", "Chief Ops Director", "AI Threshold Updated", "Confidence threshold updated to 85%", targetRecord = "Vision Filter", outcome = "Applied"),
        AuditLogEntry("2026-10-08 04:15 PM", "Chief Ops Director", "Fleet Provisioning", "Assigned Unit Omega-7 to North Sector", targetRecord = "Unit Omega-7", outcome = "Active"),
        AuditLogEntry("2026-10-07 10:00 AM", "Chief Ops Director", "Policy Sync", "Verified critical trauma triage rules", targetRecord = "SOP-04", outcome = "Verified")
    )
)

data class TimelineAuditRecord(
    val time: String,
    val title: String,
    val detail: String,
    val isCompleted: Boolean = true
)

