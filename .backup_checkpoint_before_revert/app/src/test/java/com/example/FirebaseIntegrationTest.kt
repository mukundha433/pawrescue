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

    @Test
    fun testRescueTeamCredentialsAndPortalRestriction() {
        // 1. Valid login through Rescue Team portal
        val authResult = IncidentRepository.authenticate(
            UserRole.RESCUE_TEAM,
            "jeswanth746@gmail.com",
            "testing"
        )
        assertTrue("Rescue team authentication must succeed", authResult is com.example.model.AuthResult.Success)
        val session = (authResult as com.example.model.AuthResult.Success).session
        assertEquals(UserRole.RESCUE_TEAM, session.role)
        assertEquals("jeswanth746@gmail.com", session.email)
        assertEquals("TEAM-BETA-3", session.teamId)

        // 2. Denied when attempting to access Citizen Portal
        val citizenPortalAttempt = IncidentRepository.authenticate(
            UserRole.CITIZEN,
            "jeswanth746@gmail.com",
            "testing"
        )
        assertTrue("Rescue team user cannot access Citizen portal", citizenPortalAttempt is com.example.model.AuthResult.WrongPortalError)

        // 3. Denied when attempting to access Organization Admin Portal
        val adminPortalAttempt = IncidentRepository.authenticate(
            UserRole.ORGANIZATION_ADMIN,
            "jeswanth746@gmail.com",
            "testing"
        )
        assertTrue("Rescue team user cannot gain Organization Admin privileges", adminPortalAttempt is com.example.model.AuthResult.WrongPortalError)
    }

    @Test
    fun testOrganizationAdminCredentialsAndPortalRestriction() {
        // 1. Valid login through Org Admin portal
        val authResult = IncidentRepository.authenticate(
            UserRole.ORGANIZATION_ADMIN,
            "jayasuryageetha@gmail.com",
            "testing"
        )
        assertTrue("Org Admin authentication must succeed", authResult is com.example.model.AuthResult.Success)
        val session = (authResult as com.example.model.AuthResult.Success).session
        assertEquals(UserRole.ORGANIZATION_ADMIN, session.role)
        assertEquals("jayasuryageetha@gmail.com", session.email)
        assertEquals("ORG-METRO-01", session.organizationId)

        // 2. Denied when attempting to access Rescue Team Portal
        val rescuePortalAttempt = IncidentRepository.authenticate(
            UserRole.RESCUE_TEAM,
            "jayasuryageetha@gmail.com",
            "testing"
        )
        assertTrue("Org Admin user cannot access Rescue Team portal", rescuePortalAttempt is com.example.model.AuthResult.WrongPortalError)

        // 3. Denied when attempting to access Citizen Portal
        val citizenPortalAttempt = IncidentRepository.authenticate(
            UserRole.CITIZEN,
            "jayasuryageetha@gmail.com",
            "testing"
        )
        assertTrue("Org Admin user cannot access Citizen portal", citizenPortalAttempt is com.example.model.AuthResult.WrongPortalError)
    }

    @Test
    fun testCitizenRegistrationLoginAndProfileUpdate() {
        // 1. Register new citizen
        val signupResult = IncidentRepository.signUpCitizen(
            name = "Ravi Kumar",
            emailOrPhone = "ravi.kumar@example.com",
            pass = "citizenStrongPass123"
        )
        assertTrue("Citizen signup must succeed", signupResult is com.example.model.AuthResult.Success)
        val session = (signupResult as com.example.model.AuthResult.Success).session
        assertEquals(UserRole.CITIZEN, session.role)
        assertEquals("Ravi Kumar", session.name)
        assertEquals("ravi.kumar@example.com", session.email)

        // 2. Update profile
        val updateSuccess = IncidentRepository.updateUserProfile(
            session.userId,
            "Ravi K. Sharma",
            "ravi.sharma@example.com",
            "+1 (555) 444-9988"
        )
        assertTrue("Profile update must succeed", updateSuccess)
        assertEquals("Ravi K. Sharma", IncidentRepository.currentSession.value?.name)
        assertEquals("ravi.sharma@example.com", IncidentRepository.currentSession.value?.email)

        // 3. Log in again with new email and registered password
        val loginResult = IncidentRepository.authenticate(
            UserRole.CITIZEN,
            "ravi.sharma@example.com",
            "citizenStrongPass123"
        )
        assertTrue("Login with updated profile must succeed", loginResult is com.example.model.AuthResult.Success)
    }

    @Test
    fun testCitizenRegisterLogoutAndReloginCycle() {
        // 1. Citizen registers
        val regResult = IncidentRepository.signUpCitizen(
            name = "Anita Roy",
            emailOrPhone = "anita.roy@example.com",
            pass = "secretPass123"
        )
        assertTrue("Registration must succeed", regResult is com.example.model.AuthResult.Success)
        assertNotNull("Session must be active after signup", IncidentRepository.currentSession.value)

        // 2. Citizen logs out
        IncidentRepository.logout()
        org.junit.Assert.assertNull("Session must be null after logout", IncidentRepository.currentSession.value)

        // 3. Citizen logs in again with identical credentials (verifying fix for 'no account found' bug)
        var reloginResult: com.example.model.AuthResult? = null
        IncidentRepository.authenticateAsync(
            targetPortal = UserRole.CITIZEN,
            identifier = "anita.roy@example.com",
            pass = "secretPass123"
        ) { result ->
            reloginResult = result
        }

        assertNotNull("Re-login result must not be null", reloginResult)
        assertTrue("Re-login after logout must succeed without 'No account found'", reloginResult is com.example.model.AuthResult.Success)
        assertEquals("Anita Roy", (reloginResult as com.example.model.AuthResult.Success).session.name)
        assertEquals(UserRole.CITIZEN, (reloginResult as com.example.model.AuthResult.Success).session.role)
    }

    @Test
    fun testInvalidPasswordResilience() {
        // 1. Citizen registers
        IncidentRepository.signUpCitizen(
            name = "Dev Patel",
            emailOrPhone = "dev.patel@example.com",
            pass = "devStrongPass1"
        )
        IncidentRepository.logout()

        // 2. Enter incorrect password
        var failedResult: com.example.model.AuthResult? = null
        IncidentRepository.authenticateAsync(
            targetPortal = UserRole.CITIZEN,
            identifier = "dev.patel@example.com",
            pass = "wrongPassword99"
        ) { result ->
            failedResult = result
        }

        assertTrue("Failed result must be InvalidCredentialsError", failedResult is com.example.model.AuthResult.InvalidCredentialsError)
        assertEquals("Incorrect password entered. Please try again.", (failedResult as com.example.model.AuthResult.InvalidCredentialsError).message)

        // 3. Subsequent attempt with correct password succeeds (account was not deleted or corrupted)
        var correctResult: com.example.model.AuthResult? = null
        IncidentRepository.authenticateAsync(
            targetPortal = UserRole.CITIZEN,
            identifier = "dev.patel@example.com",
            pass = "devStrongPass1"
        ) { result ->
            correctResult = result
        }

        assertTrue("Correct password must succeed", correctResult is com.example.model.AuthResult.Success)
    }

    @Test
    fun testCitizenRestrictedFromPrivilegedPortals() {
        IncidentRepository.signUpCitizen(
            name = "Maya Lin",
            emailOrPhone = "maya.lin@example.com",
            pass = "citizenMayaPass"
        )

        // Attempt login through Rescue Team portal
        val rescueAttempt = IncidentRepository.authenticate(
            UserRole.RESCUE_TEAM,
            "maya.lin@example.com",
            "citizenMayaPass"
        )
        assertTrue("Citizen cannot access Rescue Team portal", rescueAttempt is com.example.model.AuthResult.WrongPortalError)

        // Attempt login through Organization Admin portal
        val adminAttempt = IncidentRepository.authenticate(
            UserRole.ORGANIZATION_ADMIN,
            "maya.lin@example.com",
            "citizenMayaPass"
        )
        assertTrue("Citizen cannot access Organization Admin portal", adminAttempt is com.example.model.AuthResult.WrongPortalError)
    }

    @Test
    fun testCitizenSessionRestoredOnAppReopen() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        IncidentRepository.initialize(context)

        // Citizen signs in
        val authResult = IncidentRepository.authenticate(UserRole.CITIZEN, "elena@pawrescue.org", "citizen123")
        assertTrue(authResult is com.example.model.AuthResult.Success)

        // Simulate complete app process termination (wiping RAM)
        IncidentRepository.resetAllDataForTesting()
        org.junit.Assert.assertNull("RAM session is wiped on app close", IncidentRepository.currentSession.value)

        // Reopen app
        IncidentRepository.initialize(context)
        val restored = IncidentRepository.currentSession.value
        assertNotNull("Session must be restored on app reopen", restored)
        assertEquals(UserRole.CITIZEN, restored?.role)
        assertEquals("elena@pawrescue.org", restored?.email)
    }

    @Test
    fun testRescueTeamSessionRestoredOnAppReopen() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        IncidentRepository.initialize(context)

        // Rescue Team signs in
        val authResult = IncidentRepository.authenticate(UserRole.RESCUE_TEAM, "jeswanth746@gmail.com", "testing")
        assertTrue(authResult is com.example.model.AuthResult.Success)

        // Simulate complete app process termination
        IncidentRepository.resetAllDataForTesting()
        org.junit.Assert.assertNull("RAM session is wiped on app close", IncidentRepository.currentSession.value)

        // Reopen app
        IncidentRepository.initialize(context)
        val restored = IncidentRepository.currentSession.value
        assertNotNull("Rescue Team session must be restored on app reopen", restored)
        assertEquals(UserRole.RESCUE_TEAM, restored?.role)
        assertEquals("jeswanth746@gmail.com", restored?.email)
        assertEquals("TEAM-BETA-3", restored?.teamId)
    }

    @Test
    fun testOrganizationAdminSessionRestoredOnAppReopen() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        IncidentRepository.initialize(context)

        // Organization Admin signs in
        val authResult = IncidentRepository.authenticate(UserRole.ORGANIZATION_ADMIN, "jayasuryageetha@gmail.com", "testing")
        assertTrue(authResult is com.example.model.AuthResult.Success)

        // Simulate complete app process termination
        IncidentRepository.resetAllDataForTesting()
        org.junit.Assert.assertNull("RAM session is wiped on app close", IncidentRepository.currentSession.value)

        // Reopen app
        IncidentRepository.initialize(context)
        val restored = IncidentRepository.currentSession.value
        assertNotNull("Org Admin session must be restored on app reopen", restored)
        assertEquals(UserRole.ORGANIZATION_ADMIN, restored?.role)
        assertEquals("jayasuryageetha@gmail.com", restored?.email)
        assertEquals("ORG-METRO-01", restored?.organizationId)
    }

    @Test
    fun testExplicitLogoutRequiresLoginOnReopen() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        IncidentRepository.initialize(context)

        // Sign in then explicitly logout
        IncidentRepository.authenticate(UserRole.CITIZEN, "elena@pawrescue.org", "citizen123")
        IncidentRepository.logout()

        // Simulate app reopen
        IncidentRepository.initialize(context)
        org.junit.Assert.assertNull("Session must remain null after explicit logout upon app reopen", IncidentRepository.currentSession.value)
    }

    @Test
    fun testEmergencySosPhoneNumberAndIntentFormat() {
        val sosNumber = "9524318963"
        val dialUri = android.net.Uri.parse("tel:$sosNumber")
        assertEquals("tel", dialUri.scheme)
        assertEquals("9524318963", dialUri.schemeSpecificPart)
    }

    // Scenario A: New Rescue Team applicant registers -> status is pending -> login denied
    @Test
    fun testScenarioA_RescueTeamRegistrationPendingLoginDenied() {
        val email = "applicant.a@pawrescue.org"
        val req = IncidentRepository.submitRescueTeamRegistration(
            fullName = "Marcus Vance",
            email = email,
            phone = "+1 (555) 912-3456",
            passwordPlain = "rescuePass123"
        )

        assertNotNull("Request should be generated", req)
        assertEquals(com.example.model.RegistrationApprovalStatus.PENDING, req.status)
        assertEquals("Marcus Vance", req.fullName)
        assertEquals(email, req.email)

        // Attempting to log in as Rescue Team before approval MUST fail with exact prompt text
        val loginResult = IncidentRepository.authenticate(UserRole.RESCUE_TEAM, email, "rescuePass123")
        assertTrue("Pending applicant cannot log in to Rescue Team portal", loginResult is com.example.model.AuthResult.AccountInactiveError)
        val errorMsg = (loginResult as com.example.model.AuthResult.AccountInactiveError).message
        assertEquals(
            "Your account is awaiting Organization Admin approval. You cannot log in until your registration is approved.",
            errorMsg
        )
    }

    // Scenario B: Pending applicant closes and reopens app -> session restoration yields null
    @Test
    fun testScenarioB_PendingApplicantColdStartRestorationDenied() {
        val email = "applicant.b@pawrescue.org"
        IncidentRepository.submitRescueTeamRegistration(
            fullName = "Elena Rostova",
            email = email,
            phone = "+1 (555) 444-2222",
            passwordPlain = "passElena123"
        )

        // Simulate creating a fake pending session in prefs
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val pendingSession = com.example.model.UserSession(
            userId = "PENDING-UID-99",
            name = "Elena Rostova",
            email = email,
            phone = "+1 (555) 444-2222",
            role = UserRole.RESCUE_TEAM,
            status = AccountStatus.PENDING_APPROVAL,
            authToken = "FAKE-TOKEN"
        )
        com.example.data.SessionManager.saveActiveSession(context, pendingSession)

        // Cold start active session restoration MUST return null for non-ACTIVE status
        val restoredSession = com.example.data.SessionManager.getActiveSession(context)
        org.junit.Assert.assertNull("Pending account must never be restored to active session", restoredSession)
    }

    // Scenario C: Organization Admin approves the request -> applicant can now log in successfully
    @Test
    fun testScenarioC_AdminApprovalEnablesLoginAndSquadAssignment() {
        val email = "squad.candidate@pawrescue.org"
        val req = IncidentRepository.submitRescueTeamRegistration(
            fullName = "Tanya Ramirez",
            email = email,
            phone = "+1 (555) 789-0123",
            passwordPlain = "squadPass456"
        )

        // Admin approves application
        IncidentRepository.approveRescueRequest(
            requestId = req.requestId,
            adminName = "Jayasurya (Org Administrator)",
            adminUid = "ORG-ADMIN-JAYASURYA"
        )

        // Verify request updated
        val updatedReq = IncidentRepository.rescueRequests.value.find { it.requestId == req.requestId }
        assertNotNull(updatedReq)
        assertEquals(com.example.model.RegistrationApprovalStatus.APPROVED, updatedReq?.status)
        assertEquals("Jayasurya (Org Administrator)", updatedReq?.reviewedBy)

        // Candidate can now log in successfully to Rescue Team portal
        val loginResult = IncidentRepository.authenticate(UserRole.RESCUE_TEAM, email, "squadPass456")
        assertTrue("Approved member can log into Rescue Team portal", loginResult is com.example.model.AuthResult.Success)
        val session = (loginResult as com.example.model.AuthResult.Success).session
        assertEquals(UserRole.RESCUE_TEAM, session.role)
        assertEquals(AccountStatus.ACTIVE, session.status)
        assertEquals("TEAM-BETA-3", session.teamId)
        assertEquals("Unit Beta-3 (Rapid Van)", session.teamName)
    }

    // Scenario D: Organization Admin rejects the request -> applicant cannot access dashboard
    @Test
    fun testScenarioD_AdminRejectionBlocksDashboardAccess() {
        val email = "rejected.candidate@pawrescue.org"
        val req = IncidentRepository.submitRescueTeamRegistration(
            fullName = "Alex Mercer",
            email = email,
            phone = "+1 (555) 345-6789",
            passwordPlain = "alexPass789"
        )

        val reason = "Applicant lacks required state veterinary first-aid certification."
        IncidentRepository.rejectRescueRequest(
            requestId = req.requestId,
            reason = reason,
            adminName = "Jayasurya (Org Administrator)",
            adminUid = "ORG-ADMIN-JAYASURYA"
        )

        val updatedReq = IncidentRepository.rescueRequests.value.find { it.requestId == req.requestId }
        assertNotNull(updatedReq)
        assertEquals(com.example.model.RegistrationApprovalStatus.REJECTED, updatedReq?.status)
        assertEquals(reason, updatedReq?.rejectionReason)

        // Candidate login MUST be denied with exact prompt message
        val loginResult = IncidentRepository.authenticate(UserRole.RESCUE_TEAM, email, "alexPass789")
        assertTrue("Rejected member cannot log into Rescue Team portal", loginResult is com.example.model.AuthResult.AccountInactiveError)
        val errorMsg = (loginResult as com.example.model.AuthResult.AccountInactiveError).message
        assertEquals(
            "Your Rescue Team registration request has been rejected. Please contact the organization for further information.",
            errorMsg
        )
    }

    // Scenario E: Frontend role tampering or manipulated local storage
    @Test
    fun testScenarioE_RoleTamperingDefense() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val tamperedSession = com.example.model.UserSession(
            userId = "USER-HACK",
            name = "Malicious Actor",
            email = "attacker@pawrescue.org",
            role = UserRole.RESCUE_TEAM,
            status = AccountStatus.REJECTED,
            authToken = "HACKED-TOKEN"
        )
        com.example.data.SessionManager.saveActiveSession(context, tamperedSession)

        // Session manager must reject non-ACTIVE status
        val restored = com.example.data.SessionManager.getActiveSession(context)
        org.junit.Assert.assertNull("Tampered or non-active session must be blocked", restored)
    }

    // Scenario F: Ordinary Citizen cannot log in through Rescue Team portal
    @Test
    fun testScenarioF_CitizenBlockedFromRescueTeamPortal() {
        val loginResult = IncidentRepository.authenticate(UserRole.RESCUE_TEAM, "elena@pawrescue.org", "citizen123")
        assertTrue("Citizen cannot enter Rescue Team portal", loginResult is com.example.model.AuthResult.WrongPortalError)
    }

    // Scenario G: Existing approved Rescue Team account continues working
    @Test
    fun testScenarioG_ExistingApprovedRescueTeamAccountLogsInNormally() {
        val loginResult = IncidentRepository.authenticate(UserRole.RESCUE_TEAM, "jeswanth746@gmail.com", "testing")
        assertTrue("Existing approved Rescue Team can log in", loginResult is com.example.model.AuthResult.Success)
        val session = (loginResult as com.example.model.AuthResult.Success).session
        assertEquals(UserRole.RESCUE_TEAM, session.role)
        assertEquals(AccountStatus.ACTIVE, session.status)
        assertEquals("TEAM-BETA-3", session.teamId)
    }

    // Scenario H: AccountStatus normalization handles uppercase, lowercase, and APPROVED
    @Test
    fun testScenarioH_AccountStatusNormalization() {
        assertEquals(AccountStatus.APPROVED, AccountStatus.fromString("approved"))
        assertEquals(AccountStatus.APPROVED, AccountStatus.fromString("APPROVED"))
        assertEquals(AccountStatus.ACTIVE, AccountStatus.fromString("active"))
        assertEquals(AccountStatus.ACTIVE, AccountStatus.fromString("ACTIVE"))
        assertEquals(AccountStatus.PENDING_APPROVAL, AccountStatus.fromString("pending"))
        assertEquals(AccountStatus.PENDING_APPROVAL, AccountStatus.fromString("PENDING_APPROVAL"))
        assertEquals(AccountStatus.REJECTED, AccountStatus.fromString("rejected"))
        assertEquals(AccountStatus.REJECTED, AccountStatus.fromString("REJECTED"))

        assertTrue("APPROVED is considered approved", AccountStatus.APPROVED.isApproved)
        assertTrue("ACTIVE is considered approved", AccountStatus.ACTIVE.isApproved)
        org.junit.Assert.assertFalse("PENDING_APPROVAL is not approved", AccountStatus.PENDING_APPROVAL.isApproved)
        org.junit.Assert.assertFalse("REJECTED is not approved", AccountStatus.REJECTED.isApproved)
    }

    // Scenario I: SessionManager restores APPROVED sessions
    @Test
    fun testScenarioI_SessionManagerRestoresApprovedSession() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val approvedSession = com.example.model.UserSession(
            userId = "USER-APPROVED-1",
            name = "Sarah Connor",
            email = "sarah.connor@pawrescue.org",
            role = UserRole.RESCUE_TEAM,
            status = AccountStatus.APPROVED,
            teamId = "TEAM-BETA-3",
            teamName = "Unit Beta-3 (Rapid Van)",
            authToken = "AUTH-TOKEN-1"
        )
        com.example.data.SessionManager.saveActiveSession(context, approvedSession)

        val restored = com.example.data.SessionManager.getActiveSession(context)
        assertNotNull("Approved session must be restored", restored)
        assertEquals(UserRole.RESCUE_TEAM, restored?.role)
        assertEquals(AccountStatus.APPROVED, restored?.status)
    }
}
