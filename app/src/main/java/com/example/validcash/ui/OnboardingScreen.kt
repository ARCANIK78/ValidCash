package com.example.validcash.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.validcash.ComponentPositions
import com.example.validcash.OnboardingCallbacks
import com.example.validcash.R
import com.example.validcash.ui.components.CapiScanLogo
import com.example.validcash.ui.theme.AdlamDisplay

enum class OnboardingStep {
    WELCOME,
    MENU,
    MENU_OPEN,
    HOME_MENU,
    ABOUT_MENU,
    SOUND,
    CAMERA,
    DONE
}

data class OnboardingStepInfo(
    val step: OnboardingStep,
    val title: String,
    val description: String
)

enum class HighlightType {
    NONE,
    MENU_BUTTON,
    MENU_DRAWER,
    HOME_MENU_ITEM,
    ABOUT_MENU_ITEM,
    SOUND_BUTTON,
    CAMERA_AREA
}

@Composable
fun OnboardingScreen(
    callbacks: OnboardingCallbacks,
    componentPositions: ComponentPositions,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val statusBarHeight = statusBarPadding.calculateTopPadding()
    val density = LocalDensity.current
    val context = LocalContext.current
    
    val steps = listOf(
        OnboardingStepInfo(
            OnboardingStep.WELCOME,
            "BIENVENIDOS A CAPISAN",
            "La app que te ayuda a verificar tus billetes de BOLIVIANOS (Bs). Toca COMENZAR para ver una guía rápida."
        ),
        OnboardingStepInfo(
            OnboardingStep.MENU,
            "☰ BOTÓN MENÚ",
            "Toca aquí para ABRIR el menú de navegación"
        ),
        OnboardingStepInfo(
            OnboardingStep.MENU_OPEN,
            "📋 MENÚ ABIERTO",
            "El menú se ha abierto. Aquí verás las opciones de navegación"
        ),
        OnboardingStepInfo(
            OnboardingStep.HOME_MENU,
            "🏠 HOME",
            "Toca aquí para volver a la pantalla principal"
        ),
        OnboardingStepInfo(
            OnboardingStep.ABOUT_MENU,
            "ℹ️ ABOUT",
            "Toca aquí para ver información sobre la app"
        ),
        OnboardingStepInfo(
            OnboardingStep.SOUND,
            "🔊 BOTÓN DE SONIDO",
            "Toca aquí para SILENCIAR o ACTIVAR los sonidos"
        ),
        OnboardingStepInfo(
            OnboardingStep.CAMERA,
            "📷 CÁMARA",
            "Apunta la cámara al billete. ¡Lee incluso con flash o poca luz!"
        ),
        OnboardingStepInfo(
            OnboardingStep.DONE,
            "¡LISTO!",
            "¡Ya puedes usar la app! Escanea tus billetes de Bolivianos."
        )
    )
    
    val currentStep = steps[currentStepIndex]
    val highlightType = when (currentStep.step) {
        OnboardingStep.WELCOME -> HighlightType.NONE
        OnboardingStep.MENU -> HighlightType.MENU_BUTTON
        OnboardingStep.MENU_OPEN -> HighlightType.MENU_DRAWER
        OnboardingStep.HOME_MENU -> HighlightType.HOME_MENU_ITEM
        OnboardingStep.ABOUT_MENU -> HighlightType.ABOUT_MENU_ITEM
        OnboardingStep.SOUND -> HighlightType.SOUND_BUTTON
        OnboardingStep.CAMERA -> HighlightType.CAMERA_AREA
        OnboardingStep.DONE -> HighlightType.NONE
    }
    
    // Manejar transiciones de pasos
    fun goToNextStep() {
        // El drawer se cierra cuando vamos del paso ABOUT_MENU al paso SOUND
        when (currentStep.step) {
            OnboardingStep.MENU -> {
                callbacks.openDrawer()
            }
            OnboardingStep.ABOUT_MENU -> {
                // Cerrar drawer cuando vamos al paso de sonido
                callbacks.closeDrawer()
            }
            else -> {}
        }
        
        if (currentStepIndex < steps.size - 1) {
            currentStepIndex++
        } else {
            callbacks.closeDrawer()
            onComplete()
        }
    }
    
    fun goToPreviousStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--
            
            // Al ir atrás desde SOUND, reabrir el drawer
            when (steps[currentStepIndex].step) {
                OnboardingStep.ABOUT_MENU, OnboardingStep.HOME_MENU, OnboardingStep.MENU_OPEN -> {
                    callbacks.openDrawer()
                }
                OnboardingStep.MENU -> {
                    callbacks.closeDrawer()
                }
                else -> {}
            }
        }
    }
    
    // Animación de pulso
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Calcular el rectángulo de highlight basado en las posiciones reales
    val highlightRect = remember(highlightType, componentPositions, screenSize) {
        when (highlightType) {
            HighlightType.MENU_BUTTON -> {
                componentPositions.menuButton.takeIf { it != Rect.Zero }
                    ?: run {
                        val buttonSize = with(density) { 48.dp.toPx() }
                        val statusBarHeightPx = with(density) { statusBarHeight.toPx() }
                        Rect(Offset(4f, statusBarHeightPx + 4f), Size(buttonSize, buttonSize))
                    }
            }
            HighlightType.MENU_DRAWER -> {
                if (screenSize.width > 0) {
                    val drawerWidth = if (componentPositions.drawerWidth > 0) {
                        componentPositions.drawerWidth
                    } else {
                        screenSize.width * 0.75f
                    }
                    val statusBarHeightPx = with(density) { statusBarHeight.toPx() }
                    Rect(
                        Offset(0f, statusBarHeightPx),
                        Size(drawerWidth, screenSize.height - statusBarHeightPx)
                    )
                } else Rect.Zero
            }
            HighlightType.HOME_MENU_ITEM -> {
                componentPositions.menuItemHome.takeIf { it != Rect.Zero }
                    ?: Rect.Zero
            }
            HighlightType.ABOUT_MENU_ITEM -> {
                componentPositions.menuItemAbout.takeIf { it != Rect.Zero }
                    ?: Rect.Zero
            }
            HighlightType.SOUND_BUTTON -> {
                componentPositions.soundButton.takeIf { it != Rect.Zero }
                    ?: run {
                        if (screenSize.width > 0) {
                            val buttonSize = with(density) { 48.dp.toPx() }
                            val statusBarHeightPx = with(density) { statusBarHeight.toPx() }
                            Rect(
                                Offset(screenSize.width - buttonSize - 4f, statusBarHeightPx + 4f),
                                Size(buttonSize, buttonSize)
                            )
                        } else Rect.Zero
                    }
            }
            HighlightType.CAMERA_AREA -> {
                componentPositions.cameraArea.takeIf { it != Rect.Zero }
                    ?: run {
                        if (screenSize.width > 0) {
                            val statusBarHeightPx = with(density) { statusBarHeight.toPx() }
                            val topBarHeight = componentPositions.topBarHeight
                            Rect(
                                Offset(screenSize.width * 0.05f, statusBarHeightPx + topBarHeight + 20f),
                                Size(screenSize.width * 0.9f, screenSize.height * 0.4f)
                            )
                        } else Rect.Zero
                    }
            }
            HighlightType.NONE -> Rect.Zero
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .onSizeChanged { screenSize = it }
    ) {
        // Mostrar overlay con spotlight
        if (highlightType != HighlightType.NONE && highlightRect != Rect.Zero && screenSize.width > 0) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        addRect(Rect(Offset.Zero, Size(screenSize.width.toFloat(), screenSize.height.toFloat())))
                    }
                    
                    val holePath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = highlightRect,
                                cornerRadius = CornerRadius(12.dp.toPx())
                            )
                        )
                    }
                    
                    val combinedPath = Path.combine(
                        operation = PathOperation.Difference,
                        path1 = path,
                        path2 = holePath
                    )
                    
                    drawPath(path = combinedPath, color = Color.Black.copy(alpha = 0.5f))
                    
                    drawRoundRect(
                        color = Color.White.copy(alpha = pulseAlpha),
                        topLeft = highlightRect.topLeft,
                        size = highlightRect.size,
                        cornerRadius = CornerRadius(12.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
                
                // Mostrar imagen de muestra solo en el paso CÁMARA
                if (currentStep.step == OnboardingStep.CAMERA && highlightRect != Rect.Zero) {
                    val density = LocalDensity.current
                    val imageWidth = with(density) { highlightRect.width.toDp() }
                    val imageHeight = with(density) { highlightRect.height.toDp() }
                    val imageOffsetX = with(density) { highlightRect.left.toDp() }
                    val imageOffsetY = with(density) { highlightRect.top.toDp() }
                    
                    Image(
                        painter = painterResource(id = R.drawable.muestra_de_escaneo_de_billetes),
                        contentDescription = "Muestra de escaneo de billetes",
                        modifier = Modifier
                            .offset(x = imageOffsetX + (imageWidth - imageWidth * 0.9f) / 2, y = imageOffsetY + (imageHeight - imageHeight * 0.9f) / 2)
                            .width(imageWidth * 0.9f)
                            .height(imageHeight * 0.9f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        
        // Welcome y Done - overlay completo
        if (highlightType == HighlightType.NONE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .pointerInput(currentStepIndex) {
                        detectTapGestures {
                            if (currentStepIndex < steps.size - 1) {
                                currentStepIndex++
                            } else {
                                onComplete()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color.White
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CapiScanLogo(fontSize = 32.sp)
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = currentStep.title,
                            fontSize = 24.sp,
                            fontFamily = AdlamDisplay,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color(0xFF44F2F2) else Color(0xFF007A7A),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = currentStep.description,
                            fontSize = 15.sp,
                            fontFamily = AdlamDisplay,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                if (currentStepIndex < steps.size - 1) {
                                    currentStepIndex++
                                } else {
                                    onComplete()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) Color(0xFF44F2F2) else Color(0xFF007A7A),
                                contentColor = if (isDarkTheme) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = if (currentStepIndex == 0) "COMENZAR" else "USAR APP",
                                fontFamily = AdlamDisplay,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                    }
                }
            }
        }
        
        // Tarjeta de información para pasos con highlight
        if (highlightType != HighlightType.NONE) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .widthIn(max = 280.dp)
                    .navigationBarsPadding(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentStep.title,
                        fontSize = 15.sp,
                        fontFamily = AdlamDisplay,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFF44F2F2) else Color(0xFF007A7A),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = currentStep.description,
                        fontSize = 12.sp,
                        fontFamily = AdlamDisplay,
                        color = if (isDarkTheme) Color.White else Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Atrás
                        if (currentStepIndex > 0) {
                            OutlinedButton(
                                onClick = { goToPreviousStep() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Atrás",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "ATRÁS",
                                    fontFamily = AdlamDisplay,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        
                        // Botón Siguiente
                        Button(
                            onClick = { goToNextStep() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) Color(0xFF44F2F2) else Color(0xFF007A7A),
                                contentColor = if (isDarkTheme) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                if (currentStepIndex == steps.size - 1) "USAR APP" else "SIGUIENTE",
                                fontFamily = AdlamDisplay,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (currentStepIndex == steps.size - 1) Icons.Default.Check else Icons.Default.ArrowForward,
                                contentDescription = "Siguiente",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

