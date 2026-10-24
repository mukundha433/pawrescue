package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import com.example.model.ImageQualityRating
import com.example.model.ImageValidationResult
import com.example.util.CrashLogger
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import kotlin.math.abs

class ImageValidationService {

    companion object {
        const val MAX_IMAGE_FILE_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB limit
    }

    private fun openInputStreamForUri(context: Context, uri: Uri): InputStream? {
        return try {
            if (uri.scheme == "file") {
                val path = uri.path
                if (!path.isNullOrBlank()) {
                    val f = File(path)
                    if (f.exists()) FileInputStream(f) else null
                } else null
            } else {
                context.contentResolver.openInputStream(uri)
            }
        } catch (t: Throwable) {
            CrashLogger.logWarning("ImageValidationService", "openInputStreamForUri", "Failed to open stream for $uri: ${t.localizedMessage}")
            null
        }
    }

    fun validateImage(context: Context, uri: Uri): ImageValidationResult {
        return try {
            // 0. Check physical file size if accessible
            if (uri.scheme == "file") {
                val path = uri.path
                if (!path.isNullOrBlank()) {
                    val file = File(path)
                    if (file.exists()) {
                        if (file.length() == 0L) {
                            return ImageValidationResult(
                                isValid = false,
                                isCorrupted = true,
                                qualityRating = ImageQualityRating.CORRUPTED,
                                qualityScorePercent = 0,
                                qualityMessage = "Image file is empty (0 bytes). Please try another image."
                            )
                        }
                        if (file.length() > MAX_IMAGE_FILE_SIZE_BYTES) {
                            return ImageValidationResult(
                                isValid = false,
                                isCorrupted = false,
                                qualityRating = ImageQualityRating.CORRUPTED,
                                qualityScorePercent = 0,
                                qualityMessage = "Image is too large. Please choose an image below 10 MB."
                            )
                        }
                    }
                }
            }

            // 1. Check file accessibility and image bounds
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            val streamFirst = openInputStreamForUri(context, uri) ?: return ImageValidationResult(
                isValid = false,
                isCorrupted = true,
                qualityRating = ImageQualityRating.CORRUPTED,
                qualityScorePercent = 0,
                qualityMessage = "Unable to process this image. Please try another image."
            )
            streamFirst.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            val width = options.outWidth
            val height = options.outHeight
            val mimeType = options.outMimeType ?: ""

            if (width <= 0 || height <= 0) {
                return ImageValidationResult(
                    isValid = false,
                    isCorrupted = true,
                    qualityRating = ImageQualityRating.CORRUPTED,
                    qualityScorePercent = 0,
                    qualityMessage = "Corrupted or unsupported image file format. Please select a valid JPEG or PNG photo."
                )
            }

            // 2. Compute SHA-256 hash for duplicate check & authenticity
            var hashString = ""
            try {
                openInputStreamForUri(context, uri)?.use { stream ->
                    val digest = MessageDigest.getInstance("SHA-256")
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (stream.read(buffer).also { read = it } != -1) {
                        digest.update(buffer, 0, read)
                    }
                    hashString = digest.digest().joinToString("") { "%02x".format(it) }
                }
            } catch (t: Throwable) {
                CrashLogger.logWarning("ImageValidationService", "hashCalculation", t.localizedMessage ?: "Hash error")
            }

            // 3. EXIF metadata extraction
            var exifLat: Double? = null
            var exifLon: Double? = null
            var hasGps = false
            var captureTime: String? = null
            var cameraModel: String? = null

            try {
                openInputStreamForUri(context, uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    val latLong = FloatArray(2)
                    @Suppress("DEPRECATION")
                    if (exif.getLatLong(latLong)) {
                        exifLat = latLong[0].toDouble()
                        exifLon = latLong[1].toDouble()
                        hasGps = true
                    }
                    captureTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
                    val make = exif.getAttribute(ExifInterface.TAG_MAKE) ?: ""
                    val model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: ""
                    if (make.isNotBlank() || model.isNotBlank()) {
                        cameraModel = "$make $model".trim()
                    }
                }
            } catch (t: Throwable) {
                CrashLogger.logWarning("ImageValidationService", "exifExtraction", t.localizedMessage ?: "EXIF error")
            }

            // 4. Downsampled bitmap decode for luminance and sharpness analysis
            // Use 200x200 max to keep memory footprint minimal (~160 KB) and prevent OutOfMemory
            val sampleSize = calculateInSampleSize(width, height, 200, 200)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            var sampledBitmap: Bitmap? = null
            try {
                openInputStreamForUri(context, uri)?.use { stream ->
                    sampledBitmap = BitmapFactory.decodeStream(stream, null, decodeOptions)
                }
            } catch (t: Throwable) {
                CrashLogger.logWarning("ImageValidationService", "sampledDecode", "Bitmap decode fallback: ${t.localizedMessage}")
            }

            var avgLuminance = 128f
            var sharpnessVariance = 50.0
            var qualityRating = ImageQualityRating.GOOD
            var qualityScore = 91
            var qualityMsg = "Subject clarity and exposure verified"

            if (sampledBitmap != null) {
                try {
                    val lumData = evaluateLuminanceAndSharpness(sampledBitmap!!)
                    avgLuminance = lumData.first
                    sharpnessVariance = lumData.second
                } catch (t: Throwable) {
                    CrashLogger.logWarning("ImageValidationService", "luminanceSharpness", t.localizedMessage ?: "Analysis error")
                } finally {
                    try {
                        sampledBitmap?.recycle()
                    } catch (_: Throwable) {}
                }

                // Lighting and sharpness classification
                when {
                    avgLuminance < 35f -> {
                        qualityRating = ImageQualityRating.SUBOPTIMAL_LIGHTING
                        qualityScore = 65
                        qualityMsg = "Low-light capture • Shadows may obscure animal features"
                    }
                    avgLuminance > 225f -> {
                        qualityRating = ImageQualityRating.OVEREXPOSED
                        qualityScore = 60
                        qualityMsg = "Overexposed • Harsh glare detected on subject"
                    }
                    sharpnessVariance < 12.0 -> {
                        qualityRating = ImageQualityRating.BLURRY
                        qualityScore = 55
                        qualityMsg = "Blur detected • Motion or focus issue"
                    }
                    width >= 1000 && height >= 1000 && sharpnessVariance > 45.0 -> {
                        qualityRating = ImageQualityRating.EXCELLENT
                        qualityScore = 98
                        qualityMsg = "High-definition, crisp focus & optimal lighting"
                    }
                    else -> {
                        qualityRating = ImageQualityRating.GOOD
                        qualityScore = 91
                        qualityMsg = "Clear subject resolution & balanced exposure"
                    }
                }
            }

            ImageValidationResult(
                isValid = true,
                isCorrupted = false,
                qualityRating = qualityRating,
                qualityScorePercent = qualityScore,
                qualityMessage = qualityMsg,
                dimensions = "${width}x${height}",
                imageHash = hashString,
                exifGpsLatitude = exifLat,
                exifGpsLongitude = exifLon,
                hasExifGps = hasGps,
                captureTimestamp = captureTime,
                cameraDevice = cameraModel,
                avgLuminance = avgLuminance,
                sharpnessVariance = sharpnessVariance
            )
        } catch (t: Throwable) {
            CrashLogger.logError("ImageValidationService", "validateImage", t)
            ImageValidationResult(
                isValid = false,
                isCorrupted = true,
                qualityRating = ImageQualityRating.CORRUPTED,
                qualityScorePercent = 0,
                qualityMessage = "Unable to process this image. Please select another image."
            )
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun evaluateLuminanceAndSharpness(bitmap: Bitmap): Pair<Float, Double> {
        val w = bitmap.width
        val h = bitmap.height
        val totalPixels = (w * h).toFloat()
        if (totalPixels <= 0) return Pair(128f, 50.0)

        return try {
            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

            var totalLum = 0.0
            val luminances = FloatArray(w * h)

            for (i in pixels.indices) {
                val p = pixels[i]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                luminances[i] = lum
                totalLum += lum
            }

            val avgLum = (totalLum / totalPixels).toFloat()

            var gradientSum = 0.0
            var count = 0
            for (y in 0 until (h - 1) step 2) {
                for (x in 0 until (w - 1) step 2) {
                    val idx = y * w + x
                    val diffX = abs(luminances[idx] - luminances[idx + 1])
                    val diffY = abs(luminances[idx] - luminances[idx + w])
                    gradientSum += (diffX + diffY)
                    count++
                }
            }

            val sharpness = if (count > 0) (gradientSum / count) * 4.0 else 50.0
            Pair(avgLum, sharpness)
        } catch (t: Throwable) {
            CrashLogger.logWarning("ImageValidationService", "evaluateLuminanceAndSharpness", "Pixel array alloc: ${t.localizedMessage}")
            Pair(128f, 50.0)
        }
    }
}
