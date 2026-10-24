package com.example

import com.example.data.IncidentRepository
import com.example.model.AuthResult
import com.example.model.UrgencyLevel
import com.example.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testInitialSessionIsNull() {
    IncidentRepository.logout()
    assertNull(IncidentRepository.currentSession.value)
  }

  @Test
  fun testCitizenAuthenticationSuccess() {
    val result = IncidentRepository.authenticate(
      targetPortal = UserRole.CITIZEN,
      identifier = "elena@pawrescue.org",
      pass = "citizen123"
    )
    assertTrue("Should authenticate successfully", result is AuthResult.Success)
    val session = (result as AuthResult.Success).session
    assertEquals(UserRole.CITIZEN, session.role)
    assertEquals("CIT-001", session.userId)
  }

  @Test
  fun testStrictRoleGuardDeniesWrongPortal() {
    // Attempt to log into Rescue Team portal using Citizen credentials
    val wrongPortalResult = IncidentRepository.authenticate(
      targetPortal = UserRole.RESCUE_TEAM,
      identifier = "elena@pawrescue.org",
      pass = "citizen123"
    )
    assertTrue("Should reject citizen accessing rescue portal", wrongPortalResult is AuthResult.WrongPortalError)
    val err = wrongPortalResult as AuthResult.WrongPortalError
    assertEquals(UserRole.RESCUE_TEAM, err.expectedRole)
    assertEquals(UserRole.CITIZEN, err.actualRole)

    // Attempt to log into Org Admin portal using Rescue Team credentials
    val wrongAdminResult = IncidentRepository.authenticate(
      targetPortal = UserRole.ORGANIZATION_ADMIN,
      identifier = "team.beta3@pawrescue.org",
      pass = "rescue123"
    )
    assertTrue("Should reject rescue squad accessing admin portal", wrongAdminResult is AuthResult.WrongPortalError)
  }

  @Test
  fun testInactiveAccountRejected() {
    val result = IncidentRepository.authenticate(
      targetPortal = UserRole.RESCUE_TEAM,
      identifier = "pending@pawrescue.org",
      pass = "pending123"
    )
    assertTrue("Pending volunteer should be rejected", result is AuthResult.AccountInactiveError)
  }

  @Test
  fun testCitizenSignUpFlow() {
    val name = "Test Citizen"
    val contact = "newbie@pawrescue.org"
    val pass = "pass1234"

    val result = IncidentRepository.signUpCitizen(name, contact, pass)
    assertTrue(result is AuthResult.Success)
    val session = (result as AuthResult.Success).session
    assertEquals(UserRole.CITIZEN, session.role)
    assertEquals(name, session.name)
  }

  @Test
  fun testRescueTeamAuthentication() {
    val result = IncidentRepository.authenticate(
      targetPortal = UserRole.RESCUE_TEAM,
      identifier = "team.beta3@pawrescue.org",
      pass = "rescue123"
    )
    assertTrue(result is AuthResult.Success)
    val session = (result as AuthResult.Success).session
    assertEquals(UserRole.RESCUE_TEAM, session.role)
    assertEquals("TEAM-BETA-3", session.teamId)
  }

  @Test
  fun testOrgAdminAuthentication() {
    val result = IncidentRepository.authenticate(
      targetPortal = UserRole.ORGANIZATION_ADMIN,
      identifier = "admin@pawrescue.org",
      pass = "admin123"
    )
    assertTrue(result is AuthResult.Success)
    val session = (result as AuthResult.Success).session
    assertEquals(UserRole.ORGANIZATION_ADMIN, session.role)
  }

  @Test
  fun testLogoutCompletelyClearsSession() {
    IncidentRepository.authenticate(
      targetPortal = UserRole.CITIZEN,
      identifier = "elena@pawrescue.org",
      pass = "citizen123"
    )
    assertNotNull(IncidentRepository.currentSession.value)

    IncidentRepository.logout()
    assertNull(IncidentRepository.currentSession.value)
  }

  @Test
  fun testInitialActiveIncidentIsNullAndPopulatesOnCreation() {
    IncidentRepository.resetAllDataForTesting()
    assertNull("active incident must be null initially with zero database records", IncidentRepository.activeIncident.value)

    val created = IncidentRepository.addNewIntake(
      species = "Dog",
      situation = "Injured",
      urgency = UrgencyLevel.CRITICAL,
      count = 1,
      notes = "Field trauma intake",
      address = "456 Market St"
    )
    assertNotNull(created)
    assertEquals(created.id, IncidentRepository.activeIncident.value?.id)
    assertEquals("Dog", IncidentRepository.activeIncident.value?.species)
    assertEquals(UrgencyLevel.CRITICAL, IncidentRepository.activeIncident.value?.urgency)
  }

  @Test
  fun testAddNewIntakeWithEvidenceList() {
    val mockEvidence = listOf(
      com.example.model.EvidenceItem(
        id = "EV-001",
        uriString = "file:///data/user/0/com.example/files/evidence/angle_1.jpg",
        angleNumber = 1,
        imageHash = "abc123hash",
        dimensions = "1920x1080"
      ),
      com.example.model.EvidenceItem(
        id = "EV-002",
        uriString = "file:///data/user/0/com.example/files/evidence/angle_2.jpg",
        angleNumber = 2,
        imageHash = "def456hash",
        dimensions = "1920x1080"
      )
    )

    val newIncident = IncidentRepository.addNewIntake(
      species = "Cat",
      situation = "Trapped",
      urgency = UrgencyLevel.HIGH,
      count = 1,
      notes = "Kitten trapped behind drainage pipe",
      address = "123 Main St",
      evidenceList = mockEvidence
    )

    assertNotNull(newIncident.id)
    assertEquals("Cat", newIncident.species)
    assertEquals(2, newIncident.evidenceList.size)
    assertEquals("file:///data/user/0/com.example/files/evidence/angle_1.jpg", newIncident.imageUrl)
    assertEquals("abc123hash", newIncident.evidenceList[0].imageHash)
  }

  @Test
  fun testNewUserZeroRecordsScenario() {
    IncidentRepository.resetAllDataForTesting()
    IncidentRepository.logout()

    // 1. Sign up brand new citizen
    val signupResult = IncidentRepository.signUpCitizen("Alice Newcomer", "alice@example.com", "secure123")
    assertTrue(signupResult is AuthResult.Success)
    val session = (signupResult as AuthResult.Success).session

    // Check my reports for new citizen: must be empty
    val userReports = IncidentRepository.incidents.value.filter {
      it.reporterId == session.userId || (it.reporterName.isNotBlank() && it.reporterName.equals(session.name, ignoreCase = true))
    }
    assertEquals("New user with zero reports must have empty report list", 0, userReports.size)
    assertEquals("Total system reports must be 0", 0, IncidentRepository.incidents.value.size)
    assertNull("Active incident must be null", IncidentRepository.activeIncident.value)

    // Check notifications for new citizen: must be empty
    val userNotifs = IncidentRepository.notifications.value.filter {
      it.userId == null || it.userId == session.userId
    }
    assertEquals("New user must have 0 notifications", 0, userNotifs.size)

    // 2. User submits exactly 1 report
    val submitted = IncidentRepository.addNewIntake(
      species = "Dog",
      situation = "Stray",
      urgency = UrgencyLevel.MEDIUM,
      count = 1,
      notes = "Found near park",
      address = "789 Pine St"
    )
    assertNotNull(submitted)
    assertEquals(session.userId, submitted.reporterId)

    // Now my reports must have exactly 1 report
    val updatedUserReports = IncidentRepository.incidents.value.filter {
      it.reporterId == session.userId || (it.reporterName.isNotBlank() && it.reporterName.equals(session.name, ignoreCase = true))
    }
    assertEquals("User must see exactly 1 report after submitting 1 report", 1, updatedUserReports.size)
    assertEquals(submitted.id, updatedUserReports[0].id)
  }

  @Test
  fun testRescueTeamZeroAssignedMissionsScenario() {
    IncidentRepository.resetAllDataForTesting()
    IncidentRepository.logout()

    // Sign up / log in rescue team
    val auth = IncidentRepository.authenticate(
      targetPortal = UserRole.RESCUE_TEAM,
      identifier = "team.beta3@pawrescue.org",
      pass = "rescue123"
    )
    assertTrue(auth is AuthResult.Success)
    val session = (auth as AuthResult.Success).session

    // Assigned missions for this team must be 0
    val teamMissions = IncidentRepository.incidents.value.filter {
      it.assignedTeamId == session.teamId
    }
    assertEquals("Rescue team with no assigned missions must see 0 missions", 0, teamMissions.size)
  }
}
