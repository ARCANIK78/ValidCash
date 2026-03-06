package com.example.validcash.ui

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.validcash.analyzer.BillAnalyzer
import com.example.validcash.model.BanknoteData
import com.example.validcash.ui.theme.ValidCashTheme
import com.example.validcash.validator.BanknoteValidator
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    banknoteData: BanknoteData,
    onTextDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    1
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var camera: Camera? by remember { mutableStateOf(null) }

    CameraScreenContent(
        modifier = modifier,
        banknoteData = banknoteData,
        flashEnabled = flashEnabled,
        onFlashClick = { 
            flashEnabled = !flashEnabled
            camera?.cameraControl?.enableTorch(flashEnabled)
        },
        cameraPreview = {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx)
                },
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, BillAnalyzer(onTextDetected))
                            }

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            camera?.cameraControl?.enableTorch(flashEnabled)
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            )
        }
    )
}

@Composable
fun CameraScreenContent(
    modifier: Modifier = Modifier,
    banknoteData: BanknoteData,
    flashEnabled: Boolean = false,
    onFlashClick: () -> Unit = {},
    cameraPreview: @Composable () -> Unit
) {
    val isGenuine = BanknoteValidator.isGenuine(banknoteData)
    
    Box(modifier = modifier.fillMaxSize()) {
        cameraPreview()

        // Botones de control
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onFlashClick,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash",
                    tint = Color.White
                )
            }
        }

        // Overlay que muestra los datos solo cuando están completos
        if (banknoteData.isValid) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        if (isGenuine) Color.Black.copy(alpha = 0.7f) 
                        else Color.Red.copy(alpha = 0.8f)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isGenuine) "BILLETE VALIDO" else "¡BILLETE NO VÁLIDO!",
                        color = if (isGenuine) Color.Green else Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (!isGenuine) {
                        Text(
                            text = "Rango de Serie B no permitido",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("Serie: ${banknoteData.serie}", color = Color.White)
                            Text("Valor: Bs${banknoteData.valor}", color = Color.White)
                        }
                        Column {
                            Text("Nro: ${banknoteData.numeroSerie}", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@ComposePreview(showBackground = true)
@Composable
fun CameraScreenPreviewValid() {
    ValidCashTheme {
        CameraScreenContent(
            banknoteData = BanknoteData(
                serie = "A",
                valor = "100",
                numeroSerie = "12345678"
            ),
            cameraPreview = { Box(Modifier.fillMaxSize().background(Color.Gray)) }
        )
    }
}
