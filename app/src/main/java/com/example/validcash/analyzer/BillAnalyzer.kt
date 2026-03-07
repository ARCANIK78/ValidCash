package com.example.validcash.analyzer

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class BillAnalyzer(
    private val onBanknoteDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    // Throttle: mínimo 150ms entre procesamientos (balance velocidad/precisión)
    private val minProcessInterval = 150L
    
    private var lastProcessTime = 0L

    // Método para liberar recursos cuando se destruya el analyzer
    fun close() {
        recognizer.close()
    }

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessTime < minProcessInterval) {
            imageProxy.close()
            return
        }
        
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            lastProcessTime = currentTime
            processImage(imageProxy)
        } else {
            imageProxy.close()
        }
    }
    
    private fun processImage(imageProxy: ImageProxy) {
        try {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        if (text.isNotBlank()) {
                            Log.d("BillAnalyzer", "Texto detectado: $text")
                            onBanknoteDetected(text)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("BillAnalyzer", "Text recognition failed", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        } catch (e: Exception) {
            Log.e("BillAnalyzer", "Error processing image: ${e.message}")
            imageProxy.close()
        }
    }
}

