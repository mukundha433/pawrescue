package com.example.model

enum class AnimalDetectionState(val label: String) {
    ANIMAL_DETECTED("Animal Detected"),
    NO_ANIMAL_DETECTED("No Animal Detected"),
    UNCERTAIN("Uncertain Detection"),
    PENDING("Verification Pending")
}

enum class AuthenticityRating(val label: String) {
    LIKELY_REAL("Likely Real"),
    SUSPICIOUS("Suspicious"),
    LIKELY_SYNTHETIC("Likely Synthetic / Generated"),
    UNCERTAIN("Uncertain Authenticity"),
    PENDING("AI Verification Pending")
}

enum class ImageQualityRating(val label: String) {
    EXCELLENT("Good"),
    GOOD("Good"),
    ACCEPTABLE("Acceptable"),
    SUBOPTIMAL_LIGHTING("Suboptimal Lighting"),
    POOR("Poor"),
    BLURRY("Blurry"),
    OVEREXPOSED("Overexposed"),
    CORRUPTED("Corrupted / Unreadable"),
    UNDETERMINED("Analyzing")
}

enum class EvidenceProcessingState {
    IDLE,
    SELECTING,
    CAPTURING,
    PROCESSING,
    ANALYZING,
    VERIFIED,
    REVIEW_REQUIRED,
    FAILED
}

enum class VerificationStatus(val label: String) {
    VERIFIED("Verified"),
    REVIEW_REQUIRED("Review Required"),
    FAILED("Failed"),
    PENDING_REVIEW("Pending Review")
}

data class ImageValidationResult(
    val isValid: Boolean,
    val isCorrupted: Boolean = false,
    val qualityRating: ImageQualityRating = ImageQualityRating.GOOD,
    val qualityScorePercent: Int = 90,
    val qualityMessage: String = "Resolution & clarity verified",
    val dimensions: String? = null,
    val imageHash: String = "",
    val exifGpsLatitude: Double? = null,
    val exifGpsLongitude: Double? = null,
    val hasExifGps: Boolean = false,
    val captureTimestamp: String? = null,
    val cameraDevice: String? = null,
    val avgLuminance: Float = 128f,
    val sharpnessVariance: Double = 50.0
)

data class AIAnalysisResult(
    val animalDetectedState: AnimalDetectionState = AnimalDetectionState.PENDING,
    val animalDetected: Boolean? = null,
    val animalType: String? = null,
    val confidence: Float? = null, // e.g. 0.96f for 96%
    val confidencePercent: Int? = null, // e.g. 96
    val speciesMismatch: Boolean = false,
    val mismatchWarning: String? = null,
    val injuryDetected: Boolean? = null,
    val injuryType: String? = null,
    val injuryConfidencePercent: Int? = null,
    val imageQuality: ImageQualityRating = ImageQualityRating.GOOD,
    val qualityScorePercent: Int = 90,
    val qualityNote: String = "Resolution & clarity verified",
    val authenticityRating: AuthenticityRating = AuthenticityRating.PENDING,
    val authenticityStatus: String = "AI Verification Pending",
    val authenticityConfidencePercent: Int? = null,
    val authenticityExplanation: String = "Metadata inspected",
    val duplicateStatus: String = "0 Matches • No duplicate found",
    val isSuspectedDuplicate: Boolean = false,
    val matchingReportId: String? = null,
    val metadataNote: String = "Metadata inspected",
    val overallStatus: VerificationStatus = VerificationStatus.PENDING_REVIEW,
    val triageRecommendation: String? = null,
    val isAiModelConfigured: Boolean = false,
    val isDevelopmentFallback: Boolean = true,
    val rawAiResponse: String? = null
)

data class EvidenceItem(
    val id: String,
    val reportId: String? = null,
    val uriString: String,
    val localFilePath: String? = null,
    val angleNumber: Int = 1,
    val capturedAt: String = "Just now",
    val timestampMillis: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAccuracyMeters: Float? = null,
    val locationDescription: String? = null,
    val imageHash: String = "",
    val dimensions: String? = null,
    val validationResult: ImageValidationResult? = null,
    val aiAnalysis: AIAnalysisResult = AIAnalysisResult(),
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING_REVIEW,
    val isRescueTeamEvidence: Boolean = false,
    val rescueStageTag: String? = null
)
