package com.example.validcash.analyzer

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.validcash.parser.BanknoteParser
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class BillAnalyzer(
    private val onBanknoteDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    onBanknoteDetected(visionText.text)
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
    }
}
