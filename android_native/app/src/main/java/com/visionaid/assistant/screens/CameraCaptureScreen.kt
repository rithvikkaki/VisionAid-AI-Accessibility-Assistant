package com.visionaid.assistant.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.visionaid.assistant.utils.I18n
import com.visionaid.assistant.viewmodel.VisionAidViewModel
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraCaptureScreen(
    viewModel: VisionAidViewModel,
    onBack: () -> Unit,
    onCaptured: (File) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsState()
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isPerfectlyFramed by remember { mutableStateOf(false) }
    var isLowLight by remember { mutableStateOf(false) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // Camera Preview
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Optimization: Maximize Quality for better AI analysis
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(flashMode)
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val plane = imageProxy.planes[0]
                val buffer = plane.buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                val avgBrightness = data.map { it.toInt() and 0xFF }.average()
                
                val low = avgBrightness < 45.0
                val framed = avgBrightness in 55.0..225.0
                
                if (low != isLowLight) {
                    isLowLight = low
                    if (low) {
                        viewModel.speakHint(I18n.get(state.currentLang, "hint_low_light"))
                    }
                }
                
                if (framed != isPerfectlyFramed) {
                    isPerfectlyFramed = framed
                    if (framed) {
                        vibrateSuccess(vibrator)
                    }
                }
                imageProxy.close()
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalysis
                )
            } catch (exc: Exception) { }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(flashMode) {
        imageCapture?.flashMode = flashMode
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        val borderColor by animateColorAsState(
            targetValue = if (isPerfectlyFramed) Color.Green else if (isLowLight) Color.Yellow else Color.Red,
            animationSpec = tween(durationMillis = 400),
            label = "borderColor"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = borderColor.copy(alpha = 0.5f),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            
            if (isLowLight) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp)
                ) {
                    Text(
                        text = I18n.get(state.currentLang, "hint_low_light"),
                        color = Color.Yellow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            
            IconButton(onClick = {
                flashMode = when(flashMode) {
                    ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                    ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                    else -> ImageCapture.FLASH_MODE_AUTO
                }
                viewModel.speakHint("Flash mode changed")
            }) {
                val icon = when(flashMode) {
                    ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                    ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                    else -> Icons.Default.FlashOff
                }
                Icon(icon, contentDescription = "Flash", tint = Color.White)
            }
        }

        FloatingActionButton(
            onClick = {
                val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                
                imageCapture?.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            onCaptured(file)
                        }
                        override fun onError(exc: ImageCaptureException) { 
                            viewModel.speakHint("Capture failed")
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .size(88.dp),
            shape = CircleShape,
            containerColor = if (isPerfectlyFramed) Color.Green else Color.White
        ) {
            Icon(Icons.Default.Camera, contentDescription = "Capture", modifier = Modifier.size(44.dp))
        }
    }
}

private fun vibrateSuccess(vibrator: Vibrator) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(40)
    }
}
