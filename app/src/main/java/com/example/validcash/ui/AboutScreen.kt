
package com.example.validcash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Gracias por usar la app!",
            fontSize = 28.sp,
            fontFamily = AdlamDisplay,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF007A7A),
            textAlign = TextAlign.Center
        )

        CapiScanLogo(fontSize = 30.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Versión 1.0",
            fontSize = 16.sp,
            fontFamily = AdlamDisplay,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Image(
            painter = painterResource(id = R.drawable.capibaraabout),
            contentDescription = "Capibara About",
            modifier = Modifier
                .size(350.dp)
                .padding(16.dp)
        )
        

        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Software desarrollado por un equipo de 2 programadores.",
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontFamily = AdlamDisplay,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
