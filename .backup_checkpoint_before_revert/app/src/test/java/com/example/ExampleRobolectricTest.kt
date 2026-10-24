package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PawRescue", appName)
  }

  @Test
  fun testAIAnalysisFallbackStatusIsPending() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val fallbackService = com.example.service.DefaultAIAnalysisService()
    val validation = com.example.model.ImageValidationResult(
      isValid = true,
      imageHash = "unique_sample_hash",
      qualityRating = com.example.model.ImageQualityRating.GOOD,
      dimensions = "1200x800"
    )

    val result = fallbackService.analyzeImage(
      context = context,
      uri = android.net.Uri.parse("file:///dummy.jpg"),
      validation = validation,
      existingIncidents = emptyList(),
      selectedSpecies = "Dog",
      selectedSituation = "Injured"
    )

    assertEquals("AI Verification Pending", result.authenticityStatus)
    org.junit.Assert.assertNull("Confidence should not be invented when model is not configured", result.confidence)
    org.junit.Assert.assertNull("Animal detection should not be falsely confirmed without model", result.animalDetected)
    org.junit.Assert.assertTrue("Should be marked as development fallback", result.isDevelopmentFallback)
  }

  @Test
  fun testCitizenProfileUpdateAndPreferences() {
    val repo = com.example.data.IncidentRepository
    val authResult = repo.authenticate(com.example.model.UserRole.CITIZEN, "elena@pawrescue.org", "citizen123")
    org.junit.Assert.assertTrue("Auth should succeed", authResult is com.example.model.AuthResult.Success)
    val session = (authResult as com.example.model.AuthResult.Success).session
    val testUserId = session.userId

    // 1. Update Profile
    val updated = repo.updateUserProfile(
      userId = testUserId,
      newName = "Jane Responder Doe",
      newEmail = "jane.updated@example.com",
      newPhone = "+1 (555) 999-8888"
    )
    org.junit.Assert.assertTrue("Profile update should succeed", updated)
    assertEquals("Jane Responder Doe", repo.currentSession.value?.name)
    assertEquals("jane.updated@example.com", repo.currentSession.value?.email)
    assertEquals("+1 (555) 999-8888", repo.currentSession.value?.phone)

    // 2. Update Preferences
    val newPrefs = com.example.model.CitizenPreferences(
      notifyStatusUpdates = false,
      notifyTeamAssigned = true,
      notifyRescueCompleted = true,
      notifyServiceAnnouncements = false,
      preferredLanguage = "Spanish"
    )
    repo.updateCitizenPreferences(newPrefs)
    val currentPrefs = repo.citizenPreferences.value
    assertEquals(false, currentPrefs.notifyStatusUpdates)
    assertEquals(true, currentPrefs.notifyTeamAssigned)
    assertEquals(true, currentPrefs.notifyRescueCompleted)
    assertEquals(false, currentPrefs.notifyServiceAnnouncements)
    assertEquals("Spanish", currentPrefs.preferredLanguage)

    // 3. Password change validation
    val badPassResult = repo.changePassword(testUserId, "wrong-password", "newpassword123")
    org.junit.Assert.assertTrue("Wrong current password should fail", badPassResult.isFailure)

    val shortPassResult = repo.changePassword(testUserId, "citizen123", "123")
    org.junit.Assert.assertTrue("Short password should fail", shortPassResult.isFailure)

    val validPassResult = repo.changePassword(testUserId, "citizen123", "validNewPass456")
    org.junit.Assert.assertTrue("Valid password change should succeed", validPassResult.isSuccess)
  }
}
