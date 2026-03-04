package com.example.validcash.ui

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.validcash.analyzer.BillAnalyzer
import com.example.validcash.model.BanknoteData
import com.example.validcash.ui.theme.ValidCashTheme
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

    CameraScreenContent(
        modifier = modifier,
        banknoteData = banknoteData,
        cameraPreview = {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

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

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )
        }
    )
}

@Composable
fun CameraScreenContent(
    modifier: Modifier = Modifier,
    banknoteData: BanknoteData,
    cameraPreview: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        cameraPreview()

        // Overlay que muestra los datos solo cuando están completos
        if (banknoteData.isValid) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Billete Detectado",
                        color = Color.Green,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Serie: ${banknoteData.serie}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Valor: ${banknoteData.valor} BOB",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Nro Serie: ${banknoteData.numeroSerie}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@ComposePreview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    ValidCashTheme {
        CameraScreenContent(
            banknoteData = BanknoteData(
                serie = "B",
                valor = "100",
                numeroSerie = "123456789"
            ),
            cameraPreview = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Vista previa de cámara", color = Color.White)
                }
            }
        )
    }
}
