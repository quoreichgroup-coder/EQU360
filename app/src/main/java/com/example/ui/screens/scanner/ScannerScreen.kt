package com.example.ui.screens.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.repository.IEquipmentRepository
import com.example.data.scanner.QrCodeAnalyzer
import com.example.data.scanner.QrCodeParser
import com.example.data.scanner.QrParseResult
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    repository: IEquipmentRepository,
    onNavigateBack: () -> Unit,
    onNavigateToEquipment: (String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var permissionDeniedPermanently by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            permissionDeniedPermanently = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var analyzerRef by remember { mutableStateOf<QrCodeAnalyzer?>(null) }

    // Dialog state for not found equipment
    var notFoundDialogEquipmentId by remember { mutableStateOf<String?>(null) }
    var rawScanPayload by remember { mutableStateOf<String?>(null) }

    // Manual test QR quick trigger dialog
    var showManualInputModal by remember { mutableStateOf(false) }
    var manualInputCode by remember { mutableStateOf("") }

    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }

    fun handleRawCodeScanned(code: String) {
        val parseResult = QrCodeParser.parse(code)
        when (parseResult) {
            is QrParseResult.Success -> {
                triggerVibration()
                scope.launch {
                    val eq = withContext(Dispatchers.IO) {
                        repository.getEquipmentSync(parseResult.equipmentId)
                    }
                    if (eq != null) {
                        onNavigateToEquipment(eq.equipmentId)
                    } else {
                        rawScanPayload = code
                        notFoundDialogEquipmentId = parseResult.equipmentId
                    }
                }
            }
            is QrParseResult.Invalid -> {
                rawScanPayload = code
                notFoundDialogEquipmentId = code
            }
        }
    }

    // Reticle animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("scanner_screen")
    ) {
        // Camera Preview Layer
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val analyzer = QrCodeAnalyzer { detectedBarcode ->
                            handleRawCodeScanned(detectedBarcode)
                        }
                        analyzerRef = analyzer

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, analyzer)
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraInstance = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
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
            // Permission Denied or Unavailable View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate900)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.widthIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SafetyAmber,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Access Required",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The equipment barcode and QR scanner uses your camera to quickly identify plant assets. Please grant camera permission to scan tags.",
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant Permission", color = Slate900, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showManualInputModal = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Enter Code Manually", color = Color.White)
                        }
                    }
                }
            }
        }

        // Viewfinder Frame & Mask Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val frameDim = (canvasWidth * 0.72f).coerceAtMost(320.dp.toPx())
            val left = (canvasWidth - frameDim) / 2f
            val top = (canvasHeight - frameDim) / 2f - 40.dp.toPx()
            val right = left + frameDim
            val bottom = top + frameDim

            // Dark semi-transparent scrim around frame
            val scrimColor = Color(0x99000000)
            drawRect(color = scrimColor, size = Size(canvasWidth, top))
            drawRect(color = scrimColor, topLeft = Offset(0f, bottom), size = Size(canvasWidth, canvasHeight - bottom))
            drawRect(color = scrimColor, topLeft = Offset(0f, top), size = Size(left, frameDim))
            drawRect(color = scrimColor, topLeft = Offset(right, top), size = Size(canvasWidth - right, frameDim))

            // Reticle Border
            drawRoundRect(
                color = Color(0x66F59E0B),
                topLeft = Offset(left, top),
                size = Size(frameDim, frameDim),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Industrial Corner Brackets
            val cornerLen = 32.dp.toPx()
            val cornerStroke = 4.dp.toPx()
            val bracketColor = SafetyAmber

            // Top-Left
            drawLine(bracketColor, Offset(left - 2, top), Offset(left + cornerLen, top), cornerStroke, StrokeCap.Round)
            drawLine(bracketColor, Offset(left, top - 2), Offset(left, top + cornerLen), cornerStroke, StrokeCap.Round)

            // Top-Right
            drawLine(bracketColor, Offset(right + 2, top), Offset(right - cornerLen, top), cornerStroke, StrokeCap.Round)
            drawLine(bracketColor, Offset(right, top - 2), Offset(right, top + cornerLen), cornerStroke, StrokeCap.Round)

            // Bottom-Left
            drawLine(bracketColor, Offset(left - 2, bottom), Offset(left + cornerLen, bottom), cornerStroke, StrokeCap.Round)
            drawLine(bracketColor, Offset(left, bottom + 2), Offset(left, bottom - cornerLen), cornerStroke, StrokeCap.Round)

            // Bottom-Right
            drawLine(bracketColor, Offset(right + 2, bottom), Offset(right - cornerLen, bottom), cornerStroke, StrokeCap.Round)
            drawLine(bracketColor, Offset(right, bottom + 2), Offset(right, bottom - cornerLen), cornerStroke, StrokeCap.Round)

            // Animated Laser Line
            val currentLaserY = top + (frameDim * laserPosition)
            drawLine(
                color = SafetyAmber,
                start = Offset(left + 8.dp.toPx(), currentLaserY),
                end = Offset(right - 8.dp.toPx(), currentLaserY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Top Navigation Controls (Back, Title, Flashlight)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x99000000), CircleShape)
                    .testTag("scanner_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .background(Color(0x99000000), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = SafetyAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EQUIPMENT SCANNER",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            IconButton(
                onClick = {
                    val cam = cameraInstance
                    if (cam != null && cam.cameraInfo.hasFlashUnit()) {
                        isTorchOn = !isTorchOn
                        cam.cameraControl.enableTorch(isTorchOn)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isTorchOn) SafetyAmber else Color(0x99000000), CircleShape)
                    .testTag("scanner_flashlight_button")
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flashlight",
                    tint = if (isTorchOn) Slate900 else Color.White
                )
            }
        }

        // Bottom Controls, Instruction & Fast Demo Tags
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xCC0F172A))
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instruction
            Text(
                text = "Align the equipment QR code inside the frame",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Scans automatically when detected • Supports PlantCare, SAP EAM & serial tags",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Demo Scan Chips (Enables 1-tap testing in Android emulator or real world)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Scan Test:",
                    color = SafetyAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val demoTags = listOf(
                        "121SC008" to "PLANTCARE:EQUIPMENT:121SC008",
                        "CRU01" to "PLANTCARE:EQUIPMENT:CRU01",
                        "AF01" to "AF01",
                        "CV04" to "PLANTCARE:EQUIPMENT:CV04",
                        "CV05C" to "CV05C",
                        "CV06A" to "CV06A",
                        "Unknown (Test)" to "999INVALID_CODE"
                    )
                    items(demoTags) { (label, rawCode) ->
                        Box(
                            modifier = Modifier
                                .background(Slate800, RoundedCornerShape(16.dp))
                                .border(1.dp, SafetyAmber.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .clickable { handleRawCodeScanned(rawCode) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("quick_scan_$label")
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Action Row (Enter Manually & Search Directory)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showManualInputModal = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("manual_input_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Manual Code Input", fontSize = 12.sp)
                }

                Button(
                    onClick = onNavigateToSearch,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("search_directory_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SafetyAmber
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search Directory", fontSize = 12.sp, color = Color.White)
                }
            }
        }

        // Equipment Not Found Dialog
        if (notFoundDialogEquipmentId != null) {
            AlertDialog(
                onDismissRequest = {
                    notFoundDialogEquipmentId = null
                    analyzerRef?.resume()
                },
                containerColor = Slate900,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusRed,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Equipment not found",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "No equipment in the local database matches this scan identifier.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Equipment ID:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = notFoundDialogEquipmentId ?: "",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafetyAmber
                                )
                                if (rawScanPayload != null && rawScanPayload != notFoundDialogEquipmentId) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Raw Code: $rawScanPayload",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            notFoundDialogEquipmentId = null
                            onNavigateToSearch()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("search_manually_button")
                    ) {
                        Text("Search manually", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            notFoundDialogEquipmentId = null
                            analyzerRef?.resume()
                        },
                        modifier = Modifier.testTag("scan_again_button")
                    ) {
                        Text("Scan again", color = Color(0xFFCBD5E1))
                    }
                }
            )
        }

        // Manual Code Input Modal
        if (showManualInputModal) {
            AlertDialog(
                onDismissRequest = { showManualInputModal = false },
                containerColor = Slate900,
                title = {
                    Text(
                        text = "Enter Equipment Code",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Type an Equipment ID or raw QR string (e.g. 121SC008, PLANTCARE:EQUIPMENT:121SC008):",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualInputCode,
                            onValueChange = { manualInputCode = it },
                            placeholder = { Text("121SC008", color = Color(0xFF64748B)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SafetyAmber,
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manual_code_input_field")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val code = manualInputCode.trim()
                            showManualInputModal = false
                            manualInputCode = ""
                            if (code.isNotBlank()) {
                                handleRawCodeScanned(code)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_manual_code_button")
                    ) {
                        Text("Lookup", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualInputModal = false }) {
                        Text("Cancel", color = Color(0xFFCBD5E1))
                    }
                }
            )
        }
    }
}
