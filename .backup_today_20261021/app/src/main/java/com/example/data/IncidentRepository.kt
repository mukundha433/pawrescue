package com.example.data

import com.example.model.AccountStatus
import com.example.model.AdminSystemSettings
import com.example.model.AppRole
import com.example.model.AuthResult
import com.example.model.CitizenFeedback
import com.example.model.CitizenNotification
import com.example.model.CitizenUserRecord
import com.example.model.ImageHotlinks
import com.example.model.Incident
import com.example.model.MissionStatus
import com.example.model.RescueReport
import com.example.model.RescueStage
import com.example.model.RescueTeamMember
import com.example.model.TimelineAuditRecord
import com.example.model.TriageChecklist
import com.example.model.UrgencyLevel
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object IncidentRepository {

    // Default registered accounts for strict role authentication testing
    private val accounts = mutableListOf(
        UserAccount(
            id = "CIT-001",
            name = "Elena Rostova",
            identifier = "elena@pawrescue.org",
            phone = "+1 (555) 019-2841",
            passwordHash = "citizen123",
            role = UserRole.CITIZEN,
            status = AccountStatus.ACTIVE,
            avatarUrl = ImageHotlinks.VOLUNTEER_PRIYA
        ),
        UserAccount(
            id = "TEAM-BETA-3",
            name = "Sarah Miller & Dan Kline",
            identifier = "team.beta3@pawrescue.org",
            phone = "+1 (555) 302-8841",
            passwordHash = "rescue123",
            role = UserRole.RESCUE_TEAM,
            status = AccountStatus.ACTIVE,
            teamId = "TEAM-BETA-3",
            teamName = "Unit Beta-3 (Rapid Van)",
            avatarUrl = ImageHotlinks.RESCUE_VAN_CREW
        ),
        UserAccount(
            id = "ORG-ADMIN-01",
            name = "Chief Ops Director (Admin HQ)",
            identifier = "admin@pawrescue.org",
            phone = "+1 (800) 555-PAWS",
            passwordHash = "admin123",
            role = UserRole.ORGANIZATION_ADMIN,
            status = AccountStatus.ACTIVE,
            organizationId = "ORG-METRO-01",
            organizationName = "PawRescue Metro SAR Network",
            avatarUrl = ImageHotlinks.VOLUNTEER_PRIYA
        ),
        // Demo inactive rescue volunteer account
        UserAccount(
            id = "TEAM-PENDING-09",
            name = "David Miller (Unapproved Volunteer)",
            identifier = "pending@pawrescue.org",
            phone = "+1 (555) 890-1122",
            passwordHash = "pending123",
            role = UserRole.RESCUE_TEAM,
            status = AccountStatus.PENDING_APPROVAL,
            teamId = "TEAM-PENDING-09",
            teamName = "Pending Verification Unit",
            avatarUrl = ImageHotlinks.RESCUE_VAN_CREW
        ),
        // Demo suspended citizen account
        UserAccount(
            id = "CIT-BANNED-99",
            name = "Suspended User",
            identifier = "banned@pawrescue.org",
            phone = "+1 (555) 000-0000",
            passwordHash = "banned123",
            role = UserRole.CITIZEN,
            status = AccountStatus.SUSPENDED,
            avatarUrl = ImageHotlinks.VOLUNTEER_PRIYA
        )
    )

    // Initial session is NULL so First Open shows Portal Selection
    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _currentRole = MutableStateFlow(AppRole.CITIZEN)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Zero reports by default. A new user with zero records MUST see zero reports.
    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    private val _activeIncident = MutableStateFlow<Incident?>(null)
    val activeIncident: StateFlow<Incident?> = _activeIncident.asStateFlow()

    private val _isUnitOnDuty = MutableStateFlow(true)
    val isUnitOnDuty: StateFlow<Boolean> = _isUnitOnDuty.asStateFlow()

    private val _selectedDisposition = MutableStateFlow("hospital")
    val selectedDisposition: StateFlow<String> = _selectedDisposition.asStateFlow()

    // Notifications - empty by default, populated ONLY on real events
    private val _notifications = MutableStateFlow<List<CitizenNotification>>(emptyList())
    val notifications: StateFlow<List<CitizenNotification>> = _notifications.asStateFlow()

    // Rescue Team Members
    private val _teamMembers = MutableStateFlow<List<RescueTeamMember>>(emptyList())
    val teamMembers: StateFlow<List<RescueTeamMember>> = _teamMembers.asStateFlow()

    // Citizen Users Management
    private val _citizenUsers = MutableStateFlow<List<CitizenUserRecord>>(emptyList())
    val citizenUsers: StateFlow<List<CitizenUserRecord>> = _citizenUsers.asStateFlow()

    // Admin System Settings
    private val _systemSettings = MutableStateFlow(AdminSystemSettings())
    val systemSettings: StateFlow<AdminSystemSettings> = _systemSettings.asStateFlow()

    // Citizen Preferences
    private val _citizenPreferences = MutableStateFlow(com.example.model.CitizenPreferences())
    val citizenPreferences: StateFlow<com.example.model.CitizenPreferences> = _citizenPreferences.asStateFlow()

    // Vehicle and Equipment record for Rescue Team
    private val _vehicleEquipment = MutableStateFlow(com.example.model.VehicleEquipmentRecord())
    val vehicleEquipment: StateFlow<com.example.model.VehicleEquipmentRecord> = _vehicleEquipment.asStateFlow()

    val auditRecords = emptyList<TimelineAuditRecord>()

    fun getIncidentById(id: String): Incident? = _incidents.value.find { it.id == id }

    // Authentication Logic with Strict Role Verification
    fun authenticate(targetPortal: UserRole, identifier: String, pass: String): AuthResult {
        val trimmedId = identifier.trim()
        val trimmedPass = pass.trim()

        if (trimmedId.isEmpty() || trimmedPass.isEmpty()) {
            return AuthResult.InvalidCredentialsError("Please provide both email/ID and password.")
        }

        val account = accounts.find {
            it.identifier.equals(trimmedId, ignoreCase = true) ||
            it.id.equals(trimmedId, ignoreCase = true) ||
            it.phone == trimmedId
        } ?: return AuthResult.InvalidCredentialsError("No account found matching '$trimmedId'. Please check credentials.")

        if (account.passwordHash != trimmedPass) {
            return AuthResult.InvalidCredentialsError("Incorrect password entered. Please try again.")
        }

        // STRICT PORTAL / ROLE ENFORCEMENT
        if (account.role != targetPortal) {
            return AuthResult.WrongPortalError(
                expectedRole = targetPortal,
                actualRole = account.role,
                message = "Access Denied: Your account role is '${account.role.title}'. You cannot log in through the ${targetPortal.title}."
            )
        }

        // ACCOUNT STATUS CHECK
        if (account.status != AccountStatus.ACTIVE) {
            return AuthResult.AccountInactiveError(
                "Access Denied: Account status is '${account.status.name}'. Please contact the Organization Administrator to activate your account."
            )
        }

        // Create authorized session
        val session = UserSession(
            userId = account.id,
            name = account.name,
            email = account.identifier,
            phone = account.phone,
            role = account.role,
            status = account.status,
            teamId = account.teamId,
            teamName = account.teamName,
            organizationId = account.organizationId,
            organizationName = account.organizationName,
            authToken = "AUTH-${System.currentTimeMillis()}-${account.id}",
            avatarUrl = account.avatarUrl
        )

        _currentSession.value = session
        _currentRole.value = session.role
        return AuthResult.Success(session)
    }

    // Citizen public signup
    fun signUpCitizen(name: String, emailOrPhone: String, pass: String): AuthResult {
        val trimmedName = name.trim()
        val trimmedContact = emailOrPhone.trim()
        val trimmedPass = pass.trim()

        if (trimmedName.isEmpty() || trimmedContact.isEmpty() || trimmedPass.length < 4) {
            return AuthResult.InvalidCredentialsError("Please provide full name, contact, and password (at least 4 characters).")
        }

        val existing = accounts.find { it.identifier.equals(trimmedContact, ignoreCase = true) || it.phone == trimmedContact }
        if (existing != null) {
            return AuthResult.InvalidCredentialsError("An account already exists for '$trimmedContact'. Please log in.")
        }

        val newId = "CIT-${(1000..9999).random()}"
        val newAccount = UserAccount(
            id = newId,
            name = trimmedName,
            identifier = if (trimmedContact.contains("@")) trimmedContact else "$newId@citizen.pawrescue.org",
            phone = if (!trimmedContact.contains("@")) trimmedContact else "+1 (555) ${(100..999).random()}-${(1000..9999).random()}",
            passwordHash = trimmedPass,
            role = UserRole.CITIZEN,
            status = AccountStatus.ACTIVE,
            avatarUrl = ImageHotlinks.VOLUNTEER_PRIYA
        )
        accounts.add(newAccount)
        _citizenUsers.update {
            listOf(CitizenUserRecord(newId, trimmedName, newAccount.identifier, newAccount.phone, AccountStatus.ACTIVE, 0, false)) + it
        }

        val session = UserSession(
            userId = newAccount.id,
            name = newAccount.name,
            email = newAccount.identifier,
            phone = newAccount.phone,
            role = UserRole.CITIZEN,
            status = AccountStatus.ACTIVE,
            teamId = null,
            teamName = null,
            organizationId = null,
            organizationName = null,
            authToken = "AUTH-CIT-${System.currentTimeMillis()}",
            avatarUrl = newAccount.avatarUrl
        )
        _currentSession.value = session
        _currentRole.value = UserRole.CITIZEN
        return AuthResult.Success(session)
    }

    fun logout() {
        _currentSession.value = null
    }

    fun setRole(role: AppRole) {
        _currentRole.value = role
    }

    fun selectIncident(id: String) {
        val found = _incidents.value.find { it.id == id }
        if (found != null) {
            _activeIncident.value = found
        }
    }

    fun toggleDuty() {
        _isUnitOnDuty.update { !it }
    }

    fun setDisposition(id: String) {
        _selectedDisposition.value = id
    }

    // Advance Mission Status for Rescue Team
    fun updateMissionStatus(incidentId: String, newStatus: MissionStatus) {
        val mappedStage = when (newStatus) {
            MissionStatus.ASSIGNED -> RescueStage.TEAM_ASSIGNED
            MissionStatus.ACCEPTED -> RescueStage.TEAM_ASSIGNED
            MissionStatus.EN_ROUTE -> RescueStage.EN_ROUTE
            MissionStatus.ARRIVED -> RescueStage.ON_SCENE
            MissionStatus.RESCUE_IN_PROGRESS -> RescueStage.ON_SCENE
            MissionStatus.ANIMAL_RESCUED -> RescueStage.ANIMAL_SECURED
            MissionStatus.TRANSPORTED -> RescueStage.CLINICAL_WARD
            MissionStatus.COMPLETED -> RescueStage.RESOLVED
        }

        _incidents.update { list ->
            list.map {
                if (it.id == incidentId) {
                    it.copy(missionStatus = newStatus, currentStage = mappedStage)
                } else it
            }
        }
        if (_activeIncident.value?.id == incidentId) {
            _activeIncident.update { it?.copy(missionStatus = newStatus, currentStage = mappedStage) }
        }
        FirebaseIncidentDataSource.persistMissionStatus(incidentId, mappedStage, newStatus)
    }

    // Submit Rescue Report from Rescue Team
    fun submitRescueReport(report: RescueReport) {
        _incidents.update { list ->
            list.map {
                if (it.id == report.incidentId) {
                    it.copy(
                        notes = "${it.notes}\n[Rescue Report: ${report.actionTaken} - ${report.outcome}]",
                        attendingVetName = report.treatmentShelter,
                        missionStatus = MissionStatus.COMPLETED,
                        currentStage = RescueStage.RESOLVED
                    )
                } else it
            }
        }
        if (_activeIncident.value?.id == report.incidentId) {
            _activeIncident.update {
                it?.copy(
                    missionStatus = MissionStatus.COMPLETED,
                    currentStage = RescueStage.RESOLVED
                )
            }
        }
        FirebaseIncidentDataSource.persistRescueReport(report)
    }

    // Submit Citizen Feedback
    fun submitCitizenFeedback(feedback: CitizenFeedback) {
        _incidents.update { list ->
            list.map {
                if (it.id == feedback.incidentId) {
                    it.copy(feedback = feedback)
                } else it
            }
        }
        if (_activeIncident.value?.id == feedback.incidentId) {
            _activeIncident.update { it?.copy(feedback = feedback) }
        }
    }

    // Admin Complaint Management: Verify / Reject / Assign / Priority
    fun adminVerifyComplaint(id: String) {
        _incidents.update { list ->
            list.map {
                if (it.id == id) it.copy(currentStage = RescueStage.DISPATCHER_APPROVED) else it
            }
        }
        FirebaseIncidentDataSource.persistMissionStatus(id, RescueStage.DISPATCHER_APPROVED, MissionStatus.ASSIGNED)
    }

    fun adminRejectComplaint(id: String, reason: String) {
        _incidents.update { list ->
            list.filterNot { it.id == id }
        }
    }

    fun adminSetPriority(id: String, urgency: UrgencyLevel) {
        _incidents.update { list ->
            list.map {
                if (it.id == id) it.copy(urgency = urgency) else it
            }
        }
        _incidents.value.find { it.id == id }?.let {
            FirebaseIncidentDataSource.persistIncident(it)
        }
    }

    fun adminAssignTeam(id: String, teamId: String, teamName: String) {
        _incidents.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(
                        assignedTeamId = teamId,
                        assignedUnit = teamName,
                        currentStage = RescueStage.TEAM_ASSIGNED,
                        missionStatus = MissionStatus.ASSIGNED
                    )
                } else it
            }
        }
        FirebaseIncidentDataSource.persistAssignment(id, teamId, teamName, null, null)
    }

    // Rescue Team Management Actions
    fun addRescueTeamMember(name: String, email: String, phone: String, teamName: String): RescueTeamMember {
        val newId = "TM-0${_teamMembers.value.size + 1}"
        val newMember = RescueTeamMember(
            id = newId,
            name = name,
            email = email,
            phone = phone,
            teamId = "TEAM-${(10..99).random()}",
            teamName = teamName,
            status = AccountStatus.ACTIVE,
            vehicleId = "VAN-0${(1..9).random()}",
            activeMissionsCount = 0
        )
        _teamMembers.update { listOf(newMember) + it }
        accounts.add(
            UserAccount(
                id = newMember.id,
                name = newMember.name,
                identifier = newMember.email,
                phone = newMember.phone,
                passwordHash = "rescue123",
                role = UserRole.RESCUE_TEAM,
                status = AccountStatus.ACTIVE,
                teamId = newMember.teamId,
                teamName = newMember.teamName
            )
        )
        return newMember
    }

    fun approveTeamMember(id: String) {
        _teamMembers.update { list ->
            list.map { if (it.id == id) it.copy(status = AccountStatus.ACTIVE) else it }
        }
        val acc = accounts.find { it.id == id || it.identifier == id }
        if (acc != null) {
            val idx = accounts.indexOf(acc)
            accounts[idx] = acc.copy(status = AccountStatus.ACTIVE)
        }
    }

    fun toggleTeamMemberStatus(id: String) {
        _teamMembers.update { list ->
            list.map {
                if (it.id == id) {
                    val next = if (it.status == AccountStatus.ACTIVE) AccountStatus.SUSPENDED else AccountStatus.ACTIVE
                    it.copy(status = next)
                } else it
            }
        }
    }

    // User Management (Admin)
    fun toggleCitizenBan(id: String) {
        _citizenUsers.update { list ->
            list.map {
                if (it.id == id) {
                    val newBan = !it.isBanned
                    val newStatus = if (newBan) AccountStatus.SUSPENDED else AccountStatus.ACTIVE
                    it.copy(isBanned = newBan, status = newStatus)
                } else it
            }
        }
        val acc = accounts.find { it.id == id }
        if (acc != null) {
            val idx = accounts.indexOf(acc)
            accounts[idx] = acc.copy(status = if (acc.status == AccountStatus.ACTIVE) AccountStatus.SUSPENDED else AccountStatus.ACTIVE)
        }
    }

    // Update authenticated user profile
    fun updateUserProfile(userId: String, newName: String, newEmail: String, newPhone: String): Boolean {
        val trimmedName = newName.trim()
        val trimmedEmail = newEmail.trim()
        val trimmedPhone = newPhone.trim()
        if (trimmedName.isEmpty() || trimmedEmail.isEmpty()) return false

        val acc = accounts.find { it.id == userId } ?: return false
        val idx = accounts.indexOf(acc)
        val updatedAcc = acc.copy(name = trimmedName, identifier = trimmedEmail, phone = trimmedPhone)
        accounts[idx] = updatedAcc

        // If this is the current active session, update it
        val session = _currentSession.value
        if (session != null && session.userId == userId) {
            _currentSession.value = session.copy(
                name = trimmedName,
                email = trimmedEmail,
                phone = trimmedPhone
            )
        }

        // Also update in citizen users list if present
        _citizenUsers.update { list ->
            list.map {
                if (it.id == userId) it.copy(name = trimmedName, email = trimmedEmail, phone = trimmedPhone) else it
            }
        }
        return true
    }

    // Change authenticated user password
    fun changePassword(userId: String, currentPass: String, newPass: String): Result<String> {
        val acc = accounts.find { it.id == userId } ?: return Result.failure(Exception("Account not found"))
        if (acc.passwordHash != currentPass.trim()) {
            return Result.failure(Exception("Incorrect current password"))
        }
        if (newPass.trim().length < 6) {
            return Result.failure(Exception("New password must be at least 6 characters"))
        }
        val idx = accounts.indexOf(acc)
        accounts[idx] = acc.copy(passwordHash = newPass.trim())
        return Result.success("Password changed successfully")
    }

    // Delete citizen account
    fun deleteCitizenAccount(userId: String): Result<String> {
        val acc = accounts.find { it.id == userId } ?: return Result.failure(Exception("Account not found"))
        if (acc.role != UserRole.CITIZEN) {
            return Result.failure(Exception("Only citizen accounts can be self-deleted"))
        }
        accounts.remove(acc)
        _citizenUsers.update { it.filter { user -> user.id != userId } }
        if (_currentSession.value?.userId == userId) {
            _currentSession.value = null
        }
        return Result.success("Account deleted successfully")
    }

    // Update Citizen preferences
    fun updateCitizenPreferences(prefs: com.example.model.CitizenPreferences) {
        _citizenPreferences.value = prefs
    }

    // Toggle equipment item check status or update inspection status
    fun updateEquipmentStatus(
        itemId: String,
        newStatus: com.example.model.EquipmentStatus,
        inspectorName: String,
        notes: String
    ) {
        val timestamp = "Today, Just now"
        _vehicleEquipment.update { record ->
            var updatedItemName = ""
            val updated = record.equipmentList.map { eq ->
                if (eq.id == itemId) {
                    updatedItemName = eq.name
                    eq.copy(
                        status = newStatus,
                        isChecked = newStatus == com.example.model.EquipmentStatus.READY,
                        lastCheckedTimestamp = timestamp,
                        lastCheckedBy = inspectorName,
                        notes = notes.ifBlank { eq.notes }
                    )
                } else eq
            }
            val newInspection = com.example.model.EquipmentInspectionRecord(
                timestamp = timestamp,
                itemId = itemId,
                itemName = updatedItemName.ifBlank { itemId },
                status = newStatus,
                inspectorName = inspectorName,
                notes = notes
            )
            record.copy(
                equipmentList = updated,
                inspectionHistory = listOf(newInspection) + record.inspectionHistory
            )
        }
    }

    // Toggle equipment item check status
    fun toggleEquipmentItem(itemId: String) {
        _vehicleEquipment.update { record ->
            val updated = record.equipmentList.map { eq ->
                if (eq.id == itemId) {
                    val nextState = !eq.isChecked
                    val nextStatus = if (nextState) com.example.model.EquipmentStatus.READY else com.example.model.EquipmentStatus.NEEDS_INSPECTION
                    eq.copy(
                        isChecked = nextState,
                        status = nextStatus,
                        lastCheckedTimestamp = "Today, Just now"
                    )
                } else eq
            }
            record.copy(equipmentList = updated)
        }
    }

    // Report damaged or missing equipment
    fun reportEquipmentIssue(itemId: String, issueDescription: String, reporterName: String): Result<String> {
        if (issueDescription.isBlank()) return Result.failure(Exception("Issue description cannot be empty"))
        updateEquipmentStatus(
            itemId = itemId,
            newStatus = com.example.model.EquipmentStatus.REPORTED_ISSUE,
            inspectorName = reporterName,
            notes = issueDescription
        )
        return Result.success("Equipment issue logged with HQ fleet maintenance")
    }

    // Rescue Team duty status with dispatch validation
    fun setRescueTeamDuty(onDuty: Boolean): Result<String> {
        // If team is turning OFF duty while having an active mission that is not resolved/completed:
        val active = _activeIncident.value
        if (!onDuty && active != null && active.currentStage != RescueStage.RESOLVED && active.currentStage != RescueStage.SUBMITTED) {
            return Result.failure(Exception("Cannot go Off-Duty while responding to an active mission #${active.id}. Complete or hand over mission first."))
        }
        _isUnitOnDuty.value = onDuty
        return Result.success(if (onDuty) "Status updated: On-Duty (Ready for Dispatch)" else "Status updated: Off-Duty")
    }

    // System Settings (Admin)
    fun updateSystemSettings(
        threshold: Float,
        autoVerify: Boolean,
        radius: Double,
        priorityDef: String = _systemSettings.value.dispatchPolicy.priorityDefinition,
        strategy: String = _systemSettings.value.dispatchPolicy.autoDispatchStrategy,
        dupRadius: Int = _systemSettings.value.dispatchPolicy.duplicateRadiusMeters,
        reviewWindow: Int = _systemSettings.value.dispatchPolicy.duplicateReviewWindowMinutes
    ) {
        val adminName = _currentSession.value?.name ?: "Chief Ops Director"
        val newAudit = com.example.model.AuditLogEntry(
            timestamp = "Just now",
            adminName = adminName,
            action = "System Settings Updated",
            detail = "Threshold: ${(threshold * 100).toInt()}%, Auto-Verify: $autoVerify, Radius: ${radius.toInt()}km, Dup Radius: ${dupRadius}m, Window: ${reviewWindow}m",
            targetRecord = "Core Dispatch & Triage Policy",
            outcome = "Applied"
        )
        _systemSettings.update {
            it.copy(
                aiConfidenceThreshold = threshold,
                autoVerifyCritical = autoVerify,
                dispatchRadiusKm = radius,
                dispatchPolicy = it.dispatchPolicy.copy(
                    priorityDefinition = priorityDef,
                    autoDispatchStrategy = strategy,
                    duplicateRadiusMeters = dupRadius,
                    duplicateReviewWindowMinutes = reviewWindow
                ),
                lastUpdatedTimestamp = "Just now",
                auditHistory = listOf(newAudit) + it.auditHistory
            )
        }
    }

    fun updateOperatingSchedule(newHours: String, newHotline: String) {
        val adminName = _currentSession.value?.name ?: "Chief Ops Director"
        val newAudit = com.example.model.AuditLogEntry(
            timestamp = "Just now",
            adminName = adminName,
            action = "Operating Schedule Updated",
            detail = "Schedule: $newHours, Hotline: $newHotline",
            targetRecord = "Operating Schedule",
            outcome = "Applied"
        )
        _systemSettings.update {
            it.copy(
                emergencyHotline = newHotline,
                dispatchPolicy = it.dispatchPolicy.copy(operatingHours = newHours),
                lastUpdatedTimestamp = "Just now",
                auditHistory = listOf(newAudit) + it.auditHistory
            )
        }
    }

    fun clearAuditLog() {
        val adminName = _currentSession.value?.name ?: "Chief Ops Director"
        val resetAudit = com.example.model.AuditLogEntry(
            timestamp = "Just now",
            adminName = adminName,
            action = "Audit Log Cleared",
            detail = "Historical administrative logs purged by authorized administrator",
            targetRecord = "Audit Trail",
            outcome = "Archived"
        )
        _systemSettings.update {
            it.copy(auditHistory = listOf(resetAudit))
        }
    }

    fun advanceActiveIncidentStage(): RescueStage {
        val active = _activeIncident.value ?: return RescueStage.SUBMITTED
        val stages = RescueStage.entries.toTypedArray()
        val nextIndex = (active.currentStage.ordinal + 1).coerceAtMost(stages.lastIndex)
        val nextStage = stages[nextIndex]
        _activeIncident.update { it?.copy(currentStage = nextStage) }
        _incidents.update { list ->
            list.map { if (it.id == active.id) it.copy(currentStage = nextStage) else it }
        }
        return nextStage
    }

    fun markActiveIncidentResolved() {
        val active = _activeIncident.value ?: return
        _activeIncident.update { it?.copy(currentStage = RescueStage.RESOLVED, missionStatus = MissionStatus.COMPLETED) }
        _incidents.update { list ->
            list.map { if (it.id == active.id) it.copy(currentStage = RescueStage.RESOLVED, missionStatus = MissionStatus.COMPLETED) else it }
        }
    }

    fun mergeDuplicate(dupId: String, masterId: String) {
        _incidents.update { list ->
            list.filterNot { it.id == dupId }
        }
    }

    fun addNewIntake(
        species: String,
        situation: String,
        urgency: UrgencyLevel,
        count: Int,
        notes: String,
        address: String,
        latitude: Double? = null,
        longitude: Double? = null,
        accuracyMeters: Float? = null,
        locationData: com.example.model.IncidentLocationData? = null,
        evidenceList: List<com.example.model.EvidenceItem> = emptyList(),
        aiAnalysis: com.example.model.AIAnalysisResult? = null
    ): Incident {
        val newId = "SAR-2026-${(1000..9999).random()}"
        val user = _currentSession.value
        val primaryPhotoUrl = evidenceList.firstOrNull()?.uriString ?: ""

        val effectiveLat = locationData?.latitude ?: latitude
        val effectiveLon = locationData?.longitude ?: longitude
        val effectiveAccuracy = locationData?.accuracyMeters ?: accuracyMeters
        val effectiveAddress = locationData?.address ?: address

        val coordsString = if (effectiveLat != null && effectiveLon != null && effectiveLat.isFinite() && effectiveLon.isFinite()) {
            val latDir = if (effectiveLat >= 0) "N" else "S"
            val lonDir = if (effectiveLon >= 0) "E" else "W"
            String.format(java.util.Locale.US, "%.4f° %s, %.4f° %s", Math.abs(effectiveLat), latDir, Math.abs(effectiveLon), lonDir)
        } else {
            "Location Selected by Citizen"
        }

        val created = Incident(
            id = newId,
            title = "$species $situation Emergency",
            species = species,
            situation = situation,
            urgency = urgency,
            animalCount = count,
            notes = notes,
            address = effectiveAddress,
            sector = "Municipal Response Sector",
            coordinates = coordsString,
            imageUrl = primaryPhotoUrl,
            evidenceList = evidenceList,
            currentStage = RescueStage.SUBMITTED,
            missionStatus = MissionStatus.ASSIGNED,
            reporterId = user?.userId ?: "ANON",
            reporterName = user?.name ?: "Anonymous Citizen",
            reporterPhone = user?.phone ?: "",
            assignedTeamId = null,
            assignedUnit = "Pending Dispatch",
            etaMinutes = 0,
            distanceKm = 0.0,
            timestamp = "Just now",
            latitude = effectiveLat,
            longitude = effectiveLon,
            accuracyMeters = effectiveAccuracy,
            locationData = locationData,
            aiAnalysis = aiAnalysis,
            status = "Submitted",
            createdAt = "Just now",
            updatedAt = "Just now"
        )
        _incidents.update { listOf(created) + it }
        _activeIncident.value = created

        // Add a notification for the reporter
        val newNotif = CitizenNotification(
            id = "NOTIF-${System.currentTimeMillis()}",
            title = "Complaint Submitted",
            message = "Your report for $species in $situation condition has been received.",
            time = "Just now",
            incidentId = newId,
            userId = user?.userId
        )
        _notifications.update { listOf(newNotif) + it }

        // Persist to Cloud Firestore
        FirebaseIncidentDataSource.persistIncident(created)
        FirebaseIncidentDataSource.persistNotification(newNotif)

        return created
    }

    fun mergeRemoteIncidents(remoteList: List<Incident>) {
        if (remoteList.isEmpty()) return
        _incidents.update { localList ->
            val localMap = localList.associateBy { it.id }.toMutableMap()
            for (remote in remoteList) {
                val local = localMap[remote.id]
                val merged = if (local != null) {
                    remote.copy(
                        evidenceList = if (remote.evidenceList.isNotEmpty()) remote.evidenceList else local.evidenceList,
                        locationData = remote.locationData ?: local.locationData,
                        aiAnalysis = remote.aiAnalysis ?: local.aiAnalysis
                    )
                } else {
                    remote
                }
                localMap[remote.id] = merged
            }
            localMap.values.sortedByDescending { it.id }
        }
    }

    fun mergeRemoteNotifications(remoteList: List<CitizenNotification>) {
        if (remoteList.isEmpty()) return
        _notifications.update { localList ->
            val map = localList.associateBy { it.id }.toMutableMap()
            for (n in remoteList) {
                map[n.id] = n
            }
            map.values.sortedByDescending { it.id }
        }
    }

    fun setVehicleEquipment(record: com.example.model.VehicleEquipmentRecord) {
        _vehicleEquipment.value = record
    }

    fun updateSystemSettings(settings: AdminSystemSettings) {
        _systemSettings.value = settings
    }

    fun loginWithGoogleSession(firebaseUser: com.google.firebase.auth.FirebaseUser, role: UserRole): UserSession {
        val uid = firebaseUser.uid
        val email = firebaseUser.email ?: "$uid@pawrescue.org"
        val name = firebaseUser.displayName ?: "PawRescue User"
        val photoUrl = firebaseUser.photoUrl?.toString() ?: ImageHotlinks.VOLUNTEER_PRIYA

        val session = UserSession(
            userId = uid,
            name = name,
            email = email,
            phone = firebaseUser.phoneNumber ?: "",
            role = role,
            status = AccountStatus.ACTIVE,
            teamId = if (role == UserRole.RESCUE_TEAM) "TEAM-GOOGLE" else null,
            teamName = if (role == UserRole.RESCUE_TEAM) "Unit Echo (Google Auth)" else null,
            organizationId = if (role == UserRole.ORGANIZATION_ADMIN) "ORG-METRO-01" else null,
            organizationName = if (role == UserRole.ORGANIZATION_ADMIN) "PawRescue Metro SAR Network" else null,
            authToken = "FIREBASE-AUTH-$uid",
            avatarUrl = photoUrl
        )
        _currentSession.value = session
        _currentRole.value = role

        val userAccount = UserAccount(
            id = uid,
            name = name,
            identifier = email,
            phone = session.phone,
            passwordHash = "",
            role = role,
            status = AccountStatus.ACTIVE,
            teamId = session.teamId,
            teamName = session.teamName,
            organizationId = session.organizationId,
            organizationName = session.organizationName,
            avatarUrl = photoUrl
        )
        if (!accounts.any { it.id == uid }) {
            accounts.add(userAccount)
        }
        return session
    }

    fun resetAllDataForTesting() {
        _incidents.value = emptyList()
        _activeIncident.value = null
        _notifications.value = emptyList()
    }
}
