package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.model.AccountStatus
import com.example.model.AdminSystemSettings
import com.example.model.CitizenFeedback
import com.example.model.CitizenNotification
import com.example.model.CitizenPreferences
import com.example.model.EquipmentItem
import com.example.model.EquipmentStatus
import com.example.model.Incident
import com.example.model.MissionStatus
import com.example.model.RescueReport
import com.example.model.RescueStage
import com.example.model.RescueTeamMember
import com.example.model.TimelineAuditRecord
import com.example.model.UrgencyLevel
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.model.VehicleEquipmentRecord
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirebaseIncidentDataSource {
    private const val TAG = "FirebaseDataSource"

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (_: Exception) {
            false
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            if (isFirebaseAvailable) FirebaseFirestore.getInstance() else null
        } catch (e: Exception) {
            Log.w(TAG, "Firestore not initialized yet: ${e.message}")
            null
        }

    private val auth: FirebaseAuth?
        get() = try {
            if (isFirebaseAvailable) FirebaseAuth.getInstance() else null
        } catch (e: Exception) {
            Log.w(TAG, "Auth not initialized: ${e.message}")
            null
        }

    private val storage: FirebaseStorage?
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

    fun initializeRealtimeSync(
        context: Context,
        onIncidentsReceived: (List<Incident>) -> Unit,
        onNotificationsReceived: (List<CitizenNotification>) -> Unit,
        onSettingsReceived: (AdminSystemSettings) -> Unit,
        onPreferencesReceived: (CitizenPreferences) -> Unit,
        onEquipmentReceived: (VehicleEquipmentRecord) -> Unit
    ) {
        val db = firestore ?: return

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
    }

    // Persist a new or updated incident to Cloud Firestore
    fun persistIncident(incident: Incident, onComplete: ((Boolean, String?) -> Unit)? = null) {
        val db = firestore
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
            "reporterUserId" to incident.reporterId,
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
        val db = firestore
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
                // Record in audit logs
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
        val db = firestore ?: return
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
        val db = firestore ?: return
        val payload = hashMapOf<String, Any?>(
            "id" to notification.id,
            "userId" to notification.userId,
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
        val db = firestore ?: return
        val payload = hashMapOf<String, Any?>(
            "userId" to userId,
            "notifyStatusUpdates" to prefs.notifyStatusUpdates,
            "notifyTeamAssigned" to prefs.notifyTeamAssigned,
            "notifyRescueCompleted" to prefs.notifyRescueCompleted,
            "notifyServiceAnnouncements" to prefs.notifyServiceAnnouncements,
            "preferredLanguage" to prefs.preferredLanguage,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("citizen_preferences").document(userId)
            .set(payload, SetOptions.merge())
    }

    // Persist vehicle & equipment record
    fun persistEquipment(record: VehicleEquipmentRecord) {
        val db = firestore ?: return
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
        val db = firestore ?: return
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
