package com.example.ui.screens.visualinspection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.EquipmentEntity
import com.example.data.model.VisualInspectionResult
import com.example.ui.components.StatusBadge
import com.example.ui.components.WorkCenterBadge
import com.example.ui.theme.IndustrialPrimary
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualInspectionScreen(
    viewModel: VisualInspectionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEquipment360: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(SafetyAmber, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Slate900,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI VISUAL INSPECTION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyAmber,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = uiState.equipment?.let { "${it.equipmentId} • ${it.name}" } ?: uiState.equipmentId,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("ai_inspection_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate900
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentStep) {
                VisualInspectionStep.CAPTURE_PHOTO -> {
                    CapturePhotoStep(
                        equipment = uiState.equipment,
                        onPhotoCaptured = { uri -> viewModel.onPhotoCaptured(uri) }
                    )
                }

                VisualInspectionStep.PHOTO_PREVIEW_AND_OBSERVATION -> {
                    PhotoPreviewAndObservationStep(
                        imageUri = uiState.capturedImageUri,
                        observation = uiState.technicianObservation,
                        isRecording = uiState.isRecordingVoice,
                        onRetake = { viewModel.onRetakePhoto() },
                        onObservationChanged = { viewModel.onObservationChanged(it) },
                        onToggleVoice = { viewModel.toggleVoiceRecording() },
                        onAnalyze = { viewModel.startAnalysis() }
                    )
                }

                VisualInspectionStep.ANALYZING -> {
                    AnalyzingStep(
                        progressMessage = uiState.analysisProgressMessage,
                        onCancel = { viewModel.cancelAnalysis() }
                    )
                }

                VisualInspectionStep.RESULT_SUMMARY -> {
                    ResultSummaryStep(
                        result = uiState.inspectionResult,
                        imageUri = uiState.capturedImageUri,
                        onProceed = { viewModel.proceedToConfirmNotification() },
                        onEdit = { viewModel.proceedToConfirmNotification() },
                        onRetake = { viewModel.onRetakePhoto() }
                    )
                }

                VisualInspectionStep.CONFIRM_NOTIFICATION -> {
                    ConfirmNotificationStep(
                        equipment = uiState.equipment,
                        title = uiState.draftTitle,
                        priority = uiState.draftPriority,
                        observation = uiState.draftObservation,
                        damage = uiState.draftDamage,
                        cause = uiState.draftCause,
                        checks = uiState.draftChecks,
                        imageUri = uiState.capturedImageUri,
                        onTitleChange = { viewModel.updateDraftTitle(it) },
                        onPriorityChange = { viewModel.updateDraftPriority(it) },
                        onObservationChange = { viewModel.updateDraftObservation(it) },
                        onDamageChange = { viewModel.updateDraftDamage(it) },
                        onCauseChange = { viewModel.updateDraftCause(it) },
                        onConfirm = {
                            triggerVibration()
                            viewModel.confirmAndCreateNotification {
                                onNavigateToEquipment360(uiState.equipmentId)
                            }
                        }
                    )
                }

                VisualInspectionStep.SUCCESS -> {
                    SuccessConfirmationStep(
                        equipment = uiState.equipment,
                        notificationNumber = uiState.createdNotificationNumber ?: "NTF-000128",
                        onViewInEquipment360 = { onNavigateToEquipment360(uiState.equipmentId) }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 1: CAPTURE PHOTO
// ---------------------------------------------------------------------------
@Composable
fun CapturePhotoStep(
    equipment: EquipmentEntity?,
    onPhotoCaptured: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoCaptured(uri.toString())
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera View or Fallback
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraInstance = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate900),
                contentAlignment = Alignment.Center
            ) {
                Text("Camera permission requested...", color = Color.White)
            }
        }

        // Top Inherited Equipment Context Banner
        if (equipment != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = equipment.equipmentId,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyAmber
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  ${equipment.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = equipment.functionalLocation,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F766E), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Equipment identified ✓",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Top Flashlight Button
        IconButton(
            onClick = {
                val cam = cameraInstance
                if (cam != null && cam.cameraInfo.hasFlashUnit()) {
                    isTorchOn = !isTorchOn
                    cam.cameraControl.enableTorch(isTorchOn)
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .size(48.dp)
                .background(if (isTorchOn) SafetyAmber else Slate900.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(
                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Flashlight",
                tint = if (isTorchOn) Slate900 else Color.White
            )
        }

        // Bottom Controls: Shutter, Gallery, Quick Demo Samples
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Slate900.copy(alpha = 0.90f))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick Sample Photo Test Chips (Permits immediate 1-tap testing on web emulator)
            Text(
                text = "Quick Sample Photo (Field Test):",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val sampleImages = listOf(
                    "Bearing Flaking" to "content://plantcare/samples/bearing_spall.jpg",
                    "Belt Tracking Drift" to "content://plantcare/samples/belt_frayed.jpg",
                    "Chute Liner Crack" to "content://plantcare/samples/weld_crack.jpg",
                    "Oil Seal Leak" to "content://plantcare/samples/oil_leak.jpg"
                )
                items(sampleImages) { (label, uri) ->
                    Box(
                        modifier = Modifier
                            .background(Slate800, RoundedCornerShape(14.dp))
                            .border(1.dp, SafetyAmber.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable { onPhotoCaptured(uri) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("sample_photo_$label")
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Shutter Row (One-handed lower thumb area)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add from Gallery
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .background(Slate800, CircleShape)
                        .testTag("gallery_picker_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.White
                    )
                }

                // Primary Shutter Action (56dp+ prominent)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(SafetyAmber, CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .clickable {
                            onPhotoCaptured("camera://captured/photo_${System.currentTimeMillis()}.jpg")
                        }
                        .testTag("take_photo_shutter_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .background(SafetyAmber, CircleShape)
                            .border(2.dp, Slate900, CircleShape)
                    )
                }

                // Placeholder for symmetry / balance
                Box(modifier = Modifier.size(52.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "TAKE PHOTO",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 2: PHOTO PREVIEW & OPTIONAL OBSERVATION
// ---------------------------------------------------------------------------
@Composable
fun PhotoPreviewAndObservationStep(
    imageUri: String?,
    observation: String,
    isRecording: Boolean,
    onRetake: () -> Unit,
    onObservationChanged: (String) -> Unit,
    onToggleVoice: () -> Unit,
    onAnalyze: () -> Unit
) {
    var showTextInput by remember { mutableStateOf(observation.isNotBlank()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Photo Card Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = SafetyAmber,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Inspection Photo Captured", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        imageUri ?: "camera://captured/photo.jpg",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Photo Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("retake_photo_btn"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1))
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retake Photo")
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(Color(0xFF064E3B), RoundedCornerShape(8.dp))
                    .border(1.dp, StatusGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Photo Ready", color = StatusGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Optional Technician Observation Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OBSERVATION (OPTIONAL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Voice or Text",
                        fontSize = 11.sp,
                        color = SafetyAmber
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Voice Recording Button (One-Tap Speech-to-Text)
                Button(
                    onClick = onToggleVoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("voice_observe_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) StatusRed else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isRecording) StatusRed else SafetyAmber)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isRecording) Color.White else SafetyAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "Recording... (Listening)" else "🎤 Describe what you noticed (Voice)",
                        color = if (isRecording) Color.White else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (observation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Transcribed Observation:",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = observation,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle for text keyboard input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTextInput = !showTextInput }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showTextInput) "Hide text input" else "Add text instead",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AnimatedVisibility(visible = showTextInput) {
                    Column {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = observation,
                            onValueChange = onObservationChanged,
                            placeholder = { Text("E.g. Abnormal vibration around head pulley...", color = Color(0xFF64748B), fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("observation_text_input"),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SafetyAmber,
                                unfocusedBorderColor = Slate700,
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Prominent Lower Thumb Action Button
        Button(
            onClick = onAnalyze,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_ai_analysis_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Slate900,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Analyze with PlantCare AI",
                color = Slate900,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 3: ANALYZING LOADING SCREEN
// ---------------------------------------------------------------------------
@Composable
fun AnalyzingStep(
    progressMessage: String,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color(0xFF1E1B4B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(46.dp),
                        color = SafetyAmber,
                        strokeWidth = 3.5.dp
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SafetyAmber,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "AI Visual Analysis",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = progressMessage,
                    fontSize = 13.sp,
                    color = Color(0xFFA5B4FC),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Cross-referencing equipment wear patterns, open WOs & downtime logs...",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Cancel", color = Color(0xFFCBD5E1))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 4: RESULT SCREEN (FITS ON ONE SCREEN)
// ---------------------------------------------------------------------------
@Composable
fun ResultSummaryStep(
    result: VisualInspectionResult?,
    imageUri: String?,
    onProceed: () -> Unit,
    onEdit: () -> Unit,
    onRetake: () -> Unit
) {
    if (result == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Result Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI INSPECTION RESULT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                if (result.confidence == "High") Color(0xFF064E3B) else Color(0xFF78350F),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Confidence: ${result.confidence}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (result.confidence == "High") StatusGreen else SafetyAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Possible issue",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = result.possibleIssue,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "AI Observation",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = result.observations,
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )

                if (result.safetyWarning != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF451A03), RoundedCornerShape(6.dp))
                            .border(1.dp, StatusOrange.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = result.safetyWarning,
                            fontSize = 11.sp,
                            color = Color(0xFFFED7AA),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Recommended Checks Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "RECOMMENDED TECHNICIAN CHECKS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                result.recommendedChecks.forEach { check ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SafetyAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = check,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Primary Field Action Button (56dp target)
        Button(
            onClick = onProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("ai_result_create_notif_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Slate900,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Looks Correct • Create Notification",
                color = Slate900,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Secondary Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("ai_result_edit_btn"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1))
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Findings", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("ai_result_retake_btn"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1))
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retake Photo", fontSize = 12.sp)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 5: CONFIRM NOTIFICATION (HUMAN VALIDATION)
// ---------------------------------------------------------------------------
@Composable
fun ConfirmNotificationStep(
    equipment: EquipmentEntity?,
    title: String,
    priority: String,
    observation: String,
    damage: String,
    cause: String,
    checks: List<String>,
    imageUri: String?,
    onTitleChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onObservationChange: (String) -> Unit,
    onDamageChange: (String) -> Unit,
    onCauseChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val priorities = listOf("LOW", "MEDIUM", "HIGH", "VERY HIGH")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Locked Inherited Equipment Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NEW MAINTENANCE NOTIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyAmber,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = equipment?.let { "${it.equipmentId} — ${it.name}" } ?: "121SC008",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = equipment?.functionalLocation ?: "BI-PLN-CRU/CRS-003",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0F766E), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("AI PROPOSED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Editable Short Description
        Text("Short Description *", fontSize = 12.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("confirm_notif_title_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SafetyAmber,
                unfocusedBorderColor = Slate700,
                focusedContainerColor = Slate800,
                unfocusedContainerColor = Slate800
            ),
            shape = RoundedCornerShape(8.dp)
        )

        // Priority Selector
        Text("Priority", fontSize = 12.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            priorities.forEach { p ->
                val isSelected = priority == p
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(if (isSelected) SafetyAmber else Slate800, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSelected) SafetyAmber else Slate700, RoundedCornerShape(8.dp))
                        .clickable { onPriorityChange(p) }
                        .testTag("priority_select_$p"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = p,
                        color = if (isSelected) Slate900 else Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Damage & Cause
        Text("Damage Observed", fontSize = 12.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = damage,
            onValueChange = onDamageChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SafetyAmber,
                unfocusedBorderColor = Slate700,
                focusedContainerColor = Slate800,
                unfocusedContainerColor = Slate800
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Text("Probable Cause", fontSize = 12.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = cause,
            onValueChange = onCauseChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SafetyAmber,
                unfocusedBorderColor = Slate700,
                focusedContainerColor = Slate800,
                unfocusedContainerColor = Slate800
            ),
            shape = RoundedCornerShape(8.dp)
        )

        // Detailed Observation Notes
        Text("Detailed Observations & Checks", fontSize = 12.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = observation,
            onValueChange = onObservationChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SafetyAmber,
                unfocusedBorderColor = Slate700,
                focusedContainerColor = Slate800,
                unfocusedContainerColor = Slate800
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Action Button (56dp target)
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("confirm_create_notification_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Slate900,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONFIRM & CREATE NOTIFICATION",
                color = Slate900,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 6: SUCCESS CONFIRMATION
// ---------------------------------------------------------------------------
@Composable
fun SuccessConfirmationStep(
    equipment: EquipmentEntity?,
    notificationNumber: String,
    onViewInEquipment360: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("ai_inspection_success_card"),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusGreen)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF064E3B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Notification Created",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Equipment: ${equipment?.equipmentId ?: "121SC008"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SafetyAmber
                )

                Text(
                    text = "Notification: $notificationNumber",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "AI inspection findings, recommended checks, and photos were successfully logged to the local database.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onViewInEquipment360,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("success_view_in_360_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("View in Equipment 360°", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
