package com.danielsalas.danisalud.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraOcrScreen(
    onNavigateBack: () -> Unit,
    viewModel: HealthViewModel,
    language: String,
    mode: String // "bp" or "weight"
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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var editSys by remember { mutableStateOf("") }
    var editDia by remember { mutableStateOf("") }
    var editPulse by remember { mutableStateOf("") }
    var editWeight by remember { mutableStateOf("") }

    var showReviewDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var zoomRatio by remember { mutableStateOf(if (mode == "weight") 4.5f else 1.0f) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    val screenTitle = if (mode == "bp") Localization.get(language, "scan_ocr_bp") else Localization.get(language, "scan_ocr_weight")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(Localization.get(language, "back"))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isTorchOn = !isTorchOn
                        try {
                            cameraInstance?.cameraControl?.enableTorch(isTorchOn)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }) {
                        Text(if (isTorchOn) "🔦 On" else "🔦 Off")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            imageCapture = ImageCapture.Builder().build()
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                val cam = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                                cameraInstance = cam
                                cam.cameraControl.setZoomRatio(zoomRatio)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Central Guide Box
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val boxModifier = if (mode == "bp") {
                        Modifier.size(300.dp)
                    } else {
                        Modifier.fillMaxWidth().height(200.dp)
                    }

                    Card(
                        modifier = boxModifier,
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                    ) {}
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (statusMessage != null) {
                        Text(
                            text = statusMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val capture = imageCapture ?: return@Button
                            statusMessage = "Llegint pantalla..."
                            capture.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onError(exception: ImageCaptureException) {
                                        statusMessage = "Error en capturar"
                                    }

                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            @OptIn(ExperimentalGetImage::class)
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            val width = imageProxy.width
                                            val height = imageProxy.height

                                            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                            recognizer.process(image)
                                                .addOnSuccessListener { visionText ->
                                                    if (mode == "bp") {
                                                        val bp = parseBloodPressureSmart(visionText, width, height)
                                                        editSys = bp?.first?.toString() ?: ""
                                                        editDia = bp?.second?.toString() ?: ""
                                                        editPulse = bp?.third?.toString() ?: ""
                                                    } else {
                                                        val w = parseWeightSmart(visionText, width, height)
                                                        editWeight = w?.toString() ?: ""
                                                    }
                                                    statusMessage = "Captura realitzada!"
                                                    showReviewDialog = true
                                                }
                                                .addOnFailureListener {
                                                    statusMessage = "Error de lectura OCR"
                                                    showReviewDialog = true
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("📸 Capturar i Verificar", style = MaterialTheme.typography.titleMedium)
                    }

                    // Zoom Slider at the bottom
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Zoom: ${String.format(Locale.getDefault(), "%.1f", zoomRatio)}x", style = MaterialTheme.typography.labelSmall)
                            Slider(
                                value = zoomRatio,
                                onValueChange = { newZoom ->
                                    zoomRatio = newZoom
                                    try {
                                        cameraInstance?.cameraControl?.setZoomRatio(newZoom)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                valueRange = 1f..5f,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // Review & Edit Dialog
                if (showReviewDialog) {
                    AlertDialog(
                        onDismissRequest = { showReviewDialog = false },
                        title = { Text(if (mode == "bp") "Revisa la Tensió Capturada" else "Revisa el Pes Capturat") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Pots corregir qualsevol dígit si l'OCR no l'ha llegit bé del tot:")
                                if (mode == "bp") {
                                    OutlinedTextField(
                                        value = editSys,
                                        onValueChange = { editSys = it },
                                        label = { Text("Sistòlica (Alta) mmHg") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editDia,
                                        onValueChange = { editDia = it },
                                        label = { Text("Diastòlica (Baixa) mmHg") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editPulse,
                                        onValueChange = { editPulse = it },
                                        label = { Text("Pulsacions (bpm)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = editWeight,
                                        onValueChange = { editWeight = it },
                                        label = { Text("Pes (kg)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (mode == "bp") {
                                        val sys = editSys.replace(",", ".").toFloatOrNull()
                                        val dia = editDia.replace(",", ".").toFloatOrNull()
                                        val pul = editPulse.toIntOrNull() ?: 0
                                        if (sys != null && dia != null) {
                                            viewModel.insertBloodPressure(sys, dia, pul)
                                            showReviewDialog = false
                                            onNavigateBack()
                                        }
                                    } else {
                                        val w = editWeight.replace(",", ".").toFloatOrNull()
                                        if (w != null) {
                                            viewModel.insertWeight(w)
                                            showReviewDialog = false
                                            onNavigateBack()
                                        }
                                    }
                                }
                            ) {
                                Text("Desar Directament")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showReviewDialog = false }) {
                                Text("Tornar a fotografiar")
                            }
                        }
                    )
                }

            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(Localization.get(language, "camera_permission_needed"))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text(Localization.get(language, "grant_permission"))
                    }
                }
            }
        }
    }
}

fun cleanOcrText(text: String): String {
    return text.uppercase()
        .replace("O", "0")
        .replace("Q", "0")
        .replace("I", "1")
        .replace("L", "1")
        .replace("S", "5")
        .replace("Z", "2")
        .replace("B", "8")
        .replace("G", "6")
}

data class OcrNumber(val value: Float, val top: Int)

fun parseBloodPressureSmart(visionText: com.google.mlkit.vision.text.Text, imageWidth: Int, imageHeight: Int): Triple<Float, Float, Int>? {
    val items = mutableListOf<OcrNumber>()

    for (block in visionText.textBlocks) {
        for (line in block.lines) {
            val box = line.boundingBox
            if (box != null) {
                val cleaned = cleanOcrText(line.text)
                val regex = "\\d+([.,]\\d+)?".toRegex()
                regex.findAll(cleaned).forEach { match ->
                    val num = match.value.replace(",", ".").toFloatOrNull()
                    val top = box.top
                    if (num != null && num in 30.0f..300.0f) {
                        items.add(OcrNumber(num, top))
                    }
                }
            }
        }
    }

    if (items.isEmpty()) return null

    // Sort strictly by vertical top coordinate from top to bottom (smallest top = highest on screen)
    val sortedByTop = items.sortedBy { it.top }

    // Collect distinct numbers in top-to-bottom order
    val uniqueNumbers = mutableListOf<Float>()
    for (item in sortedByTop) {
        if (!uniqueNumbers.contains(item.value)) {
            uniqueNumbers.add(item.value)
        }
    }

    if (uniqueNumbers.size >= 2) {
        // Top value = Maximum (Systolic)
        val sys = uniqueNumbers.getOrNull(0) ?: 120.0f
        // Middle value = Minimum (Diastolic)
        val dia = uniqueNumbers.getOrNull(1) ?: 80.0f
        // Bottom value = Pulse
        val pul = uniqueNumbers.getOrNull(2)?.toInt() ?: 75

        return Triple(sys, dia, pwdRangeSafe(pul))
    }

    return null
}

fun pwdRangeSafe(p: Int): Int {
    return if (p in 30..200) p else 75
}

fun parseWeightSmart(visionText: com.google.mlkit.vision.text.Text, imageWidth: Int, imageHeight: Int): Float? {
    val fullText = visionText.textBlocks.joinToString(" ") { it.text }
    val cleaned = cleanOcrText(fullText)

    val decimalRegex = "\\d+([.,]\\d{1,2})".toRegex()
    val decimalMatch = decimalRegex.findAll(cleaned).map { 
        it.value.replace(",", ".").toFloatOrNull() 
    }.firstOrNull { it != null && it in 20.0f..300.0f }

    if (decimalMatch != null) return decimalMatch

    val integerRegex = "\\d{3,4}".toRegex()
    val intMatches = integerRegex.findAll(cleaned).map { it.value.toIntOrNull() }
        .filter { it != null && it in 3000..25000 }
        .toList()

    if (intMatches.isNotEmpty()) {
        val valInt = intMatches.first() ?: return null
        return valInt / 100.0f
    }

    val generalRegex = "\\d+([.,]\\d+)?".toRegex()
    val match = generalRegex.findAll(cleaned).map { 
        it.value.replace(",", ".").toFloatOrNull() 
    }.firstOrNull { it != null && it in 20.0f..300.0f }
    
    return match
}
