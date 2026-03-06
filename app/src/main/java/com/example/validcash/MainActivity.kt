package com.example.validcash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.validcash.ui.CameraScreen
import com.example.validcash.ui.theme.AdlamDisplay
import com.example.validcash.ui.theme.ValidCashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ValidCashTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onDismiss = { showSplash = false })
                } else {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onDismiss: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val backgroundColor = if (isDarkTheme) Color(0xFF44F2F2) else Color(0xFF007A7A)
    val textColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFFFFFFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "BIENVENIDOS A MI APP",
                fontSize = 33.sp,
                fontFamily = AdlamDisplay,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "CAPI",
                    fontSize = 45.sp,
                    fontFamily = AdlamDisplay,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Color(0xFF007FFF), 
                            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                        )
                        .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                )
                Text(
                    text = "SCAN",
                    fontSize = 45.sp,
                    fontFamily = AdlamDisplay,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Color(0xFF301934),
                            shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                        )
                        .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.capibara),
                contentDescription = "Logo de la aplicación",
                modifier = Modifier.size(450.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Presione la pantalla para continuar",
                fontSize = 22.sp,
                fontFamily = AdlamDisplay,
                fontWeight = FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MainContent() {
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
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
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
