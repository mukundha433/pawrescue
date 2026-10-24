package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.model.EvidenceItem
import com.example.model.ImageQualityRating
import com.example.model.ImageValidationResult
import com.example.model.Incident
import com.example.model.VerificationStatus
import com.example.util.CrashLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EvidenceService(
    private val validationService: ImageValidationService = ImageValidationService(),
    private val aiAnalysisService: AIAnalysisService = DefaultAIAnalysisService()
) {

    suspend fun processEvidenceImage(
        context: Context,
        sourceUri: Uri,
        angleNumber: Int,
        selectedSpecies: String,
        selectedSituation: String,
        existingIncidents: List<Incident> = emptyList()
    ): EvidenceItem = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val capturedAt = SimpleDateFormat("hh:mm a", Locale.US).format(Date())

        try {
            val evidenceDir = File(context.filesDir, "evidence").apply {
                if (!exists()) mkdirs()
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val localFile = File(evidenceDir, "angle_${angleNumber}_${timeStamp}_${id.take(6)}.jpg")

            // Copy and downsample/compress if necessary
            var copiedSuccessfully = false
            try {
                val inputStream: InputStream? = if (sourceUri.scheme == "file") {
                    val p = sourceUri.path
                    if (!p.isNullOrBlank()) File(p).takeIf { it.exists() }?.inputStream() else null
                } else {
                    context.contentResolver.openInputStream(sourceUri)
                }

                inputStream?.use { input ->
                    // First decode bounds
                    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    // If stream cannot be reset, copy to file first
                    FileOutputStream(localFile).use { out ->
                        input.copyTo(out)
                    }
                    copiedSuccessfully = localFile.exists() && localFile.length() > 0L
                }
            } catch (t: Throwable) {
                CrashLogger.logError("EvidenceService", "copySourceStream", t)
            }

            if (!copiedSuccessfully) {
                val failedValidation = ImageValidationResult(
                    isValid = false,
                    isCorrupted = true,
                    qualityRating = ImageQualityRating.CORRUPTED,
                    qualityScorePercent = 0,
                    qualityMessage = "Unable to process this image. Please select another image."
                )
                return@withContext EvidenceItem(
                    id = id,
                    uriString = sourceUri.toString(),
                    localFilePath = null,
                    angleNumber = angleNumber,
                    capturedAt = capturedAt,
                    timestampMillis = System.currentTimeMillis(),
                    validationResult = failedValidation,
                    verificationStatus = VerificationStatus.FAILED
                )
            }

            val savedUri = Uri.fromFile(localFile)

            // 1. Run Image Validation Pipeline
            val validation: ImageValidationResult = validationService.validateImage(context, savedUri)

            // 2. Location retrieval
            val deviceLocation = LocationHelper.getCurrentLocation(context)
            val latitude = validation.exifGpsLatitude ?: deviceLocation?.latitude
            val longitude = validation.exifGpsLongitude ?: deviceLocation?.longitude
            val accuracy = if (validation.hasExifGps) 4f else (deviceLocation?.accuracy ?: 12f)
            val locationDesc = if (latitude != null && longitude != null && latitude.isFinite() && longitude.isFinite()) {
                String.format(Locale.US, "%.4f° N, %.4f° W", Math.abs(latitude), Math.abs(longitude))
            } else null

            // 3. Run AI Analysis Pipeline
            val aiResult = try {
                aiAnalysisService.analyzeImage(
                    context = context,
                    uri = savedUri,
                    validation = validation,
                    existingIncidents = existingIncidents,
                    selectedSpecies = selectedSpecies,
                    selectedSituation = selectedSituation
                )
            } catch (t: Throwable) {
                CrashLogger.logError("EvidenceService", "aiAnalysis", t)
                com.example.model.AIAnalysisResult(
                    animalDetected = null,
                    animalType = selectedSpecies,
                    authenticityStatus = "AI verification unavailable",
                    isAiModelConfigured = false,
                    isDevelopmentFallback = true
                )
            }

            EvidenceItem(
                id = id,
                uriString = savedUri.toString(),
                localFilePath = localFile.absolutePath,
                angleNumber = angleNumber,
                capturedAt = capturedAt,
                timestampMillis = System.currentTimeMillis(),
                latitude = latitude,
                longitude = longitude,
                locationAccuracyMeters = accuracy,
                locationDescription = locationDesc,
                imageHash = validation.imageHash,
                dimensions = validation.dimensions,
                validationResult = validation,
                aiAnalysis = aiResult,
                verificationStatus = if (validation.isValid) VerificationStatus.PENDING_REVIEW else VerificationStatus.FAILED
            )
        } catch (t: Throwable) {
            CrashLogger.logError("EvidenceService", "processEvidenceImage", t)
            EvidenceItem(
                id = id,
                uriString = sourceUri.toString(),
                angleNumber = angleNumber,
                capturedAt = capturedAt,
                timestampMillis = System.currentTimeMillis(),
                validationResult = ImageValidationResult(
                    isValid = false,
                    isCorrupted = true,
                    qualityRating = ImageQualityRating.CORRUPTED,
                    qualityScorePercent = 0,
                    qualityMessage = "Error processing image: ${t.localizedMessage ?: "Unknown error"}"
                ),
                verificationStatus = VerificationStatus.FAILED
            )
        }
    }
}
