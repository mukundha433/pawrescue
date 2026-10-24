package com.example

import com.example.data.FirebaseIncidentDataSource
import com.example.data.IncidentRepository
import com.example.model.AccountStatus
import com.example.model.CitizenNotification
import com.example.model.CitizenPreferences
import com.example.model.MissionStatus
import com.example.model.RescueReport
import com.example.model.RescueStage
import com.example.model.UrgencyLevel
import com.example.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FirebaseIntegrationTest {

    @Before
    fun setup() {
        IncidentRepository.resetAllDataForTesting()
    }

    @Test
    fun testCitizenIncidentCreationAndPersistence() {
        // Authenticate citizen
        val authResult = IncidentRepository.authenticate(UserRole.CITIZEN, "elena@pawrescue.org", "citizen123")
        assertTrue("Citizen authentication must succeed", authResult is com.example.model.AuthResult.Success)

        // Submit new intake incident
        val incident = IncidentRepository.addNewIntake(
            species = "Canine",
            situation = "Fractured limb near highway overpass",
            urgency = UrgencyLevel.CRITICAL,
            count = 1,
            notes = "Animal in deep shock, requires immediate splinting and van transport",
            address = "742 Evergreen Terrace, Sector 4",
            latitude = 37.7749,
            longitude = -122.4194
        )

        assertNotNull("Incident should have generated ID", incident.id)
        assertTrue("Incident ID should start with SAR-2026-", incident.id.startsWith("SAR-2026-"))
        assertEquals("Canine", incident.species)
        assertEquals(UrgencyLevel.CRITICAL, incident.urgency)
        assertEquals(RescueStage.SUBMITTED, incident.currentStage)

        // Verify notification delivered to citizen
        val notifs = IncidentRepository.notifications.value
        assertTrue("Notification should be created for report", notifs.isNotEmpty())
        assertEquals("Complaint Submitted", notifs.first().title)
        assertEquals(incident.id, notifs.first().incidentId)
    }

    @Test
    fun testOrganizationAdminAssignmentWorkflow() {
        // 1. Create incident
        val incident = IncidentRepository.addNewIntake(
            species = "Feline",
            situation = "Trapped in stormwater drain",
            urgency = UrgencyLevel.HIGH,
            count = 1,
            notes = "Kitten crying, water rising",
            address = "Pier 39, Dock B"
        )

        // 2. Admin verifies complaint
        IncidentRepository.adminVerifyComplaint(incident.id)
        val verified = IncidentRepository.getIncidentById(incident.id)
        assertEquals(RescueStage.DISPATCHER_APPROVED, verified?.currentStage)

        // 3. Admin assigns squad
        IncidentRepository.adminAssignTeam(
            id = incident.id,
            teamId = "TEAM-BETA-3",
            teamName = "Unit Beta-3 (Rapid Van)"
        )

        val assigned = IncidentRepository.getIncidentById(incident.id)
        assertNotNull(assigned)
        assertEquals("TEAM-BETA-3", assigned?.assignedTeamId)
        assertEquals("Unit Beta-3 (Rapid Van)", assigned?.assignedUnit)
        assertEquals(RescueStage.TEAM_ASSIGNED, assigned?.currentStage)
        assertEquals(MissionStatus.ASSIGNED, assigned?.missionStatus)
    }

    @Test
    fun testRescueTeamMissionLifecycle() {
        // 1. Create and assign incident
        val incident = IncidentRepository.addNewIntake(
            species = "Avian",
            situation = "Injured hawk with tangled fishing line",
            urgency = UrgencyLevel.MEDIUM,
            count = 1,
            notes = "Perched low on branch, unable to fly",
            address = "Golden Gate Park"
        )
        IncidentRepository.adminAssignTeam(incident.id, "TEAM-BETA-3", "Unit Beta-3")

        // 2. Rescue team updates status: En Route -> On Scene -> Animal Rescued
        IncidentRepository.updateMissionStatus(incident.id, MissionStatus.EN_ROUTE)
        assertEquals(RescueStage.EN_ROUTE, IncidentRepository.getIncidentById(incident.id)?.currentStage)

        IncidentRepository.updateMissionStatus(incident.id, MissionStatus.ARRIVED)
        assertEquals(RescueStage.ON_SCENE, IncidentRepository.getIncidentById(incident.id)?.currentStage)

        IncidentRepository.updateMissionStatus(incident.id, MissionStatus.ANIMAL_RESCUED)
        assertEquals(RescueStage.ANIMAL_SECURED, IncidentRepository.getIncidentById(incident.id)?.currentStage)

        // 3. Submit completion report
        val report = RescueReport(
            incidentId = incident.id,
            outcome = "Secured & Stabilized",
            animalCondition = "Stable, mild dehydration",
            actionTaken = "Removed monofilament line, cleaned wing laceration, administered fluids",
            rescueNotes = "Transferred to Metro Wildlife Care Sanctuary",
            rescuePhotos = emptyList(),
            treatmentShelter = "Metro Wildlife Care Sanctuary",
            submittedAt = "Today, 10:45 AM"
        )
        IncidentRepository.submitRescueReport(report)

        val completed = IncidentRepository.getIncidentById(incident.id)
        assertEquals(MissionStatus.COMPLETED, completed?.missionStatus)
        assertEquals(RescueStage.RESOLVED, completed?.currentStage)
        assertEquals("Metro Wildlife Care Sanctuary", completed?.attendingVetName)
    }

    @Test
    fun testRemoteDataMergingAcrossPortals() {
        val remoteIncident = com.example.model.Incident(
            id = "SAR-2026-9999",
            title = "Canine Highway Emergency",
            species = "Canine",
            situation = "Stray wandering median",
            urgency = UrgencyLevel.CRITICAL,
            notes = "Reported by patrol unit",
            address = "Interstate 280, Mile 14",
            currentStage = RescueStage.TEAM_ASSIGNED,
            missionStatus = MissionStatus.ASSIGNED,
            reporterId = "CIT-001",
            assignedTeamId = "TEAM-BETA-3",
            assignedUnit = "Unit Beta-3 (Rapid Van)"
        )

        // Merge remote list from Firestore
        IncidentRepository.mergeRemoteIncidents(listOf(remoteIncident))

        val retrieved = IncidentRepository.getIncidentById("SAR-2026-9999")
        assertNotNull("Remote incident should be merged into repository", retrieved)
        assertEquals("Canine Highway Emergency", retrieved?.title)
        assertEquals(UrgencyLevel.CRITICAL, retrieved?.urgency)
        assertEquals("TEAM-BETA-3", retrieved?.assignedTeamId)
    }

    @Test
    fun testCitizenPreferencesPersistence() {
        val prefs = CitizenPreferences(
            notifyStatusUpdates = true,
            notifyTeamAssigned = true,
            notifyRescueCompleted = true,
            notifyServiceAnnouncements = true,
            preferredLanguage = "Spanish"
        )

        IncidentRepository.updateCitizenPreferences(prefs)
        val current = IncidentRepository.citizenPreferences.value
        assertEquals("Spanish", current.preferredLanguage)
        assertTrue(current.notifyServiceAnnouncements)
    }
}
