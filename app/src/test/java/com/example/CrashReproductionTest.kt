package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.IncidentRepository
import com.example.model.ImageQualityRating
import com.example.model.ImageValidationResult
import com.example.model.IncidentLocationData
import com.example.model.UrgencyLevel
import com.example.service.DefaultAIAnalysisService
import com.example.service.DefaultGeocodingService
import com.example.service.DefaultLocationService
import com.example.service.EvidenceService
import com.example.service.LocationHelper
import com.example.service.MapConfiguration
import com.example.util.CrashLogger
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CrashReproductionTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun testMapConfigurationInitialization() {
        MapConfiguration.initialize(context)
        assertEquals("© OpenStreetMap contributors", MapConfiguration.OSM_ATTRIBUTION)
        assertTrue(MapConfiguration.USER_AGENT.contains("PawRescue"))
        assertNotNull(MapConfiguration.getDefaultTileSource())
    }

    @Test
    fun testLocationServiceSafety() {
        val locationService = DefaultLocationService()
        // Should not crash even without physical GPS hardware or permission
        val hasPerm = locationService.hasPermission(context)
        val isGps = locationService.isGpsEnabled(context)
        assertFalse(hasPerm)
    }

    @Test
    fun testGeocodingServiceSafety() = runBlocking {
        val geocodingService = DefaultGeocodingService()
        // Bounds checking and caching verification without network timeouts
        val invalidAddr = geocodingService.reverseGeocode(context, Double.NaN, 999.0)
        assertEquals("Invalid Coordinates", invalidAddr)

        val outOfBoundsAddr = geocodingService.reverseGeocode(context, 105.0, -200.0)
        assertEquals("Invalid Coordinates", outOfBoundsAddr)

        // Search location with empty or short query returns immediately
        val emptySearch = geocodingService.searchLocations(context, "a")
        assertTrue(emptySearch.isEmpty())
    }

    @Test
    fun testIncidentRepositoryAddNewIntakeWithLocationData() {
        val locationData = IncidentLocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            accuracyMeters = 5.2f,
            address = "Broadway & 5th Ave, NY",
            locationSource = "device_gps",
            locationConfirmed = true
        )

        val incident = IncidentRepository.addNewIntake(
            species = "Dog",
            situation = "Injured",
            urgency = UrgencyLevel.CRITICAL,
            count = 1,
            notes = "Test notes",
            address = locationData.address,
            locationData = locationData
        )
        assertNotNull(incident)
        assertTrue(incident.id.startsWith("SAR-2026-"))
        assertEquals("Submitted", incident.status)
        assertEquals(40.7128, incident.latitude!!, 0.001)
        assertEquals(-74.0060, incident.longitude!!, 0.001)
        assertEquals(5.2f, incident.accuracyMeters!!, 0.01f)
        assertEquals("Broadway & 5th Ave, NY", incident.address)
        assertNotNull(incident.locationData)
        assertTrue(incident.locationData!!.locationConfirmed)
    }

    @Test
    fun testEvidenceServiceWithEmptyOrInvalidFile() = runBlocking {
        val dummyFile = File(context.cacheDir, "test_dummy.txt").apply {
            writeText("not an image")
        }
        val uri = Uri.fromFile(dummyFile)

        val service = EvidenceService()
        val item = service.processEvidenceImage(
            context = context,
            sourceUri = uri,
            angleNumber = 1,
            selectedSpecies = "Dog",
            selectedSituation = "Injured"
        )
        assertNotNull(item)
        val validation = item.validationResult
        assertNotNull(validation)
        assertFalse(validation!!.qualityMessage.isBlank())
    }

    @Test
    fun testEvidenceServiceWithCorruptImage() = runBlocking {
        val corruptFile = File(context.cacheDir, "test_corrupt.jpg").apply {
            FileOutputStream(this).use {
                it.write(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0x00, 0x01, 0x02))
            }
        }
        val uri = Uri.fromFile(corruptFile)

        val service = EvidenceService()
        val item = service.processEvidenceImage(
            context = context,
            sourceUri = uri,
            angleNumber = 1,
            selectedSpecies = "Cat",
            selectedSituation = "Trapped"
        )
        assertNotNull(item)
        assertNotNull(item.validationResult)
    }

    @Test
    fun testAIAnalysisGracefulDegradation() = runBlocking {
        val aiService = DefaultAIAnalysisService()
        val invalidValidation = ImageValidationResult(
            isValid = false,
            isCorrupted = true,
            qualityRating = ImageQualityRating.CORRUPTED,
            qualityScorePercent = 0,
            qualityMessage = "Corrupted file"
        )

        val result = aiService.analyzeImage(
            context = context,
            uri = Uri.parse("file:///dummy.jpg"),
            validation = invalidValidation,
            existingIncidents = emptyList(),
            selectedSpecies = "Dog",
            selectedSituation = "Injured"
        )

        assertNotNull(result)
        assertEquals("Image too poor to verify", result.authenticityStatus)
        assertNull(result.animalDetected)
        assertNull(result.confidence)
    }

    @Test
    fun testCrashLoggerSafeLogging() {
        CrashLogger.logInfo("TestScreen", "Info log message")
        CrashLogger.logWarning("TestScreen", "TestWarning", "Warning message")
        CrashLogger.logError(
            screen = "TestScreen",
            action = "TestAction",
            throwable = RuntimeException("Sample safe exception"),
            contextInfo = mapOf("password" to "should_be_stripped", "safeKey" to "safeValue")
        )
    }

    @Test
    fun testRouteServiceCalculationAndFormatting() {
        val routeService = com.example.service.DefaultRouteService()
        // Distance calculation between known points (e.g. San Diego points ~ 1.2km apart)
        val distance = routeService.calculateDistanceKm(32.7157, -117.1611, 32.7257, -117.1611)
        assertTrue("Distance should be positive", distance > 0.5)

        val eta = routeService.calculateEtaMinutes(distance, averageSpeedKmh = 30.0)
        assertTrue("ETA should be at least 1 minute", eta >= 1)

        val formattedDist = routeService.formatDistance(distance)
        assertTrue("Formatted distance should contain units", formattedDist.contains("km") || formattedDist.contains("m"))

        val formattedEta = routeService.formatEta(eta)
        assertTrue("Formatted ETA should contain min", formattedEta.contains("min"))

        val routeResult = routeService.buildRoute(32.7157, -117.1611, 32.7257, -117.1611)
        assertNotNull(routeResult)
        assertTrue("Route should have at least 2 waypoints", routeResult.points.size >= 2)
    }

    @Test
    fun testMapFilterStateLogic() {
        val filter = com.example.model.MapFilterState(showCritical = true, showRoutine = false)
        assertTrue(filter.matchesUrgency(UrgencyLevel.CRITICAL))
        assertFalse(filter.matchesUrgency(UrgencyLevel.LOW))
        assertTrue(filter.matchesUrgency(null))
    }
}
