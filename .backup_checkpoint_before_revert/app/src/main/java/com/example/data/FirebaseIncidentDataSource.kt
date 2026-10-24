package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.model.AccountStatus
import com.example.model.AdminSystemSettings
import com.example.model.AuthResult
import com.example.model.CitizenFeedback
import com.example.model.CitizenNotification
import com.example.model.CitizenPreferences
import com.example.model.EquipmentItem
import com.example.model.EquipmentStatus
import com.example.model.ImageHotlinks
import com.example.model.Incident
import com.example.model.MissionStatus
import com.example.model.RegistrationApprovalStatus
import com.example.model.RescueRegistrationRequest
import com.example.model.RescueReport
import com.example.model.RescueStage
import com.example.model.RescueTeamMember
import com.example.model.TimelineAuditRecord
import com.example.model.UrgencyLevel
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.model.VehicleEquipmentRecord
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

object FirebaseIncidentDataSource {
    private const val TAG = "FirebaseDataSource"

    private var appContext: Context? = null

    val isFirebaseAvailable: Boolean
        get() = try {
            val ctx = appContext
            if (ctx != null) {
                FirebaseApp.getApps(ctx).isNotEmpty()
            } else {
                FirebaseApp.getInstance() != null
            }
        } catch (_: Exception) {
            try {
                FirebaseApp.getInstance() != null
            } catch (_: Exception) {
                false
            }
        }

    fun getFirestore(): FirebaseFirestore? {
        if (!isFirebaseAvailable) return null
        return try {
            val ctx = appContext
            val dbId = if (ctx != null) {
                try {
                    ctx.getString(com.example.R.string.firestore_database_id)
                } catch (_: Exception) {
                    null
                }
            } else null

            if (!dbId.isNullOrBlank()) {
                FirebaseFirestore.getInstance(dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore initialization fallback: ${e.message}")
            try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }
        }
    }

    val auth: FirebaseAuth?
        get() = try {
            if (isFirebaseAvailable) FirebaseAuth.getInstance() else null
        } catch (e: Exception) {
            Log.w(TAG, "Auth not initialized: ${e.message}")
            null
        }

    val storage: FirebaseStorage?
        get() = try {
            if (isFirebaseAvailable) FirebaseStorage.getInstance() else null
        } catch (e: Exception) {
            Log.w(TAG, "Storage not initialized: ${e.message}")
            null
        }

    private var incidentsListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null
    private var settingsListener: ListenerRegistration? = null
    private var preferencesListener: ListenerRegistration? = null
    private var equipmentListener: ListenerRegistration? = null
    private var rescueRequestsListener: ListenerRegistration? = null

    fun initializeRealtimeSync(
        context: Context,
        onIncidentsReceived: (List<Incident>) -> Unit,
        onNotificationsReceived: (List<CitizenNotification>) -> Unit,
        onSettingsReceived: (AdminSystemSettings) -> Unit,
        onPreferencesReceived: (CitizenPreferences) -> Unit,
        onEquipmentReceived: (VehicleEquipmentRecord) -> Unit,
        onRescueRequestsReceived: ((List<RescueRegistrationRequest>) -> Unit)? = null
    ) {
        appContext = context.applicationContext
        val db = getFirestore() ?: return

        // 1. Incidents Realtime Observation
        try {
            incidentsListener?.remove()
            incidentsListener = db.collection("incidents")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Incidents listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val parsed = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                val id = doc.id
                                val title = data["title"] as? String ?: (data["incidentNumber"] as? String ?: id)
                                val species = data["species"] as? String ?: (data["animalType"] as? String ?: "Animal")
                                val situation = data["situation"] as? String ?: (data["conditionDescription"] as? String ?: "")
                                val urgencyStr = data["urgency"] as? String ?: "HIGH"
                                val urgency = try { UrgencyLevel.valueOf(urgencyStr) } catch (_: Exception) { UrgencyLevel.HIGH }
                                val statusStr = data["status"] as? String ?: "SUBMITTED"
                                val stage = try { RescueStage.valueOf(statusStr) } catch (_: Exception) { RescueStage.SUBMITTED }
                                val missionStatusStr = data["missionStatus"] as? String ?: "ASSIGNED"
                                val missionStatus = try { MissionStatus.valueOf(missionStatusStr) } catch (_: Exception) { MissionStatus.ASSIGNED }
                                val notes = data["notes"] as? String ?: ""
                                val address = data["address"] as? String ?: (data["locationAddress"] as? String ?: "")
                                val coordinates = data["coordinates"] as? String ?: ""
                                val imageUrl = data["imageUrl"] as? String ?: ""
                                val reporterId = data["reporterId"] as? String ?: (data["reporterUserId"] as? String ?: "")
                                val reporterName = data["reporterName"] as? String ?: ""
                                val reporterPhone = data["reporterPhone"] as? String ?: (data["reporterContact"] as? String ?: "")
                                val assignedTeamId = data["assignedTeamId"] as? String
                                val assignedUnit = data["assignedUnit"] as? String ?: (data["assignedTeamName"] as? String ?: "Pending Dispatch")
                                val timestamp = data["timestamp"] as? String ?: "Just now"

                                Incident(
                                    id = id,
                                    title = title,
                                    species = species,
                                    situation = situation,
                                    urgency = urgency,
                                    notes = notes,
                                    address = address,
                                    coordinates = coordinates,
                                    imageUrl = imageUrl,
                                    currentStage = stage,
                                    missionStatus = missionStatus,
                                    reporterId = reporterId,
                                    reporterName = reporterName,
                                    reporterPhone = reporterPhone,
                                    assignedTeamId = assignedTeamId,
                                    assignedUnit = assignedUnit,
                                    timestamp = timestamp,
                                    status = statusStr
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing incident ${doc.id}", e)
                                null
                            }
                        }
                        if (parsed.isNotEmpty()) {
                            onIncidentsReceived(parsed)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach incidents listener", e)
        }

        // 2. Notifications Realtime Observation
        try {
            notificationsListener?.remove()
            notificationsListener = db.collection("citizen_notifications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Notifications listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val parsed = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                CitizenNotification(
                                    id = doc.id,
                                    title = data["title"] as? String ?: "Alert",
                                    message = data["message"] as? String ?: "",
                                    time = data["time"] as? String ?: (data["timestamp"] as? String ?: "Just now"),
                                    incidentId = data["incidentId"] as? String,
                                    userId = data["userId"] as? String,
                                    isRead = data["isRead"] as? Boolean ?: false
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                        onNotificationsReceived(parsed)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach notifications listener", e)
        }

        // 3. Admin Settings Realtime Observation
        try {
            settingsListener?.remove()
            settingsListener = db.collection("admin_settings").document("default")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    try {
                        val data = snapshot.data ?: return@addSnapshotListener
                        val threshold = (data["urgencyThresholdHours"] as? Number)?.toFloat() ?: 0.85f
                        val hotline = data["hotlinePhone"] as? String ?: "+1 (800) 555-PAWS"
                        val requireAi = data["requireAiPhotoVerification"] as? Boolean ?: true
                        onSettingsReceived(
                            AdminSystemSettings(
                                aiConfidenceThreshold = threshold,
                                autoVerifyCritical = requireAi,
                                emergencyHotline = hotline
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing settings", e)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach settings listener", e)
        }
        // 4. Rescue Registration Requests Realtime Observation
        try {
            rescueRequestsListener?.remove()
            rescueRequestsListener = db.collection("rescueRegistrationRequests")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Rescue requests listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val parsed = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                val statusStr = (data["status"] as? String)?.uppercase() ?: "PENDING"
                                val status = try {
                                    RegistrationApprovalStatus.valueOf(statusStr)
                                } catch (_: Exception) {
                                    RegistrationApprovalStatus.PENDING
                                }
                                RescueRegistrationRequest(
                                    requestId = data["requestId"] as? String ?: doc.id,
                                    uid = data["uid"] as? String ?: "",
                                    fullName = data["fullName"] as? String ?: "",
                                    email = data["email"] as? String ?: "",
                                    phoneNumber = data["phoneNumber"] as? String ?: (data["phone"] as? String ?: ""),
                                    status = status,
                                    createdAt = data["createdAtFormatted"] as? String ?: "Recent",
                                    reviewedAt = data["reviewedAtFormatted"] as? String,
                                    reviewedBy = data["reviewedBy"] as? String,
                                    rejectionReason = data["rejectionReason"] as? String
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing rescue request ${doc.id}", e)
                                null
                            }
                        }
                        onRescueRequestsReceived?.invoke(parsed)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach rescue requests listener", e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out exception: ${e.message}")
        }
    }

    // Check and restore persistent Firebase Auth session on cold start
    fun checkAndRestoreSession(onComplete: (UserSession?) -> Unit) {
        val authClient = auth
        val currentUser = authClient?.currentUser
        if (currentUser == null) {
            onComplete(null)
            return
        }

        val uid = currentUser.uid
        val email = currentUser.email ?: ""
        val db = getFirestore()
        if (db == null) {
            val session = buildFallbackSession(uid, email)
            onComplete(session)
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val statusStr = doc.getString("status") ?: doc.getString("approvalStatus")
                    val status = AccountStatus.fromString(statusStr)

                    // A pending, rejected, or suspended applicant must never gain portal access unless approved in rescueRegistrationRequests
                    if (!status.isApproved) {
                        db.collection("rescueRegistrationRequests").whereEqualTo("email", email.lowercase().trim()).get()
                            .addOnSuccessListener { reqSnap ->
                                val matchingReq = reqSnap.documents.firstOrNull()
                                val reqStatus = matchingReq?.getString("status")?.trim()?.lowercase()
                                if (reqStatus == "approved") {
                                    val healedSession = UserSession(
                                        userId = uid,
                                        name = doc.getString("name") ?: (matchingReq.getString("fullName") ?: (if (email.isNotEmpty()) email.substringBefore("@") else "Rescue Squad")),
                                        email = email,
                                        phone = doc.getString("phone") ?: (matchingReq.getString("phoneNumber") ?: ""),
                                        role = UserRole.RESCUE_TEAM,
                                        status = AccountStatus.APPROVED,
                                        teamId = "TEAM-BETA-3",
                                        teamName = "Unit Beta-3 (Rapid Van)",
                                        authToken = "SESSION-$uid",
                                        avatarUrl = ImageHotlinks.RESCUE_VAN_CREW
                                    )
                                    val userUpdates = hashMapOf<String, Any?>(
                                        "id" to uid,
                                        "name" to healedSession.name,
                                        "email" to email,
                                        "phone" to healedSession.phone,
                                        "status" to "approved",
                                        "approvalStatus" to "approved",
                                        "role" to "rescue_team",
                                        "teamId" to "TEAM-BETA-3",
                                        "teamName" to "Unit Beta-3 (Rapid Van)",
                                        "updatedAt" to FieldValue.serverTimestamp()
                                    )
                                    db.collection("users").document(uid).set(userUpdates, SetOptions.merge())
                                    onComplete(healedSession)
                                } else {
                                    Log.w(TAG, "Denying session restoration for account $uid with status $status (reqStatus: $reqStatus)")
                                    onComplete(null)
                                }
                            }
                            .addOnFailureListener {
                                Log.w(TAG, "Denying session restoration for account $uid with status $status")
                                onComplete(null)
                            }
                        return@addOnSuccessListener
                    }

                    val roleStr = doc.getString("role") ?: "CITIZEN"
                    val role = parseRole(roleStr)
                    val name = doc.getString("name") ?: (if (email.isNotEmpty()) email.substringBefore("@") else "PawRescue User")
                    val phone = doc.getString("phone") ?: ""
                    val teamId = doc.getString("teamId")
                    val teamName = doc.getString("teamName")
                    val orgId = doc.getString("organizationId")
                    val orgName = doc.getString("organizationName")
                    val avatarUrl = doc.getString("avatarUrl") ?: if (role == UserRole.RESCUE_TEAM) ImageHotlinks.RESCUE_VAN_CREW else ImageHotlinks.VOLUNTEER_PRIYA

                    val session = UserSession(
                        userId = uid,
                        name = name,
                        email = email,
                        phone = phone,
                        role = role,
                        status = status,
                        teamId = teamId,
                        teamName = teamName,
                        organizationId = orgId,
                        organizationName = orgName,
                        authToken = "SESSION-$uid",
                        avatarUrl = avatarUrl
                    )
                    onComplete(session)
                } else {
                    val lower = email.lowercase().trim()
                    if (lower == "jeswanth746@gmail.com" || lower == "jayasuryageetha@gmail.com") {
                        val repaired = buildFallbackSession(uid, email)
                        persistUserAccount(repaired)
                        onComplete(repaired)
                    } else {
                        db.collection("rescueRegistrationRequests").whereEqualTo("email", lower).get()
                            .addOnSuccessListener { reqSnap ->
                                val matchingReq = reqSnap.documents.firstOrNull()
                                if (matchingReq?.getString("status")?.equals("approved", ignoreCase = true) == true) {
                                    val repaired = UserSession(
                                        userId = uid,
                                        name = matchingReq.getString("fullName") ?: (if (email.isNotEmpty()) email.substringBefore("@") else "Rescue Squad"),
                                        email = email,
                                        phone = matchingReq.getString("phoneNumber") ?: "",
                                        role = UserRole.RESCUE_TEAM,
                                        status = AccountStatus.APPROVED,
                                        teamId = "TEAM-BETA-3",
                                        teamName = "Unit Beta-3 (Rapid Van)",
                                        authToken = "SESSION-$uid",
                                        avatarUrl = ImageHotlinks.RESCUE_VAN_CREW
                                    )
                                    persistUserAccount(repaired)
                                    onComplete(repaired)
                                } else {
                                    onComplete(null)
                                }
                            }
                            .addOnFailureListener {
                                onComplete(null)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to fetch profile during session restore: ${e.message}")
                onComplete(null)
            }
    }

    private fun parseRole(roleStr: String): UserRole {
        return try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            when {
                roleStr.equals("rescue_team", ignoreCase = true) -> UserRole.RESCUE_TEAM
                roleStr.equals("organization_admin", ignoreCase = true) -> UserRole.ORGANIZATION_ADMIN
                else -> UserRole.CITIZEN
            }
        }
    }

    private fun buildFallbackSession(uid: String, email: String): UserSession {
        val lowerEmail = email.lowercase().trim()
        val role = when {
            lowerEmail == "jeswanth746@gmail.com" -> UserRole.RESCUE_TEAM
            lowerEmail == "jayasuryageetha@gmail.com" -> UserRole.ORGANIZATION_ADMIN
            else -> UserRole.CITIZEN
        }
        val isRescue = role == UserRole.RESCUE_TEAM
        val isAdmin = role == UserRole.ORGANIZATION_ADMIN

        return UserSession(
            userId = uid,
            name = when {
                isRescue -> "Jeswanth (Rescue Squad Lead)"
                isAdmin -> "Jayasurya (Org Administrator)"
                lowerEmail.contains("@") -> lowerEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                else -> "Citizen User"
            },
            email = email,
            phone = if (isRescue) "+1 (555) 746-0199" else if (isAdmin) "+1 (800) 555-PAWS" else "",
            role = role,
            status = AccountStatus.ACTIVE,
            teamId = if (isRescue) "TEAM-BETA-3" else null,
            teamName = if (isRescue) "Unit Beta-3 (Rapid Van)" else null,
            organizationId = if (isAdmin) "ORG-METRO-01" else null,
            organizationName = if (isAdmin) "PawRescue Metro SAR Network" else null,
            authToken = "SESSION-$uid",
            avatarUrl = if (isRescue) ImageHotlinks.RESCUE_VAN_CREW else ImageHotlinks.VOLUNTEER_PRIYA
        )
    }

    fun authenticateWithFirebase(
        targetPortal: UserRole,
        identifier: String,
        pass: String,
        onResult: (AuthResult) -> Unit
    ) {
        val trimmedId = identifier.trim()
        val trimmedEmail = trimmedId.lowercase()
        val trimmedPass = pass.trim()
        val authClient = auth

        if (trimmedId.isEmpty() || trimmedPass.isEmpty()) {
            onResult(AuthResult.InvalidCredentialsError("Please provide both email/ID and password."))
            return
        }

        // Check if this matches a local in-memory account first (for offline tests or phone/ID logins)
        val localAccount = IncidentRepository.findLocalAccount(trimmedId)

        // If authClient is null or identifier is not an email, authenticate against local store
        if (authClient == null || !trimmedId.contains("@")) {
            val localResult = IncidentRepository.authenticateLocally(targetPortal, trimmedId, trimmedPass)
            onResult(localResult)
            return
        }

        // Perform real Firebase Authentication
        authClient.signInWithEmailAndPassword(trimmedEmail, trimmedPass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                val db = getFirestore()
                if (db != null) {
                    fun proceedWithSession(
                        effectiveUid: String,
                        doc: com.google.firebase.firestore.DocumentSnapshot?,
                        role: UserRole,
                        status: AccountStatus
                    ) {
                        val name = doc?.getString("name") ?: (localAccount?.name ?: trimmedEmail.substringBefore("@"))
                        val phone = doc?.getString("phone") ?: (localAccount?.phone ?: "")

                        val session = UserSession(
                            userId = effectiveUid,
                            name = name,
                            email = trimmedEmail,
                            phone = phone,
                            role = role,
                            status = status,
                            teamId = if (role == UserRole.RESCUE_TEAM) (doc?.getString("teamId") ?: "TEAM-BETA-3") else null,
                            teamName = if (role == UserRole.RESCUE_TEAM) (doc?.getString("teamName") ?: "Unit Beta-3 (Rapid Van)") else null,
                            organizationId = if (role == UserRole.ORGANIZATION_ADMIN) (doc?.getString("organizationId") ?: "ORG-METRO-01") else null,
                            organizationName = if (role == UserRole.ORGANIZATION_ADMIN) (doc?.getString("organizationName") ?: "PawRescue Metro SAR Network") else null,
                            authToken = "FIREBASE-TOKEN-$effectiveUid",
                            avatarUrl = doc?.getString("avatarUrl") ?: if (role == UserRole.RESCUE_TEAM) ImageHotlinks.RESCUE_VAN_CREW else ImageHotlinks.VOLUNTEER_PRIYA
                        )

                        if (doc == null || !doc.exists()) {
                            persistUserAccount(session)
                        }

                        IncidentRepository.setCurrentSession(session)
                        onResult(AuthResult.Success(session))
                    }

                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val rawStatus = doc?.getString("status") ?: doc?.getString("approvalStatus")
                            var status = AccountStatus.fromString(rawStatus)
                            var role = parseRole(doc?.getString("role") ?: "CITIZEN")

                            if (targetPortal == UserRole.RESCUE_TEAM) {
                                // If the user document does not reflect approval, cross-verify with rescueRegistrationRequests
                                if (!status.isApproved || role != UserRole.RESCUE_TEAM) {
                                    db.collection("rescueRegistrationRequests").whereEqualTo("email", trimmedEmail).get()
                                        .addOnSuccessListener { reqSnap ->
                                            val matchingReq = reqSnap.documents.firstOrNull()
                                            val reqStatus = matchingReq?.getString("status")?.trim()?.lowercase()

                                            if (reqStatus == "approved") {
                                                status = AccountStatus.APPROVED
                                                role = UserRole.RESCUE_TEAM
                                                val healedName = doc?.getString("name") ?: (matchingReq.getString("fullName") ?: trimmedEmail.substringBefore("@"))
                                                val healedPhone = doc?.getString("phone") ?: (matchingReq.getString("phoneNumber") ?: "")

                                                val userUpdates = hashMapOf<String, Any?>(
                                                    "id" to uid,
                                                    "name" to healedName,
                                                    "email" to trimmedEmail,
                                                    "phone" to healedPhone,
                                                    "status" to "approved",
                                                    "approvalStatus" to "approved",
                                                    "role" to "rescue_team",
                                                    "teamId" to "TEAM-BETA-3",
                                                    "teamName" to "Unit Beta-3 (Rapid Van)",
                                                    "updatedAt" to FieldValue.serverTimestamp()
                                                )
                                                db.collection("users").document(uid).set(userUpdates, SetOptions.merge())
                                                proceedWithSession(uid, doc, role, status)
                                            } else if (reqStatus == "rejected") {
                                                onResult(
                                                    AuthResult.AccountInactiveError(
                                                        "Your Rescue Team registration request has been rejected. Please contact the organization for further information."
                                                    )
                                                )
                                            } else {
                                                onResult(
                                                    AuthResult.AccountInactiveError(
                                                        "Your account is awaiting Organization Admin approval. You cannot log in until your registration is approved."
                                                    )
                                                )
                                            }
                                        }
                                        .addOnFailureListener {
                                            if (status == AccountStatus.PENDING_APPROVAL) {
                                                onResult(
                                                    AuthResult.AccountInactiveError(
                                                        "Your account is awaiting Organization Admin approval. You cannot log in until your registration is approved."
                                                    )
                                                )
                                            } else if (status == AccountStatus.REJECTED) {
                                                onResult(
                                                    AuthResult.AccountInactiveError(
                                                        "Your Rescue Team registration request has been rejected. Please contact the organization for further information."
                                                    )
                                                )
                                            } else {
                                                onResult(
                                                    AuthResult.InvalidCredentialsError(
                                                        "Unable to verify account authorization due to a network error. Please check your connection and tap to retry."
                                                    )
                                                )
                                            }
                                        }
                                    return@addOnSuccessListener
                                }
                            }

                            // 1. Account Approval Status Enforcement
                            if (status == AccountStatus.PENDING_APPROVAL) {
                                onResult(
                                    AuthResult.AccountInactiveError(
                                        "Your account is awaiting Organization Admin approval. You cannot log in until your registration is approved."
                                    )
                                )
                                return@addOnSuccessListener
                            }

                            if (status == AccountStatus.REJECTED) {
                                onResult(
                                    AuthResult.AccountInactiveError(
                                        "Your Rescue Team registration request has been rejected. Please contact the organization for further information."
                                    )
                                )
                                return@addOnSuccessListener
                            }

                            if (status == AccountStatus.SUSPENDED) {
                                onResult(
                                    AuthResult.AccountInactiveError(
                                        "Access Denied: Account status is 'SUSPENDED'. Please contact the Organization Administrator to activate your account."
                                    )
                                )
                                return@addOnSuccessListener
                            }

                            // 2. Strict Role Enforcement
                            if (role != targetPortal) {
                                onResult(
                                    AuthResult.WrongPortalError(
                                        expectedRole = targetPortal,
                                        actualRole = role,
                                        message = "Access Denied: Your account role is '${role.title}'. You cannot log in through the ${targetPortal.title}."
                                    )
                                )
                                return@addOnSuccessListener
                            }

                            proceedWithSession(uid, doc, role, status)
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to verify account authorization with Firestore: ${e.message}")
                            // Deny dashboard access temporarily and show retry option; do not assume account is approved
                            onResult(
                                AuthResult.InvalidCredentialsError(
                                    "Unable to verify account authorization due to a network error. Please check your connection and tap to retry."
                                )
                            )
                        }
                } else {
                    val localResult = IncidentRepository.authenticateLocally(targetPortal, trimmedId, trimmedPass)
                    onResult(localResult)
                }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Firebase signIn failed: ${error.message}")
                val isRequestedRescue = trimmedEmail == "jeswanth746@gmail.com" && targetPortal == UserRole.RESCUE_TEAM
                val isRequestedAdmin = trimmedEmail == "jayasuryageetha@gmail.com" && targetPortal == UserRole.ORGANIZATION_ADMIN

                if ((isRequestedRescue || isRequestedAdmin) && (error is com.google.firebase.auth.FirebaseAuthInvalidUserException || error.message?.contains("no user record", ignoreCase = true) == true)) {
                    // Automatically provision the designated team account in Firebase Auth
                    authClient.createUserWithEmailAndPassword(trimmedEmail, trimmedPass)
                        .addOnSuccessListener { regResult ->
                            val uid = regResult.user?.uid ?: "USER-${System.currentTimeMillis()}"
                            val session = buildFallbackSession(uid, trimmedEmail)
                            persistUserAccount(session)
                            IncidentRepository.setCurrentSession(session)
                            onResult(AuthResult.Success(session))
                        }
                        .addOnFailureListener {
                            val localResult = IncidentRepository.authenticateLocally(targetPortal, trimmedId, trimmedPass)
                            onResult(localResult)
                        }
                    return@addOnFailureListener
                }

                if (error is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                    onResult(AuthResult.InvalidCredentialsError("Incorrect password entered. Please try again."))
                    return@addOnFailureListener
                }

                if (error is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                    // Check local accounts (e.g. for offline testing)
                    val localResult = IncidentRepository.authenticateLocally(targetPortal, trimmedId, trimmedPass)
                    onResult(localResult)
                    return@addOnFailureListener
                }

                // General network or connection failure: check local cache/accounts fallback
                val localResult = IncidentRepository.authenticateLocally(targetPortal, trimmedId, trimmedPass)
                if (localResult is AuthResult.Success) {
                    onResult(localResult)
                } else {
                    onResult(AuthResult.InvalidCredentialsError(error.localizedMessage ?: "Sign in failed. Please check your internet connection."))
                }
            }
    }

    fun registerCitizenWithFirebase(
        name: String,
        emailOrPhone: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        val trimmedName = name.trim()
        val trimmedContact = emailOrPhone.trim()
        val trimmedEmail = trimmedContact.lowercase()
        val trimmedPass = password.trim()
        val authClient = auth

        if (trimmedName.isEmpty() || trimmedContact.isEmpty() || trimmedPass.length < 6) {
            onResult(AuthResult.InvalidCredentialsError("Please provide full name, contact, and password (at least 6 characters)."))
            return
        }

        if (authClient == null || !trimmedContact.contains("@")) {
            val localResult = IncidentRepository.signUpCitizen(trimmedName, trimmedContact, trimmedPass)
            onResult(localResult)
            return
        }

        authClient.createUserWithEmailAndPassword(trimmedEmail, trimmedPass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: "CIT-${System.currentTimeMillis()}"
                val session = UserSession(
                    userId = uid,
                    name = trimmedName,
                    email = trimmedEmail,
                    phone = if (trimmedContact != trimmedEmail) trimmedContact else "",
                    role = UserRole.CITIZEN,
                    status = AccountStatus.ACTIVE,
                    teamId = null,
                    teamName = null,
                    organizationId = null,
                    organizationName = null,
                    authToken = "FIREBASE-TOKEN-$uid",
                    avatarUrl = ImageHotlinks.VOLUNTEER_PRIYA
                )

                // Persist citizen profile in Cloud Firestore using authenticated UID
                persistUserAccount(session)

                // Mirror to local repository accounts
                IncidentRepository.addLocalAccount(session, trimmedPass)
                IncidentRepository.setCurrentSession(session)
                onResult(AuthResult.Success(session))
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Citizen registration failed: ${error.message}")
                if (error is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                    onResult(AuthResult.InvalidCredentialsError("An account already exists for '$trimmedEmail'. Please log in."))
                } else {
                    // If offline or network unavailable, register locally so user can continue
                    val localResult = IncidentRepository.signUpCitizen(trimmedName, trimmedContact, trimmedPass)
                    if (localResult is AuthResult.Success) {
                        onResult(localResult)
                    } else {
                        onResult(AuthResult.InvalidCredentialsError(error.localizedMessage ?: "Registration failed."))
                    }
                }
            }
    }

    // Submit Rescue Team registration request with initial pending approval status
    fun submitRescueRegistrationRequest(
        fullName: String,
        email: String,
        phone: String,
        passwordPlain: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim().lowercase()
        val trimmedPhone = phone.trim()
        val trimmedPass = passwordPlain.trim()

        if (trimmedName.isEmpty() || trimmedEmail.isEmpty() || trimmedPhone.isEmpty() || trimmedPass.length < 6) {
            onResult(false, "Please complete all fields with a valid password (at least 6 characters).")
            return
        }

        val requestId = "REQ-" + (System.currentTimeMillis() % 100000).toString()
        val nowFormatted = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.US).format(java.util.Date())

        val authClient = auth
        val db = getFirestore()

        if (authClient == null || db == null) {
            IncidentRepository.submitRescueTeamRegistration(trimmedName, trimmedEmail, trimmedPhone, trimmedPass, requestId)
            onResult(true, null)
            return
        }

        authClient.createUserWithEmailAndPassword(trimmedEmail, trimmedPass)
            .addOnSuccessListener { regResult ->
                val uid = regResult.user?.uid ?: "RESCUE-${System.currentTimeMillis()}"

                // 1. Create Firestore user document with role CITIZEN, requestedRole RESCUE_TEAM and status PENDING_APPROVAL
                val userPayload = hashMapOf<String, Any?>(
                    "id" to uid,
                    "name" to trimmedName,
                    "email" to trimmedEmail,
                    "phone" to trimmedPhone,
                    "role" to "CITIZEN",
                    "requestedRole" to "RESCUE_TEAM",
                    "status" to "PENDING_APPROVAL",
                    "approvalStatus" to "pending",
                    "avatarUrl" to ImageHotlinks.RESCUE_VAN_CREW,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                // 2. Create rescueRegistrationRequests document with status pending
                val requestPayload = hashMapOf<String, Any?>(
                    "id" to requestId,
                    "requestId" to requestId,
                    "uid" to uid,
                    "fullName" to trimmedName,
                    "email" to trimmedEmail,
                    "phoneNumber" to trimmedPhone,
                    "status" to "pending",
                    "createdAtFormatted" to nowFormatted,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                db.collection("users").document(uid).set(userPayload, SetOptions.merge())
                    .addOnCompleteListener {
                        db.collection("rescueRegistrationRequests").document(requestId)
                            .set(requestPayload, SetOptions.merge())
                            .addOnCompleteListener {
                                // Immediately sign out so applicant is not kept in an active unapproved session
                                authClient.signOut()
                                IncidentRepository.submitRescueTeamRegistration(trimmedName, trimmedEmail, trimmedPhone, trimmedPass, requestId, uid)
                                onResult(true, null)
                            }
                    }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Failed to create auth user for rescue team: ${error.message}")
                if (error is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                    onResult(false, "An account or registration request already exists for '$trimmedEmail'.")
                } else {
                    IncidentRepository.submitRescueTeamRegistration(trimmedName, trimmedEmail, trimmedPhone, trimmedPass, requestId)
                    onResult(true, null)
                }
            }
    }

    // Approve Rescue Team Registration by Organization Admin
    fun approveRescueRegistrationRequest(
        request: RescueRegistrationRequest,
        adminSession: UserSession,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val nowFormatted = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.US).format(java.util.Date())
        IncidentRepository.approveRescueRequest(request.requestId, adminSession.name, adminSession.userId)

        val db = getFirestore()
        if (db == null) {
            onComplete(true, null)
            return
        }

        val requestUpdates = hashMapOf<String, Any?>(
            "status" to "approved",
            "reviewedAtFormatted" to nowFormatted,
            "reviewedBy" to adminSession.name,
            "reviewedByUid" to adminSession.userId,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("rescueRegistrationRequests").document(request.requestId)
            .set(requestUpdates, SetOptions.merge())
            .addOnSuccessListener {
                val targetUid = request.uid.trim()
                val email = request.email.trim().lowercase()

                fun updateUserDoc(resolvedUid: String) {
                    val userUpdates = hashMapOf<String, Any?>(
                        "id" to resolvedUid,
                        "name" to request.fullName,
                        "email" to email,
                        "phone" to request.phoneNumber,
                        "status" to "APPROVED",
                        "approvalStatus" to "approved",
                        "role" to "RESCUE_TEAM",
                        "teamId" to "TEAM-BETA-3",
                        "teamName" to "Unit Beta-3 (Rapid Van)",
                        "reviewedBy" to adminSession.name,
                        "reviewedByUid" to adminSession.userId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(resolvedUid)
                        .set(userUpdates, SetOptions.merge())
                        .addOnCompleteListener {
                            val auditPayload = hashMapOf<String, Any?>(
                                "action" to "Rescue Team Registration Approved",
                                "adminName" to adminSession.name,
                                "adminUid" to adminSession.userId,
                                "targetRequest" to request.requestId,
                                "targetEmail" to email,
                                "targetName" to request.fullName,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            db.collection("audit_logs").add(auditPayload)
                            onComplete(true, null)
                        }
                }

                if (targetUid.isNotBlank()) {
                    updateUserDoc(targetUid)
                } else {
                    db.collection("users").whereEqualTo("email", email).get()
                        .addOnSuccessListener { querySnap ->
                            val foundUid = querySnap.documents.firstOrNull()?.id
                            if (!foundUid.isNullOrBlank()) {
                                db.collection("rescueRegistrationRequests").document(request.requestId)
                                    .update("uid", foundUid)
                                updateUserDoc(foundUid)
                            } else {
                                onComplete(true, null)
                            }
                        }
                        .addOnFailureListener {
                            onComplete(true, null)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to approve rescue registration request ${request.requestId}", e)
                onComplete(false, e.localizedMessage ?: "Failed to update database")
            }
    }

    // Reject Rescue Team Registration by Organization Admin
    fun rejectRescueRegistrationRequest(
        request: RescueRegistrationRequest,
        reason: String?,
        adminSession: UserSession,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val effectiveReason = reason?.trim().takeIf { !it.isNullOrBlank() } ?: "Application did not meet current rescue squad requirements."
        val nowFormatted = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.US).format(java.util.Date())
        IncidentRepository.rejectRescueRequest(request.requestId, effectiveReason, adminSession.name, adminSession.userId)

        val db = getFirestore()
        if (db == null) {
            onComplete(true, null)
            return
        }

        val requestUpdates = hashMapOf<String, Any?>(
            "status" to "rejected",
            "rejectionReason" to effectiveReason,
            "reviewedAtFormatted" to nowFormatted,
            "reviewedBy" to adminSession.name,
            "reviewedByUid" to adminSession.userId,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("rescueRegistrationRequests").document(request.requestId)
            .set(requestUpdates, SetOptions.merge())
            .addOnSuccessListener {
                val targetUid = request.uid.trim()
                val email = request.email.trim().lowercase()

                fun rejectUserDoc(resolvedUid: String) {
                    val userUpdates = hashMapOf<String, Any?>(
                        "id" to resolvedUid,
                        "name" to request.fullName,
                        "email" to email,
                        "status" to "REJECTED",
                        "approvalStatus" to "rejected",
                        "rejectionReason" to effectiveReason,
                        "reviewedBy" to adminSession.name,
                        "reviewedByUid" to adminSession.userId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(resolvedUid)
                        .set(userUpdates, SetOptions.merge())
                        .addOnCompleteListener {
                            val auditPayload = hashMapOf<String, Any?>(
                                "action" to "Rescue Team Registration Rejected",
                                "adminName" to adminSession.name,
                                "adminUid" to adminSession.userId,
                                "targetRequest" to request.requestId,
                                "targetEmail" to email,
                                "targetName" to request.fullName,
                                "reason" to effectiveReason,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            db.collection("audit_logs").add(auditPayload)
                            onComplete(true, null)
                        }
                }

                if (targetUid.isNotBlank()) {
                    rejectUserDoc(targetUid)
                } else {
                    db.collection("users").whereEqualTo("email", email).get()
                        .addOnSuccessListener { querySnap ->
                            val foundUid = querySnap.documents.firstOrNull()?.id
                            if (!foundUid.isNullOrBlank()) {
                                db.collection("rescueRegistrationRequests").document(request.requestId)
                                    .update("uid", foundUid)
                                rejectUserDoc(foundUid)
                            } else {
                                onComplete(true, null)
                            }
                        }
                        .addOnFailureListener {
                            onComplete(true, null)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to reject rescue registration request ${request.requestId}", e)
                onComplete(false, e.localizedMessage ?: "Failed to update database")
            }
    }

    // Background synchronization of authenticated user profile with Firebase Auth and Firestore
    fun syncAuthAndProfile(session: UserSession, passwordPlain: String) {
        val authClient = auth ?: return
        val email = session.email
        if (!email.contains("@")) return

        authClient.signInWithEmailAndPassword(email, passwordPlain)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: session.userId
                persistUserAccount(session.copy(userId = uid))
            }
            .addOnFailureListener {
                // If account does not exist in Firebase Auth, auto-provision it
                authClient.createUserWithEmailAndPassword(email, passwordPlain)
                    .addOnSuccessListener { createResult ->
                        val uid = createResult.user?.uid ?: session.userId
                        persistUserAccount(session.copy(userId = uid))
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Auth sync note: ${e.message}")
                        persistUserAccount(session)
                    }
            }
    }

    // Persist a new or updated incident to Cloud Firestore
    fun persistIncident(incident: Incident, onComplete: ((Boolean, String?) -> Unit)? = null) {
        val db = getFirestore()
        if (db == null) {
            onComplete?.invoke(false, "Firestore not available")
            return
        }

        val payload = hashMapOf<String, Any?>(
            "id" to incident.id,
            "incidentNumber" to (if (incident.title.startsWith("SAR-")) incident.title else incident.id),
            "title" to incident.title,
            "species" to incident.species,
            "animalType" to incident.species,
            "situation" to incident.situation,
            "conditionDescription" to incident.situation,
            "urgency" to incident.urgency.name,
            "notes" to incident.notes,
            "address" to incident.address,
            "locationAddress" to incident.address,
            "coordinates" to incident.coordinates,
            "imageUrl" to incident.imageUrl,
            "status" to incident.currentStage.name,
            "missionStatus" to incident.missionStatus.name,
            "reporterUserId" to (auth?.currentUser?.uid ?: incident.reporterId),
            "reporterId" to incident.reporterId,
            "reporterName" to incident.reporterName,
            "reporterContact" to incident.reporterPhone,
            "reporterPhone" to incident.reporterPhone,
            "assignedTeamId" to incident.assignedTeamId,
            "assignedUnit" to incident.assignedUnit,
            "assignedTeamName" to incident.assignedUnit,
            "timestamp" to incident.timestamp,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("incidents").document(incident.id)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Incident ${incident.id} persisted to Cloud Firestore successfully")
                onComplete?.invoke(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to persist incident ${incident.id} to Firestore", e)
                onComplete?.invoke(false, e.localizedMessage)
            }
    }

    // Persist mission assignment to Cloud Firestore
    fun persistAssignment(
        incidentId: String,
        teamId: String,
        teamName: String,
        vehicleId: String?,
        responders: String?,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        val db = getFirestore()
        if (db == null) {
            onComplete?.invoke(false, "Firestore not available")
            return
        }

        val updates = hashMapOf<String, Any?>(
            "assignedTeamId" to teamId,
            "assignedTeamName" to teamName,
            "assignedUnit" to teamName,
            "assignedVehicleId" to vehicleId,
            "assignedResponders" to responders,
            "status" to "ASSIGNED",
            "missionStatus" to MissionStatus.ASSIGNED.name,
            "assignedAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("incidents").document(incidentId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Assignment for $incidentId saved to Firestore")
                val auditPayload = hashMapOf<String, Any?>(
                    "id" to "AUD-${System.currentTimeMillis()}",
                    "incidentId" to incidentId,
                    "action" to "SQUAD_ASSIGNED",
                    "performedBy" to "Admin Dispatch HQ",
                    "details" to "Assigned to $teamName ($vehicleId)",
                    "timestamp" to FieldValue.serverTimestamp()
                )
                db.collection("audit_logs").add(auditPayload)
                onComplete?.invoke(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save assignment for $incidentId", e)
                onComplete?.invoke(false, e.localizedMessage)
            }
    }

    // Persist mission status update to Cloud Firestore
    fun persistMissionStatus(
        incidentId: String,
        stage: RescueStage,
        missionStatus: MissionStatus,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val db = getFirestore() ?: return
        val updates = hashMapOf<String, Any?>(
            "status" to stage.name,
            "missionStatus" to missionStatus.name,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("incidents").document(incidentId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    // Persist notification to Cloud Firestore
    fun persistNotification(notification: CitizenNotification) {
        val db = getFirestore() ?: return
        val payload = hashMapOf<String, Any?>(
            "id" to notification.id,
            "userId" to (auth?.currentUser?.uid ?: notification.userId),
            "title" to notification.title,
            "message" to notification.message,
            "time" to notification.time,
            "incidentId" to notification.incidentId,
            "isRead" to notification.isRead,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("citizen_notifications").document(notification.id)
            .set(payload, SetOptions.merge())
    }

    // Persist citizen preferences to Cloud Firestore
    fun persistPreferences(userId: String, prefs: CitizenPreferences) {
        val db = getFirestore() ?: return
        val targetUid = auth?.currentUser?.uid ?: userId
        val payload = hashMapOf<String, Any?>(
            "userId" to targetUid,
            "notifyStatusUpdates" to prefs.notifyStatusUpdates,
            "notifyTeamAssigned" to prefs.notifyTeamAssigned,
            "notifyRescueCompleted" to prefs.notifyRescueCompleted,
            "notifyServiceAnnouncements" to prefs.notifyServiceAnnouncements,
            "preferredLanguage" to prefs.preferredLanguage,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("citizen_preferences").document(targetUid)
            .set(payload, SetOptions.merge())
    }

    // Persist vehicle & equipment record
    fun persistEquipment(record: VehicleEquipmentRecord) {
        val db = getFirestore() ?: return
        val payload = hashMapOf<String, Any?>(
            "id" to record.vehicleId,
            "vehicleId" to record.vehicleId,
            "modelDescription" to record.modelDescription,
            "registrationPlate" to record.registrationPlate,
            "readinessStatus" to record.readinessStatus,
            "lastCheckedAt" to FieldValue.serverTimestamp()
        )
        db.collection("vehicle_equipment").document(record.vehicleId)
            .set(payload, SetOptions.merge())
    }

    // Persist rescue completion report
    fun persistRescueReport(report: RescueReport) {
        val db = getFirestore() ?: return
        val payload = hashMapOf<String, Any?>(
            "id" to "REP-${report.incidentId}",
            "incidentId" to report.incidentId,
            "teamId" to "TEAM-BETA-3",
            "disposition" to report.outcome,
            "clinicName" to report.treatmentShelter,
            "notes" to report.rescueNotes,
            "actionTaken" to report.actionTaken,
            "completedAt" to FieldValue.serverTimestamp()
        )
        db.collection("rescue_reports").document("REP-${report.incidentId}")
            .set(payload, SetOptions.merge())
    }

    // Persist user account in Cloud Firestore
    fun persistUserAccount(session: UserSession) {
        val db = getFirestore() ?: return
        val payload = hashMapOf<String, Any?>(
            "id" to session.userId,
            "name" to session.name,
            "email" to session.email,
            "phone" to session.phone,
            "role" to session.role.name,
            "status" to session.status.name,
            "teamId" to session.teamId,
            "teamName" to session.teamName,
            "organizationId" to session.organizationId,
            "organizationName" to session.organizationName,
            "avatarUrl" to session.avatarUrl,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(session.userId)
            .set(payload, SetOptions.merge())
    }

    // Send password reset email via Firebase Auth
    fun sendPasswordReset(email: String, onComplete: (Boolean, String?) -> Unit) {
        val authClient = auth
        val trimmed = email.trim()
        if (authClient != null && trimmed.contains("@")) {
            authClient.sendPasswordResetEmail(trimmed)
                .addOnSuccessListener {
                    onComplete(true, "Password reset link sent to $trimmed.")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Password reset note: ${e.message}")
                    onComplete(true, "Password reset instructions dispatched to $trimmed.")
                }
        } else {
            onComplete(true, "Password reset instructions dispatched to $trimmed.")
        }
    }

    // Upload photo to Firebase Storage
    fun uploadEvidencePhoto(
        uri: Uri,
        incidentId: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val stor = storage
        if (stor == null) {
            onFailure("Firebase Storage is not available")
            return
        }

        val ref = stor.reference.child("incidents/$incidentId/evidence_${System.currentTimeMillis()}.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }.addOnFailureListener { e ->
                    onFailure(e.localizedMessage ?: "Failed to get download URL")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.localizedMessage ?: "Upload failed")
            }
    }
}
