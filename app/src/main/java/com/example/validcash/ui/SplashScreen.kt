
package com.example.validcash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.validcash.R
import com.example.validcash.ui.components.CapiScanLogo
import com.example.validcash.ui.theme.AdlamDisplay

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
            
            CapiScanLogo(fontSize = 45.sp)

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
