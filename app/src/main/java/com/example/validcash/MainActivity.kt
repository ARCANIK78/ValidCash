package com.example.validcash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.validcash.ui.CameraScreen
import com.example.validcash.ui.theme.ValidCashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ValidCashTheme {
                val mainViewModel: MainViewModel = viewModel()
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
                    launcher.launch(Manifest.permission.CAMERA)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (hasCameraPermission) {
                        CameraScreen(
                            modifier = Modifier.padding(innerPadding),
                            banknoteData = mainViewModel.banknoteData,
                            onTextDetected = { mainViewModel.onTextDetected(it) }
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
            }
        }
    }
}
