
package com.example.validcash.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.validcash.MainViewModel

@Composable
fun HomeContent(
    mainViewModel: MainViewModel,
    isSoundEnabled: Boolean = true
) {
    val context = LocalContext.current
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
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    val banknoteData = remember(mainViewModel.banknoteData) {
        mainViewModel.banknoteData
    }

    if (hasCameraPermission) {
        CameraScreen(
            modifier = Modifier.fillMaxSize(),
            banknoteData = banknoteData,
            isSoundEnabled = isSoundEnabled,
            onTextDetected = { text, ctx -> mainViewModel.onTextDetected(text, ctx) }
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Se necesita permiso de cámara")
        }
    }
}
