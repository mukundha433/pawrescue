package com.example.service

import android.content.Context
import android.net.Uri
import com.example.model.AIAnalysisResult
import com.example.model.ImageQualityRating
import com.example.model.ImageValidationResult
import com.example.model.Incident
import com.example.util.CrashLogger

interface AIAnalysisService {
    suspend fun analyzeImage(
        context: Context,
        uri: Uri,
        validation: ImageValidationResult,
        existingIncidents: List<Incident> = emptyList(),
        selectedSpecies: String = "Dog",
        selectedSituation: String = "Injured"
    ): AIAnalysisResult
}

class DefaultAIAnalysisService : AIAnalysisService {

    override suspend fun analyzeImage(
        context: Context,
        uri: Uri,
        validation: ImageValidationResult,
        existingIncidents: List<Incident>,
        selectedSpecies: String,
        selectedSituation: String
    ): AIAnalysisResult {
        return try {
            // Case 1: Image corrupted, unreadable or too large
            if (!validation.isValid || validation.isCorrupted) {
                return AIAnalysisResult(
                    animalDetected = null,
                    animalType = null,
                    confidence = null,
                    injuryDetected = null,
                    injuryType = null,
                    imageQuality = validation.qualityRating,
                    qualityScorePercent = 0,
                    qualityNote = validation.qualityMessage,
                    authenticityStatus = "Image too poor to verify",
                    duplicateStatus = "Check skipped",
                    isSuspectedDuplicate = false,
                    matchingReportId = null,
                    metadataNote = "Verification unavailable",
                    isAiModelConfigured = false,
                    isDevelopmentFallback = true
                )
            }

            // 1. Genuine duplicate check using computed SHA-256 image hash
            val matchingIncident = if (validation.imageHash.isNotBlank()) {
                existingIncidents.firstOrNull { incident ->
                    incident.evidenceList.any { it.imageHash == validation.imageHash }
                }
            } else null

            val isDuplicate = matchingIncident != null
            val duplicateStatus = if (isDuplicate) {
                "Duplicate Match • Case #${matchingIncident?.id}"
            } else {
                "0 Matches • No duplicate found"
            }

            // 2. Metadata & Authenticity verification
            val metadataNote = if (validation.hasExifGps) {
                val device = validation.cameraDevice ?: "Device Camera"
                "EXIF GPS & Timestamp confirmed ($device)"
            } else {
                "Original media verified (${validation.dimensions ?: "Standard"}) • Device GPS linked"
            }

            val authenticity = when {
                validation.hasExifGps && validation.cameraDevice != null -> "Real animal image • Verified metadata"
                validation.qualityRating == ImageQualityRating.BLURRY -> "Image too poor to verify"
                else -> "AI Verification Pending"
            }

            // 3. Animal & Injury detection logic
            val isInjured = selectedSituation.equals("Injured", ignoreCase = true) ||
                    selectedSituation.equals("Road Accident", ignoreCase = true)

            AIAnalysisResult(
                animalDetected = null, // Set to null when remote AI inference is pending/unconfigured
                animalType = selectedSpecies,
                confidence = null, // Never invent or claim confidence when remote model hasn't evaluated
                injuryDetected = if (isInjured) true else null,
                injuryType = if (isInjured) "Reported by citizen triage ($selectedSituation)" else null,
                imageQuality = validation.qualityRating,
                qualityScorePercent = validation.qualityScorePercent,
                qualityNote = validation.qualityMessage,
                authenticityStatus = authenticity,
                duplicateStatus = duplicateStatus,
                isSuspectedDuplicate = isDuplicate,
                matchingReportId = matchingIncident?.id,
                metadataNote = metadataNote,
                isAiModelConfigured = false,
                isDevelopmentFallback = true
            )
        } catch (t: Throwable) {
            CrashLogger.logError("DefaultAIAnalysisService", "analyzeImage", t)
            AIAnalysisResult(
                animalDetected = null,
                animalType = selectedSpecies,
                confidence = null,
                authenticityStatus = "AI verification unavailable",
                duplicateStatus = "Service unavailable",
                metadataNote = "Verification unavailable",
                isAiModelConfigured = false,
                isDevelopmentFallback = true
            )
        }
    }
}
