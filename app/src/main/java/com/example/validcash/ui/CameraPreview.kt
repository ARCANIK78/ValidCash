package com.example.validcash.ui

import android.graphics.ImageFormat
import android.util.Log
import android.util.Size
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.validcash.analyzer.BillAnalyzer
import com.example.validcash.model.BanknoteData
import com.example.validcash.ui.theme.ValidCashTheme
import com.example.validcash.utils.SoundManager
import com.example.validcash.validator.BanknoteValidator
import java.util.concurrent.Executors

// Resolución máxima limitada a 1080p para mejor rendimiento
private val MAX_RESOLUTION = Size(1920, 1080)

private fun getOptimalResolution(context: android.content.Context): Size {
    return try {
        val cameraManager = context.getSystemService(android.content.Context.CAMERA_SERVICE) as CameraManager

        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        } ?: return MAX_RESOLUTION
        
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val streamConfigMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        
        val outputSizes = streamConfigMap?.getOutputSizes(ImageFormat.JPEG)
        
        if (outputSizes != null && outputSizes.isNotEmpty()) {
            // Filtrar tamaños mayores a 1080p y seleccionar el más cercano
            val filteredSizes = outputSizes.filter { 
                it.width <= MAX_RESOLUTION.width && it.height <= MAX_RESOLUTION.height 
            }
            
            val selectedSize = if (filteredSizes.isNotEmpty()) {
                // Seleccionar el más grande pero dentro del límite
                filteredSizes.sortedByDescending { it.width * it.height }.first()
            } else {
                // Si ninguno es menor a 1080p, buscar el más cercano por abajo
                outputSizes.sortedByDescending { it.width * it.height }.last()
            }
            
            Log.d("CameraPreview", "Resolución seleccionada: ${selectedSize.width}x${selectedSize.height}")
            selectedSize
        } else {
            MAX_RESOLUTION
        }
    } catch (e: Exception) {
        Log.e("CameraPreview", "Error al obtener resolución: ${e.message}")
        MAX_RESOLUTION
    }
}

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    banknoteData: BanknoteData,
    isSoundEnabled: Boolean = true,
    onTextDetected: (String, android.content.Context) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Referencia al analyzer para liberar recursos después
    val billAnalyzer = remember { BillAnalyzer { text -> onTextDetected(text, context) } }

    var flashEnabled by remember { mutableStateOf(false) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var camera: Camera? by remember { mutableStateOf(null) }

    val optimalResolution = remember { getOptimalResolution(context) }

    // Liberar recursos cuando el composable se destruya
    DisposableEffect(Unit) {
        onDispose {
            billAnalyzer.close()
            cameraExecutor.shutdown()
        }
    }

    CameraScreenContent(
        modifier = modifier,
        banknoteData = banknoteData,
        isSoundEnabled = isSoundEnabled,
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
                            .setTargetResolution(optimalResolution)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, billAnalyzer)
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
    isSoundEnabled: Boolean = true,
    flashEnabled: Boolean = false,
    onFlashClick: () -> Unit = {},
    cameraPreview: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // SoundManager para reproducir sonidos - solo se usa si el sonido está habilitado
    val soundManager = remember(isSoundEnabled) { 
        if (isSoundEnabled) SoundManager(context) else null
    }
    
    // Estado para controlar que el sonido solo se reproduzca una vez por detección
    var lastPlayedBanknote by remember { mutableStateOf("") }
    
    // Validar el billete
    val isGenuine = if (banknoteData.isValidatable) {
        BanknoteValidator.isGenuine(banknoteData)
    } else {
        true // Para billete no validable (100/200), mostrar como válido por defecto
    }
    
    // Reproducir sonido cuando cambia el billete detectado
    LaunchedEffect(banknoteData.numeroSerie, banknoteData.valor) {
        // Solo reproducir sonido si está habilitado
        if (isSoundEnabled && soundManager != null && banknoteData.isValid && banknoteData.numeroSerie != lastPlayedBanknote) {
            lastPlayedBanknote = banknoteData.numeroSerie
            
            if (banknoteData.isValidatable) {
                // Billetes con validación (10, 20, 50)
                if (BanknoteValidator.isGenuine(banknoteData)) {
                    soundManager.playSuccessSound()
                } else {
                    soundManager.playErrorSound()
                }
            } else {
                // Billetes sin validación disponible (100, 200) - reproducir sonido de éxito
                soundManager.playSuccessSound()
            }
        }
    }
    
    // Liberar recursos del SoundManager
    DisposableEffect(Unit) {
        onDispose {
            soundManager?.release()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        cameraPreview()

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onFlashClick,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash",
                    tint = Color.White
                )
            }
        }

        if (banknoteData.isValid) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        when {
                            !banknoteData.isValidatable -> Color.Blue.copy(alpha = 0.7f) // Azul para 100/200
                            isGenuine -> Color.Black.copy(alpha = 0.7f)
                            else -> Color.Red.copy(alpha = 0.8f)
                        }
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            !banknoteData.isValidatable -> "BILLETE SIN REPORTE"
                            isGenuine -> "BILLETE VALIDO"
                            else -> "¡BILLETE NO VÁLIDO!"
                        },
                        color = when {
                            !banknoteData.isValidatable -> Color.Cyan
                            isGenuine -> Color.Green
                            else -> Color.White
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (!banknoteData.isValidatable) {
                        Text(
                            text = "Billete de Bs${banknoteData.valor} en circulación normal",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    if (banknoteData.isValidatable && !isGenuine) {
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

