package com.example.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.IncidentRepository
import com.example.model.AIAnalysisResult
import com.example.model.EvidenceItem
import com.example.model.ImageQualityRating
import com.example.model.Incident
import com.example.model.IncidentLocationData
import com.example.model.UrgencyLevel
import com.example.service.DefaultGeocodingService
import com.example.service.DefaultLocationService
import com.example.service.DeviceLocation
import com.example.service.EvidenceService
import com.example.service.GeocodingResult
import com.example.service.LocationAccuracyTier
import com.example.service.LocationHelper
import com.example.service.LocationService
import com.example.service.LocationState
import com.example.ui.components.PawRescueMapView
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnPrimaryContainer
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.SecondarySlate
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.util.CrashLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewIncidentIntakeScreen(
    onNavigateBack: () -> Unit,
    onSubmitSuccess: (String) -> Unit,
    onSosClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val locationService: LocationService = remember { DefaultLocationService() }
    val geocodingService = remember { DefaultGeocodingService() }

    // -------------------------------------------------------------------------
    // 3-STEP WORKFLOW HIERARCHY
    // Step 1: Report Details (Animal Info + Field Evidence)
    // Step 2: AI Verification & Location (Section A: AI Telemetry, Section B: Incident Location)
    // Step 3: Review & Submit (Summary + Mini Map + Submit)
    // -------------------------------------------------------------------------
    var currentStep by remember { mutableIntStateOf(1) }

    // STEP 1 STATE: Animal Information
    var selectedSpecies by remember { mutableStateOf("Dog") }
    var selectedSituation by remember { mutableStateOf("Injured") }
    var selectedSeverity by remember { mutableStateOf("Critical") }
    var animalCount by remember { mutableIntStateOf(1) }
    var notes by remember {
        mutableStateOf("")
    }

    // STEP 1 STATE: Field Evidence (Up to 4 Photos)
    var evidenceList by remember { mutableStateOf<List<EvidenceItem>>(emptyList()) }
    var selectedEvidenceIndex by remember { mutableIntStateOf(0) }
    var isProcessingEvidence by remember { mutableStateOf(false) }

    // User Feedback / Error Banner
    var userErrorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog state for photo capture
    var showSourceChooserDialog by remember { mutableStateOf(false) }
    var pendingAngleTarget by remember { mutableIntStateOf(1) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoPreviewConfirmDialog by remember { mutableStateOf(false) }
    var pendingCapturedPreviewUri by remember { mutableStateOf<Uri?>(null) }

    // STEP 2 STATE: Incident Location (Real OSM & GPS)
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var accuracyMeters by remember { mutableStateOf<Float?>(null) }
    var locationAddress by remember { mutableStateOf("") }
    var locationSource by remember { mutableStateOf("pending") } // "device_gps", "map_pin", "search"
    var isLocationConfirmed by remember { mutableStateOf(false) }
    var userDeviceLocation by remember { mutableStateOf<DeviceLocation?>(null) }
    var isLocatingGps by remember { mutableStateOf(false) }
    var confirmedLocationData by remember { mutableStateOf<IncidentLocationData?>(null) }

    // Search Location state
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<GeocodingResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // STEP 2 STATE: AI Verification Telemetry
    var aiAnalysisResult by remember { mutableStateOf<AIAnalysisResult?>(null) }
    var isRunningAiVerification by remember { mutableStateOf(false) }

    // STEP 3 STATE: Submission & Confirmation
    var isSubmitting by remember { mutableStateOf(false) }
    var submittedIncident by remember { mutableStateOf<Incident?>(null) }

    // Initial GPS location attempt if permission already available
    LaunchedEffect(Unit) {
        if (locationService.hasPermission(context) && latitude == null) {
            coroutineScope.launch {
                isLocatingGps = true
                val state = locationService.getCurrentLocation(context)
                if (state is LocationState.Success) {
                    userDeviceLocation = state.location
                    latitude = state.location.latitude
                    longitude = state.location.longitude
                    accuracyMeters = state.location.accuracyMeters
                    locationSource = "device_gps"
                    locationAddress = geocodingService.reverseGeocode(context, state.location.latitude, state.location.longitude)
                }
                isLocatingGps = false
            }
        }
    }

    // Handle system back navigation according to 3-step hierarchy
    BackHandler {
        when (currentStep) {
            3 -> currentStep = 2
            2 -> currentStep = 1
            else -> onNavigateBack()
        }
    }

    // -------------------------------------------------------------------------
    // CAMERA & GALLERY LAUNCHERS (Crash-proof)
    // -------------------------------------------------------------------------
    fun createTempPictureUri(): Uri? {
        return try {
            val tempFile = File.createTempFile("pawrescue_cam_", ".jpg", context.cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
        } catch (t: Throwable) {
            CrashLogger.logError("NewIncidentIntakeScreen", "createTempPictureUri", t)
            null
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        try {
            if (success && tempCameraUri != null) {
                pendingCapturedPreviewUri = tempCameraUri
                showPhotoPreviewConfirmDialog = true
            }
        } catch (t: Throwable) {
            CrashLogger.logError("NewIncidentIntakeScreen", "takePictureCallback", t)
            userErrorMessage = "Unable to process captured photo. Please try again."
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = createTempPictureUri()
                if (uri != null) {
                    tempCameraUri = uri
                    takePictureLauncher.launch(uri)
                } else {
                    userErrorMessage = "Unable to initialize camera storage. You can select photos from your gallery instead."
                }
            } catch (e: ActivityNotFoundException) {
                userErrorMessage = "No camera application installed on this device. Please choose from gallery."
            } catch (t: Throwable) {
                CrashLogger.logError("NewIncidentIntakeScreen", "cameraPermissionLauncher", t)
                userErrorMessage = "Camera launch failed. Please select from gallery."
            }
        } else {
            userErrorMessage = "Camera permission was denied. You can select evidence from your gallery."
        }
    }

    fun launchCameraForTarget(targetAngle: Int) {
        pendingAngleTarget = targetAngle
        try {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                val uri = createTempPictureUri()
                if (uri != null) {
                    tempCameraUri = uri
                    takePictureLauncher.launch(uri)
                } else {
                    userErrorMessage = "Unable to prepare camera file. Please select from gallery."
                }
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } catch (e: ActivityNotFoundException) {
            userErrorMessage = "No camera application found on this device. Please select from your gallery."
        } catch (t: Throwable) {
            CrashLogger.logError("NewIncidentIntakeScreen", "launchCameraForTarget", t)
            userErrorMessage = "Camera is currently unavailable. Please select from gallery."
        }
    }

    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessingEvidence = true
                userErrorMessage = null
                try {
                    val service = EvidenceService()
                    val existing = IncidentRepository.incidents.value
                    val item = service.processEvidenceImage(
                        context = context,
                        sourceUri = uri,
                        angleNumber = pendingAngleTarget,
                        selectedSpecies = selectedSpecies,
                        selectedSituation = selectedSituation,
                        existingIncidents = existing
                    )

                    val valResult = item.validationResult
                    if (valResult != null && (!valResult.isValid || valResult.isCorrupted)) {
                        userErrorMessage = valResult.qualityMessage
                    } else {
                        val current = evidenceList.toMutableList()
                        val existingIdx = current.indexOfFirst { it.angleNumber == pendingAngleTarget }
                        if (existingIdx >= 0) {
                            current[existingIdx] = item
                            selectedEvidenceIndex = existingIdx
                        } else {
                            current.add(item)
                            selectedEvidenceIndex = current.lastIndex
                        }
                        evidenceList = current
                        aiAnalysisResult = item.aiAnalysis
                    }
                } catch (t: Throwable) {
                    CrashLogger.logError("NewIncidentIntakeScreen", "processVisualMedia", t)
                    userErrorMessage = "Unable to process selected image. Please try another image."
                } finally {
                    isProcessingEvidence = false
                    showSourceChooserDialog = false
                }
            }
        }
    }

    fun launchGalleryForTarget(targetAngle: Int) {
        pendingAngleTarget = targetAngle
        try {
            pickVisualMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (e: ActivityNotFoundException) {
            userErrorMessage = "Photo picker is not supported on this device."
        } catch (t: Throwable) {
            CrashLogger.logError("NewIncidentIntakeScreen", "launchGalleryForTarget", t)
            userErrorMessage = "Unable to open gallery. Please try again."
        }
    }

    fun processConfirmedPhoto(uri: Uri, angleNumber: Int) {
        isProcessingEvidence = true
        userErrorMessage = null
        coroutineScope.launch {
            try {
                val service = EvidenceService()
                val existing = IncidentRepository.incidents.value
                val item = service.processEvidenceImage(
                    context = context,
                    sourceUri = uri,
                    angleNumber = angleNumber,
                    selectedSpecies = selectedSpecies,
                    selectedSituation = selectedSituation,
                    existingIncidents = existing
                )
                val valResult = item.validationResult
                if (valResult != null && (!valResult.isValid || valResult.isCorrupted)) {
                    userErrorMessage = valResult.qualityMessage
                } else {
                    val current = evidenceList.toMutableList()
                    val existingIdx = current.indexOfFirst { it.angleNumber == angleNumber }
                    if (existingIdx >= 0) {
                        current[existingIdx] = item
                        selectedEvidenceIndex = existingIdx
                    } else {
                        current.add(item)
                        selectedEvidenceIndex = current.lastIndex
                    }
                    evidenceList = current
                    aiAnalysisResult = item.aiAnalysis
                }
            } catch (t: Throwable) {
                CrashLogger.logError("NewIncidentIntakeScreen", "processConfirmedPhoto", t)
                userErrorMessage = "Unable to save evidence photo. Please try again."
            } finally {
                isProcessingEvidence = false
                showPhotoPreviewConfirmDialog = false
                showSourceChooserDialog = false
            }
        }
    }

    // -------------------------------------------------------------------------
    // LOCATION ACTIONS: USE MY CURRENT LOCATION & PERMISSION
    // -------------------------------------------------------------------------
    fun fetchGpsLocation() {
        coroutineScope.launch {
            isLocatingGps = true
            userErrorMessage = null
            when (val state = locationService.getCurrentLocation(context)) {
                is LocationState.Success -> {
                    val loc = state.location
                    userDeviceLocation = loc
                    latitude = loc.latitude
                    longitude = loc.longitude
                    accuracyMeters = loc.accuracyMeters
                    locationSource = "device_gps"
                    isLocationConfirmed = false
                    val addr = geocodingService.reverseGeocode(context, loc.latitude, loc.longitude)
                    locationAddress = addr
                }
                is LocationState.GpsDisabled -> {
                    userErrorMessage = "Device GPS is disabled. Please enable Location in Android settings or select rescue location manually on the map."
                }
                is LocationState.PermissionRequired -> {
                    userErrorMessage = "Location permission is required to detect your GPS position."
                }
                is LocationState.Error -> {
                    userErrorMessage = state.message
                }
                else -> {}
            }
            isLocatingGps = false
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchGpsLocation()
        } else {
            userErrorMessage = "Location permission denied. You can tap anywhere on the map or search to set the rescue location manually."
        }
    }

    fun onUseMyCurrentLocationClick() {
        if (locationService.hasPermission(context)) {
            fetchGpsLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Reverse geocode when map marker is dragged or tapped
    fun onMapLocationPicked(lat: Double, lon: Double, fromDrag: Boolean) {
        latitude = lat
        longitude = lon
        isLocationConfirmed = false
        locationSource = if (fromDrag) "map_pin" else "map_tap"
        coroutineScope.launch {
            val addr = geocodingService.reverseGeocode(context, lat, lon)
            locationAddress = addr
        }
    }

    // Search location execution
    fun onExecuteSearch(q: String) {
        val trimmed = q.trim()
        if (trimmed.length < 3) {
            userErrorMessage = "Please enter at least 3 characters to search for a rescue location."
            return
        }
        coroutineScope.launch {
            isSearching = true
            userErrorMessage = null
            try {
                val results = geocodingService.searchLocations(context, trimmed)
                if (results.isEmpty()) {
                    userErrorMessage = "No locations found matching '$trimmed'. You can tap directly on the map to pinpoint."
                }
                searchResults = results
            } catch (t: Throwable) {
                CrashLogger.logError("NewIncidentIntakeScreen", "searchLocations", t)
                userErrorMessage = "Location search error. Please try selecting directly on the map."
            } finally {
                isSearching = false
            }
        }
    }

    // Confirm Rescue Location Button Click Handler
    fun onConfirmLocationClick() {
        val lat = latitude
        val lon = longitude
        if (lat == null || lon == null || !lat.isFinite() || !lon.isFinite()) {
            userErrorMessage = "Please select a valid location on the map or tap 'Use My Current Location'."
            return
        }

        val addr = if (locationAddress.isNotBlank()) locationAddress else LocationHelper.getAddressFromCoordinates(context, lat, lon)
        locationAddress = addr

        val data = IncidentLocationData(
            latitude = lat,
            longitude = lon,
            accuracyMeters = accuracyMeters,
            address = addr,
            timestamp = "Just now",
            locationSource = locationSource,
            locationConfirmed = true
        )
        confirmedLocationData = data
        isLocationConfirmed = true
        userErrorMessage = null
    }

    // -------------------------------------------------------------------------
    // DIALOGS: SOURCE CHOOSER & CAMERA PREVIEW
    // -------------------------------------------------------------------------
    if (showSourceChooserDialog) {
        AlertDialog(
            onDismissRequest = { showSourceChooserDialog = false },
            title = {
                Text(
                    text = if (pendingAngleTarget <= evidenceList.size && evidenceList.any { it.angleNumber == pendingAngleTarget }) {
                        "Retake Photo $pendingAngleTarget"
                    } else {
                        "Upload Photo (Photo $pendingAngleTarget of 4)"
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose how to capture animal field evidence:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondarySlate
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainer)
                            .clickable {
                                showSourceChooserDialog = false
                                launchCameraForTarget(pendingAngleTarget)
                            }
                            .padding(14.dp)
                            .testTag("dialog_option_take_photo"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = PrimaryEmerald)
                        }
                        Column {
                            Text("Take Photo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text("Use device camera to snap photo", style = MaterialTheme.typography.bodySmall, color = SecondarySlate)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainer)
                            .clickable {
                                showSourceChooserDialog = false
                                launchGalleryForTarget(pendingAngleTarget)
                            }
                            .padding(14.dp)
                            .testTag("dialog_option_choose_gallery"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = PrimaryEmerald)
                        }
                        Column {
                            Text("Choose from Gallery", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text("Select an existing photo from device album", style = MaterialTheme.typography.bodySmall, color = SecondarySlate)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceChooserDialog = false }) {
                    Text("Cancel", color = SecondarySlate)
                }
            }
        )
    }

    if (showPhotoPreviewConfirmDialog && pendingCapturedPreviewUri != null) {
        AlertDialog(
            onDismissRequest = { showPhotoPreviewConfirmDialog = false },
            title = {
                Text(
                    text = "Confirm Photo • Photo $pendingAngleTarget",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerHigh)
                    ) {
                        AsyncImage(
                            model = pendingCapturedPreviewUri,
                            contentDescription = "Captured camera preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        text = "Review the photo above. Is the animal clearly visible for triage?",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondarySlate,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingCapturedPreviewUri
                        if (uri != null) {
                            processConfirmedPhoto(uri, pendingAngleTarget)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_photo_btn")
                ) {
                    Text("Confirm Photo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPhotoPreviewConfirmDialog = false
                        launchCameraForTarget(pendingAngleTarget)
                    },
                    modifier = Modifier.testTag("retake_preview_btn")
                ) {
                    Text("Retake", color = PrimaryEmerald, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // SUBMISSION SUCCESS CONFIRMATION MODAL
    // -------------------------------------------------------------------------
    if (submittedIncident != null) {
        val incident = submittedIncident!!
        AlertDialog(
            onDismissRequest = { /* Force explicit user choice */ },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(22.dp))
                    }
                    Text("Rescue Request Submitted", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your request has been successfully dispatched to the rescue organization command center.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = SurfaceContainerLow,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Request ID:", fontSize = 12.sp, color = SecondarySlate)
                                Text(incident.id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Initial Status:", fontSize = 12.sp, color = SecondarySlate)
                                Text("Submitted", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Species / Situation:", fontSize = 12.sp, color = SecondarySlate)
                                Text("${incident.species} • ${incident.situation}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Location:", fontSize = 12.sp, color = SecondarySlate)
                                Text(incident.address, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                            }
                        }
                    }

                    Text(
                        text = "You can track live squad dispatch and medical ward updates under 'My Reports'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondarySlate
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val caseId = incident.id
                        submittedIncident = null
                        onSubmitSuccess(caseId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("track_request_btn")
                ) {
                    Text("Track Request", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        submittedIncident = null
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("back_to_dashboard_btn")
                ) {
                    Text("Back to Dashboard", color = SecondarySlate)
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // MAIN SCREEN CONTENT
    // -------------------------------------------------------------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP 3-STEP PROGRESS INDICATOR
        // 1 Report Details → 2 AI & Location → 3 Review
        WorkflowProgressIndicator(
            currentStep = currentStep,
            onStepClick = { step -> currentStep = step }
        )

        // Dismissible user feedback/error banner
        AnimatedVisibility(
            visible = userErrorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            userErrorMessage?.let { errMsg ->
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                            Text(errMsg, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        IconButton(
                            onClick = { userErrorMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = ErrorRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // SEPARATE SCREENS ACCORDING TO CURRENT STEP
        when (currentStep) {
            1 -> {
                Step1ReportDetailsContent(
                    species = selectedSpecies,
                    onSpeciesChange = { selectedSpecies = it },
                    situation = selectedSituation,
                    onSituationChange = { selectedSituation = it },
                    urgency = selectedSeverity,
                    onUrgencyChange = { selectedSeverity = it },
                    animalCount = animalCount,
                    onAnimalCountChange = { animalCount = it },
                    notes = notes,
                    onNotesChange = { notes = it },
                    evidenceList = evidenceList,
                    selectedEvidenceIndex = selectedEvidenceIndex,
                    onSelectedEvidenceIndexChange = { selectedEvidenceIndex = it },
                    onTakePhotoClick = { targetSlot -> launchCameraForTarget(targetSlot) },
                    onChooseGalleryClick = { targetSlot -> launchGalleryForTarget(targetSlot) },
                    onRetakePhotoClick = { targetSlot ->
                        pendingAngleTarget = targetSlot
                        showSourceChooserDialog = true
                    },
                    onRemovePhotoClick = { removeIndex ->
                        val updated = evidenceList.toMutableList()
                        if (removeIndex in updated.indices) {
                            updated.removeAt(removeIndex)
                            evidenceList = updated
                            selectedEvidenceIndex = 0.coerceAtMost(updated.size - 1)
                        }
                    },
                    isProcessingEvidence = isProcessingEvidence,
                    onContinue = {
                        if (aiAnalysisResult == null && evidenceList.isNotEmpty()) {
                            isRunningAiVerification = true
                            coroutineScope.launch {
                                try {
                                    val service = EvidenceService()
                                    val existing = IncidentRepository.incidents.value
                                    val firstItem = evidenceList.first()
                                    val reprocessed = service.processEvidenceImage(
                                        context = context,
                                        sourceUri = Uri.parse(firstItem.uriString),
                                        angleNumber = firstItem.angleNumber,
                                        selectedSpecies = selectedSpecies,
                                        selectedSituation = selectedSituation,
                                        existingIncidents = existing
                                    )
                                    aiAnalysisResult = reprocessed.aiAnalysis
                                } catch (t: Throwable) {
                                    CrashLogger.logError("NewIncidentIntakeScreen", "onContinueAiAnalysis", t)
                                } finally {
                                    isRunningAiVerification = false
                                    currentStep = 2
                                }
                            }
                        } else {
                            currentStep = 2
                        }
                    }
                )
            }

            2 -> {
                Step2AiAndLocationContent(
                    species = selectedSpecies,
                    situation = selectedSituation,
                    evidenceList = evidenceList,
                    selectedEvidenceIndex = selectedEvidenceIndex,
                    aiAnalysis = aiAnalysisResult ?: (evidenceList.firstOrNull()?.aiAnalysis ?: AIAnalysisResult()),
                    latitude = latitude,
                    longitude = longitude,
                    accuracyMeters = accuracyMeters,
                    address = locationAddress,
                    onAddressChange = {
                        locationAddress = it
                        isLocationConfirmed = false
                    },
                    isLocationConfirmed = isLocationConfirmed,
                    userDeviceLocation = userDeviceLocation,
                    isLocatingGps = isLocatingGps,
                    onUseMyCurrentLocation = { onUseMyCurrentLocationClick() },
                    onMapLocationPicked = { lat, lon, fromDrag -> onMapLocationPicked(lat, lon, fromDrag) },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    searchResults = searchResults,
                    isSearching = isSearching,
                    onSearchExecute = { q -> onExecuteSearch(q) },
                    onSelectSearchResult = { result ->
                        latitude = result.latitude
                        longitude = result.longitude
                        locationAddress = result.displayName
                        locationSource = "search"
                        isLocationConfirmed = false
                        searchQuery = ""
                        searchResults = emptyList()
                    },
                    onClearSearch = {
                        searchQuery = ""
                        searchResults = emptyList()
                    },
                    onConfirmLocation = { onConfirmLocationClick() },
                    onBack = { currentStep = 1 },
                    onContinue = { currentStep = 3 }
                )
            }

            3 -> {
                Step3ReviewAndSubmitContent(
                    species = selectedSpecies,
                    situation = selectedSituation,
                    urgency = selectedSeverity,
                    animalCount = animalCount,
                    notes = notes,
                    evidenceList = evidenceList,
                    aiAnalysis = aiAnalysisResult ?: (evidenceList.firstOrNull()?.aiAnalysis ?: AIAnalysisResult()),
                    confirmedLocation = confirmedLocationData ?: IncidentLocationData(
                        latitude = latitude ?: 0.0,
                        longitude = longitude ?: 0.0,
                        accuracyMeters = accuracyMeters,
                        address = if (locationAddress.isNotBlank()) locationAddress else "Rescue Location",
                        locationSource = locationSource,
                        locationConfirmed = isLocationConfirmed
                    ),
                    onEditEvidence = { currentStep = 1 },
                    onEditDetails = { currentStep = 1 },
                    onEditLocation = { currentStep = 2 },
                    onBack = { currentStep = 2 },
                    isSubmitting = isSubmitting,
                    onSubmit = {
                        isSubmitting = true
                        coroutineScope.launch {
                            try {
                                delay(500)
                                val urgencyEnum = when (selectedSeverity) {
                                    "Critical" -> UrgencyLevel.CRITICAL
                                    "High" -> UrgencyLevel.HIGH
                                    "Moderate" -> UrgencyLevel.MEDIUM
                                    else -> UrgencyLevel.LOW
                                }

                                val finalLocation = confirmedLocationData ?: IncidentLocationData(
                                    latitude = latitude ?: 0.0,
                                    longitude = longitude ?: 0.0,
                                    accuracyMeters = accuracyMeters,
                                    address = if (locationAddress.isNotBlank()) locationAddress else "Rescue Location",
                                    locationSource = locationSource,
                                    locationConfirmed = true
                                )

                                val created = IncidentRepository.addNewIntake(
                                    species = selectedSpecies,
                                    situation = selectedSituation,
                                    urgency = urgencyEnum,
                                    count = animalCount,
                                    notes = notes,
                                    address = finalLocation.address,
                                    latitude = finalLocation.latitude,
                                    longitude = finalLocation.longitude,
                                    accuracyMeters = finalLocation.accuracyMeters,
                                    locationData = finalLocation,
                                    evidenceList = evidenceList,
                                    aiAnalysis = aiAnalysisResult
                                )
                                submittedIncident = created
                            } catch (t: Throwable) {
                                CrashLogger.logError("NewIncidentIntakeScreen", "submitRescueRequest", t)
                                userErrorMessage = "Unable to submit request. Please try again."
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PROGRESS INDICATOR: 1 Report Details → 2 AI & Location → 3 Review
// -----------------------------------------------------------------------------
@Composable
private fun WorkflowProgressIndicator(
    currentStep: Int,
    onStepClick: (Int) -> Unit
) {
    Surface(
        color = SurfaceContainerLow,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().testTag("workflow_progress_rail")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkflowStepBadge(
                stepNumber = 1,
                label = "Report Details",
                isCurrent = currentStep == 1,
                isCompleted = currentStep > 1,
                onClick = { if (currentStep > 1) onStepClick(1) }
            )

            WorkflowStepConnector(isCompleted = currentStep > 1)

            WorkflowStepBadge(
                stepNumber = 2,
                label = "AI & Location",
                isCurrent = currentStep == 2,
                isCompleted = currentStep > 2,
                onClick = { if (currentStep > 2) onStepClick(2) }
            )

            WorkflowStepConnector(isCompleted = currentStep > 2)

            WorkflowStepBadge(
                stepNumber = 3,
                label = "Review",
                isCurrent = currentStep == 3,
                isCompleted = false,
                onClick = {}
            )
        }
    }
}

@Composable
private fun WorkflowStepBadge(
    stepNumber: Int,
    label: String,
    isCurrent: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = isCompleted, onClick = onClick)
            .testTag("workflow_step_$stepNumber")
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> PrimaryEmerald
                        isCurrent -> PrimaryContainer
                        else -> SurfaceContainerHigh
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "$stepNumber",
                    color = if (isCurrent) OnPrimaryContainer else SecondarySlate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isCompleted -> PrimaryEmerald
                isCurrent -> MaterialTheme.colorScheme.onSurface
                else -> SecondarySlate
            }
        )
    }
}

@Composable
private fun WorkflowStepConnector(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(2.dp)
            .background(if (isCompleted) PrimaryEmerald else SurfaceContainerHigh)
    )
}

// -----------------------------------------------------------------------------
// STEP 1 CONTENT: REPORT DETAILS
// -----------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step1ReportDetailsContent(
    species: String,
    onSpeciesChange: (String) -> Unit,
    situation: String,
    onSituationChange: (String) -> Unit,
    urgency: String,
    onUrgencyChange: (String) -> Unit,
    animalCount: Int,
    onAnimalCountChange: (Int) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    evidenceList: List<EvidenceItem>,
    selectedEvidenceIndex: Int,
    onSelectedEvidenceIndexChange: (Int) -> Unit,
    onTakePhotoClick: (Int) -> Unit,
    onChooseGalleryClick: (Int) -> Unit,
    onRetakePhotoClick: (Int) -> Unit,
    onRemovePhotoClick: (Int) -> Unit,
    isProcessingEvidence: Boolean,
    onContinue: () -> Unit
) {
    val isFormComplete = evidenceList.isNotEmpty() && species.isNotBlank() && situation.isNotBlank()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Report Animal Distress",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Provide information about the animal and upload clear evidence.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondarySlate
                )
            }
        }

        // SECTION 1: FIELD EVIDENCE
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("field_evidence_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = PrimaryEmerald)
                            Text(
                                text = "Field Evidence",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(SurfaceContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${evidenceList.size}/4 Photos",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SecondarySlate
                            )
                        }
                    }

                    if (evidenceList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceContainerLow)
                                .border(1.5.dp, PrimaryContainer, RoundedCornerShape(14.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(24.dp))
                                }

                                Text(
                                    text = "Capture Animal Evidence",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Take a clear photo with your device camera or select from your gallery. Clear photos from multiple angles help triage injuries.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondarySlate,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { onTakePhotoClick(1) },
                                        modifier = Modifier.weight(1f).height(44.dp).testTag("take_photo_initial_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("Take Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { onChooseGalleryClick(1) },
                                        modifier = Modifier.weight(1f).height(44.dp).testTag("choose_gallery_initial_btn"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(16.dp))
                                            Text("From Gallery", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val activeItem = evidenceList.getOrNull(selectedEvidenceIndex) ?: evidenceList.first()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceContainerHigh)
                        ) {
                            AsyncImage(
                                model = activeItem.uriString,
                                contentDescription = "Photo ${activeItem.angleNumber}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(Color(0xD90F172A))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Photo ${activeItem.angleNumber} of ${evidenceList.size}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(Color(0xD9213145))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = activeItem.dimensions ?: "Format Verified",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(evidenceList) { index, item ->
                                val isSelected = index == selectedEvidenceIndex
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) PrimaryEmerald else SurfaceContainerHigh,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onSelectedEvidenceIndexChange(index) }
                                ) {
                                    AsyncImage(
                                        model = item.uriString,
                                        contentDescription = "Thumbnail ${item.angleNumber}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .background(Color(0xCC000000))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("${item.angleNumber}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (evidenceList.size < 4) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SurfaceContainerLow)
                                            .border(1.dp, PrimaryContainer, RoundedCornerShape(10.dp))
                                            .clickable {
                                                val nextSlot = (1..4).firstOrNull { slot -> evidenceList.none { it.angleNumber == slot } } ?: 1
                                                onRetakePhotoClick(nextSlot)
                                            }
                                            .testTag("add_photo_thumb_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(20.dp))
                                            Text("Add", fontSize = 10.sp, color = PrimaryEmerald, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRetakePhotoClick(activeItem.angleNumber) },
                                modifier = Modifier.weight(1f).height(40.dp).testTag("retake_photo_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Cached, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Retake Photo", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = { onRemovePhotoClick(selectedEvidenceIndex) },
                                modifier = Modifier.height(40.dp).testTag("remove_photo_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // SECTION 2: ANIMAL INFORMATION
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("animal_info_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = PrimaryEmerald)
                        Text(
                            text = "Animal Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Species Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Species Identified", style = MaterialTheme.typography.labelMedium, color = SecondarySlate)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val speciesList = listOf("Dog", "Cat", "Cattle / Cow", "Goat", "Bird", "Other")
                            speciesList.forEach { sp ->
                                val isSelected = sp == species
                                FilterPill(
                                    label = sp,
                                    isSelected = isSelected,
                                    icon = if (isSelected) Icons.Default.Check else null,
                                    onClick = { onSpeciesChange(sp) }
                                )
                            }
                        }
                    }

                    // Observed Situation Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Observed Situation", style = MaterialTheme.typography.labelMedium, color = SecondarySlate)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val situations = listOf("Injured", "Sick / Weak", "Trapped", "Abandoned", "Aggressive", "Road Accident", "Stray / Homeless", "Other")
                            situations.forEach { sit ->
                                val isSelected = sit == situation
                                FilterPill(
                                    label = sit,
                                    isSelected = isSelected,
                                    icon = if (isSelected) Icons.Default.Check else null,
                                    onClick = { onSituationChange(sit) }
                                )
                            }
                        }
                    }

                    // Urgency Level Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Urgency Level", style = MaterialTheme.typography.labelMedium, color = SecondarySlate)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val urgencyList = listOf("Critical", "High", "Moderate", "Routine")
                            urgencyList.forEach { urg ->
                                val isSelected = urg == urgency
                                FilterPill(
                                    label = urg,
                                    isSelected = isSelected,
                                    icon = if (isSelected) Icons.Default.Check else null,
                                    onClick = { onUrgencyChange(urg) }
                                )
                            }
                        }
                    }

                    // Number of Animals (Minimum = 1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Number of Animals", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text("Minimum count: 1", style = MaterialTheme.typography.bodySmall, color = SecondarySlate)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainer)
                                    .clickable { if (animalCount > 1) onAnimalCountChange(animalCount - 1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease count", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = "$animalCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainer)
                                    .clickable { if (animalCount < 10) onAnimalCountChange(animalCount + 1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase count", tint = OnPrimaryContainer, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Additional Information Text Area
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Additional Information", style = MaterialTheme.typography.labelMedium, color = SecondarySlate)
                        OutlinedTextField(
                            value = notes,
                            onValueChange = onNotesChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("field_additional_info_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = SurfaceContainerHigh
                            ),
                            placeholder = { Text("e.g., Injured dog near roadside. It appears unable to walk and has a visible injury on its rear leg...", fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // BOTTOM BUTTON: Continue to AI & Location
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onContinue,
                    enabled = isFormComplete && !isProcessingEvidence,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("continue_to_step2_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessingEvidence) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying Media...")
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Continue to AI & Location", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (!isFormComplete) {
                    Text(
                        text = "Upload at least 1 photo and fill animal details to continue",
                        fontSize = 11.sp,
                        color = SecondarySlate,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 2 CONTENT: AI VERIFICATION + REAL INTERACTIVE OPENSTREETMAP
// -----------------------------------------------------------------------------
@Composable
private fun Step2AiAndLocationContent(
    species: String,
    situation: String,
    evidenceList: List<EvidenceItem>,
    selectedEvidenceIndex: Int,
    aiAnalysis: AIAnalysisResult,
    latitude: Double?,
    longitude: Double?,
    accuracyMeters: Float?,
    address: String,
    onAddressChange: (String) -> Unit,
    isLocationConfirmed: Boolean,
    userDeviceLocation: DeviceLocation?,
    isLocatingGps: Boolean,
    onUseMyCurrentLocation: () -> Unit,
    onMapLocationPicked: (Double, Double, Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<GeocodingResult>,
    isSearching: Boolean,
    onSearchExecute: (String) -> Unit,
    onSelectSearchResult: (GeocodingResult) -> Unit,
    onClearSearch: () -> Unit,
    onConfirmLocation: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val activeItem = evidenceList.getOrNull(selectedEvidenceIndex) ?: evidenceList.firstOrNull()
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Header Title & Subtitle
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "AI Verification & Location",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Verify the submitted evidence and pinpoint the rescue location.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondarySlate
                )
            }
        }

        // =====================================================================
        // SECTION A — AI IMAGE VERIFICATION
        // =====================================================================
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("ai_verification_section")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = "Section A — AI Image Verification",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(PrimaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (aiAnalysis.isAiModelConfigured) "VERIFIED" else "AI VERIFICATION PENDING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryContainer
                            )
                        }
                    }

                    if (activeItem != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerHigh)
                        ) {
                            AsyncImage(
                                model = activeItem.uriString,
                                contentDescription = "Evidence Image for AI Verification",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .background(Color(0xD90F172A))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Analyzed Media • ${activeItem.dimensions ?: "Standard Format"}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TriageCheckItem(
                            icon = Icons.Default.Pets,
                            iconTint = PrimaryEmerald,
                            title = "Animal Detection",
                            metric = if (aiAnalysis.animalDetected == true) "${aiAnalysis.animalType} (Detected)" else "AI verification pending",
                            metricColor = if (aiAnalysis.animalDetected == true) PrimaryEmerald else SecondarySlate,
                            subtitle = "$species reported by citizen • AI vision model pending verification"
                        )

                        TriageCheckItem(
                            icon = Icons.Default.Healing,
                            iconTint = PrimaryEmerald,
                            title = "Image Quality",
                            metric = "${aiAnalysis.imageQuality.label} (${aiAnalysis.qualityScorePercent}%)",
                            metricColor = when (aiAnalysis.imageQuality) {
                                ImageQualityRating.EXCELLENT, ImageQualityRating.GOOD -> PrimaryEmerald
                                ImageQualityRating.SUBOPTIMAL_LIGHTING, ImageQualityRating.OVEREXPOSED -> Color(0xFFE65100)
                                else -> ErrorRed
                            },
                            subtitle = aiAnalysis.qualityNote
                        )

                        TriageCheckItem(
                            icon = Icons.Default.Shield,
                            iconTint = PrimaryEmerald,
                            title = "Image Authenticity",
                            metric = aiAnalysis.authenticityStatus,
                            metricColor = PrimaryEmerald,
                            subtitle = aiAnalysis.metadataNote
                        )

                        TriageCheckItem(
                            icon = Icons.Default.Radar,
                            iconTint = if (aiAnalysis.isSuspectedDuplicate) ErrorRed else SecondarySlate,
                            title = "Duplicate Check",
                            metric = if (aiAnalysis.isSuspectedDuplicate) "1 Match Found" else "No nearby duplicate detected",
                            metricColor = if (aiAnalysis.isSuspectedDuplicate) ErrorRed else SecondarySlate,
                            subtitle = aiAnalysis.duplicateStatus
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceContainer)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = SecondarySlate, modifier = Modifier.size(16.dp))
                        Text(
                            text = "AI assists the organization with triage. Human triage dispatchers make the final rescue decision.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondarySlate,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // =====================================================================
        // SECTION B — INCIDENT LOCATION (Production Real Interactive OpenStreetMap)
        // =====================================================================
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("incident_location_section")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with GPS status indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryEmerald)
                            Text(
                                text = "Section B — Incident Location",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isLocationConfirmed) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(PrimaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(12.dp))
                                    Text("Location Confirmed", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnPrimaryContainer)
                                }
                            }
                        } else if (latitude != null && longitude != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(SurfaceContainerHigh)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("Position Selected", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = SecondarySlate)
                            }
                        }
                    }

                    // 1. Search Location Field Above Map
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_location_input"),
                            placeholder = { Text("Search rescue location (street, area, landmark)...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryEmerald)
                            },
                            trailingIcon = {
                                if (isSearching) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = PrimaryEmerald)
                                } else if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = onClearSearch) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                keyboardController?.hide()
                                onSearchExecute(searchQuery)
                            }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = SurfaceContainerHigh
                            )
                        )

                        // Search Results Dropdown List
                        if (searchResults.isNotEmpty()) {
                            Surface(
                                color = SurfaceContainerLow,
                                shape = RoundedCornerShape(10.dp),
                                shadowElevation = 3.dp,
                                modifier = Modifier.fillMaxWidth().testTag("search_results_container")
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    searchResults.forEach { res ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    keyboardController?.hide()
                                                    onSelectSearchResult(res)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(16.dp))
                                            Column {
                                                Text(res.displayName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                                if (res.area.isNotBlank()) {
                                                    Text(res.area, fontSize = 10.sp, color = SecondarySlate)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Real Interactive OpenStreetMap Container
                    PawRescueMapView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        latitude = latitude,
                        longitude = longitude,
                        userLocation = userDeviceLocation,
                        isInteractive = true,
                        initialZoom = if (latitude != null) 16.0 else 12.0,
                        onLocationSelected = { lat, lon, fromDrag ->
                            onMapLocationPicked(lat, lon, fromDrag)
                        }
                    )

                    // Drag & Tap Pin Instruction Guidance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Drag the red pin to the exact rescue location, or tap anywhere on the map.",
                            fontSize = 11.sp,
                            color = SecondarySlate
                        )
                    }

                    // 3. Action: Use My Current Location Button
                    Button(
                        onClick = onUseMyCurrentLocation,
                        enabled = !isLocatingGps,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("use_my_current_location_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLocatingGps) {
                            CircularProgressIndicator(color = OnPrimaryContainer, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detecting device GPS...", color = OnPrimaryContainer, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(18.dp))
                                Text("Use My Current Location", color = OnPrimaryContainer, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 4. Confirmed Rescue Address Field
                    OutlinedTextField(
                        value = address,
                        onValueChange = onAddressChange,
                        label = { Text("Confirmed Rescue Address", fontSize = 11.sp) },
                        placeholder = { Text("Select position on map or type street address", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("rescue_address_input"),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.NearMe, contentDescription = null, tint = PrimaryEmerald) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            unfocusedBorderColor = SurfaceContainerHigh
                        )
                    )

                    // 5. GPS Coordinates & Accuracy Readout
                    if (latitude != null && longitude != null && latitude.isFinite() && longitude.isFinite()) {
                        Surface(
                            color = SurfaceContainerLow,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val latDir = if (latitude >= 0) "N" else "S"
                                    val lonDir = if (longitude >= 0) "E" else "W"
                                    Text(
                                        text = String.format(Locale.US, "GPS: %.4f° %s, %.4f° %s", Math.abs(latitude), latDir, Math.abs(longitude), lonDir),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (accuracyMeters != null) {
                                        val tier = when {
                                            accuracyMeters <= 15f -> LocationAccuracyTier.VERIFIED
                                            accuracyMeters <= 40f -> LocationAccuracyTier.MODERATE
                                            else -> LocationAccuracyTier.LOW
                                        }

                                        val badgeColor = when (tier) {
                                            LocationAccuracyTier.VERIFIED -> PrimaryEmerald
                                            LocationAccuracyTier.MODERATE -> Color(0xFFD97706)
                                            LocationAccuracyTier.LOW -> Color(0xFFDC2626)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(badgeColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${tier.badgeLabel} (±${String.format(Locale.US, "%.1f", accuracyMeters)}m)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor
                                            )
                                        }
                                    }
                                }

                                if (accuracyMeters != null && accuracyMeters > 40f) {
                                    Text(
                                        text = "GPS accuracy is low. Move closer to an open area or drag the red pin to adjust manually.",
                                        fontSize = 10.sp,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }

                    // 6. Primary Action: Confirm Rescue Location Button
                    Button(
                        onClick = onConfirmLocation,
                        enabled = latitude != null && longitude != null,
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("confirm_rescue_location_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocationConfirmed) PrimaryEmerald else PrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                if (isLocationConfirmed) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isLocationConfirmed) Color.White else OnPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isLocationConfirmed) "Location Confirmed ✓" else "Confirm Rescue Location",
                                color = if (isLocationConfirmed) Color.White else OnPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // NAVIGATION BUTTONS: Back to Details & Continue to Review
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(50.dp).testTag("step2_back_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Back to Details", fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = onContinue,
                    enabled = isLocationConfirmed,
                    modifier = Modifier.weight(1.3f).height(50.dp).testTag("continue_to_step3_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Continue to Review", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 3 CONTENT: REVIEW & SUBMIT WITH REAL MAP PREVIEW
// -----------------------------------------------------------------------------
@Composable
private fun Step3ReviewAndSubmitContent(
    species: String,
    situation: String,
    urgency: String,
    animalCount: Int,
    notes: String,
    evidenceList: List<EvidenceItem>,
    aiAnalysis: AIAnalysisResult,
    confirmedLocation: IncidentLocationData,
    onEditEvidence: () -> Unit,
    onEditDetails: () -> Unit,
    onEditLocation: () -> Unit,
    onBack: () -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Review Rescue Request",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Check your information before submitting the rescue request.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondarySlate
                )
            }
        }

        // SECTION 1 — EVIDENCE SUMMARY
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("review_evidence_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Section 1 — Evidence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onEditEvidence, modifier = Modifier.testTag("edit_evidence_btn")) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(14.dp))
                                Text("Edit Evidence", color = PrimaryEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(evidenceList) { _, item ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceContainerHigh)
                            ) {
                                AsyncImage(
                                    model = item.uriString,
                                    contentDescription = "Review Evidence ${item.angleNumber}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color(0xCC000000))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Photo ${item.angleNumber}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Text("${evidenceList.size} photo(s) attached and format verified", fontSize = 11.sp, color = SecondarySlate)
                }
            }
        }

        // SECTION 2 — ANIMAL DETAILS
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("review_animal_details_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Section 2 — Animal Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onEditDetails, modifier = Modifier.testTag("edit_details_btn")) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(14.dp))
                                Text("Edit Details", color = PrimaryEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    ReviewDetailRow(label = "Species", value = species)
                    ReviewDetailRow(label = "Observed Situation", value = situation)
                    ReviewDetailRow(label = "Urgency Level", value = urgency, isUrgent = urgency == "Critical")
                    ReviewDetailRow(label = "Number of Animals", value = "$animalCount")
                    HorizontalDivider(color = SurfaceContainerHigh)
                    Text("Additional Notes:", fontSize = 12.sp, color = SecondarySlate, fontWeight = FontWeight.Medium)
                    Text(notes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // SECTION 3 — AI VERIFICATION (Read-Only)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("review_ai_verification_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Section 3 — AI Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(PrimaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (aiAnalysis.isAiModelConfigured) "VERIFIED" else "PENDING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryContainer
                            )
                        }
                    }

                    ReviewDetailRow(label = "Animal Detection", value = if (aiAnalysis.animalDetected == true) "${aiAnalysis.animalType} (Detected)" else "AI Verification Pending")
                    ReviewDetailRow(label = "Image Quality", value = "${aiAnalysis.imageQuality.label} (${aiAnalysis.qualityScorePercent}%)")
                    ReviewDetailRow(label = "Image Authenticity", value = aiAnalysis.authenticityStatus)
                    ReviewDetailRow(label = "Duplicate Check", value = if (aiAnalysis.isSuspectedDuplicate) "1 Match" else "0 Matches")

                    Text(
                        text = "AI verification results are recorded for emergency squad priority dispatch.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondarySlate,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // SECTION 4 — RESCUE LOCATION WITH REAL MAP PREVIEW
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("review_location_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Section 4 — Rescue Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onEditLocation, modifier = Modifier.testTag("edit_location_btn")) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(14.dp))
                                Text("Edit Location", color = PrimaryEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
                        Text(confirmedLocation.address, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    val latDir = if (confirmedLocation.latitude >= 0) "N" else "S"
                    val lonDir = if (confirmedLocation.longitude >= 0) "E" else "W"
                    Text(
                        text = String.format(Locale.US, "GPS: %.4f° %s, %.4f° %s", Math.abs(confirmedLocation.latitude), latDir, Math.abs(confirmedLocation.longitude), lonDir),
                        fontSize = 12.sp,
                        color = SecondarySlate
                    )

                    if (confirmedLocation.accuracyMeters != null) {
                        Text(
                            text = "GPS Accuracy: ±${String.format(Locale.US, "%.1f", confirmedLocation.accuracyMeters)} m",
                            fontSize = 11.sp,
                            color = PrimaryEmerald,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Real Interactive OpenStreetMap Preview in Review Screen
                    PawRescueMapView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        latitude = confirmedLocation.latitude,
                        longitude = confirmedLocation.longitude,
                        isInteractive = false,
                        initialZoom = 15.0
                    )
                }
            }
        }

        // PRIMARY BUTTON: Submit Rescue Request
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSubmit,
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_rescue_request_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("Submitting Rescue Request...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("Submit Rescue Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("step3_back_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Back to AI & Location", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewDetailRow(label: String, value: String, isUrgent: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = SecondarySlate)
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUrgent) ErrorRed else MaterialTheme.colorScheme.onSurface
        )
    }
}

// -----------------------------------------------------------------------------
// FILTER PILL COMPOSABLE
// -----------------------------------------------------------------------------
@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(if (isSelected) PrimaryEmerald else SurfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("filter_pill_${label.lowercase().replace(" ", "_").replace("/", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// -----------------------------------------------------------------------------
// TRIAGE CHECK ITEM COMPOSABLE
// -----------------------------------------------------------------------------
@Composable
private fun TriageCheckItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    metric: String,
    metricColor: Color,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(metric, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = metricColor)
            }
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}
