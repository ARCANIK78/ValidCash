package com.example.validcash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.validcash.ui.AboutScreen
import com.example.validcash.ui.HomeContent
import com.example.validcash.ui.OnboardingScreen
import com.example.validcash.ui.OnboardingViewModel
import com.example.validcash.ui.SplashScreen
import com.example.validcash.ui.components.CapiScanLogo
import com.example.validcash.ui.theme.AdlamDisplay
import com.example.validcash.ui.theme.ValidCashTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        clearCache()
        
        enableEdgeToEdge()
        setContent {
            ValidCashTheme {
                var showSplash by remember { mutableStateOf(true) }
                var onboardingViewModel: OnboardingViewModel? by remember { mutableStateOf(null) }
                var showOnboarding by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    onboardingViewModel = OnboardingViewModel().also { vm ->
                        vm.init(applicationContext)
                        showOnboarding = !vm.isOnboardingCompleted()
                    }
                }

                if (showSplash) {
                    SplashScreen(onDismiss = { showSplash = false })
                } else {
                    MainContent(
                        onboardingViewModel = onboardingViewModel,
                        showOnboarding = showOnboarding,
                        onOnboardingComplete = {
                            onboardingViewModel?.completeOnboarding()
                            showOnboarding = false
                        }
                    )
                }
            }
        }
    }
    
    private fun clearCache() {
        try {
            val cacheDir = cacheDir
            deleteDir(cacheDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun deleteDir(dir: java.io.File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list() ?: return false
            for (i in children.indices) {
                val success = deleteDir(java.io.File(dir, children[i]))
                if (!success) return false
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        }
        return false
    }
}

enum class Screen { HOME, ABOUT }

data class ComponentPositions(
    val menuButton: Rect = Rect.Zero,
    val soundButton: Rect = Rect.Zero,
    val cameraArea: Rect = Rect.Zero,
    val menuItemHome: Rect = Rect.Zero,
    val menuItemAbout: Rect = Rect.Zero,
    val topBarHeight: Float = 0f,
    val drawerWidth: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    onboardingViewModel: OnboardingViewModel? = null,
    mainViewModel: MainViewModel = viewModel(),
    showOnboarding: Boolean = false,
    onOnboardingComplete: () -> Unit = {}
) {
    // Estado del drawer controlado POR el onboarding
    var isDrawerControlledByOnboarding by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    
    // Estado para posiciones de componentes
    var componentPositions by remember { mutableStateOf(ComponentPositions()) }
    
    // Callbacks para que el onboarding controle la UI
    val onboardingCallbacks = remember {
        OnboardingCallbacks(
            openDrawer = {
                isDrawerControlledByOnboarding = true
                scope.launch { drawerState.open() }
            },
            closeDrawer = {
                isDrawerControlledByOnboarding = false
                scope.launch { drawerState.close() }
            },
            toggleSound = {
                isSoundEnabled = onboardingViewModel?.toggleSound() ?: true
            }
        )
    }
    
    // Durante el onboarding, NO permitir que el usuario interactúe con los botones
    val isOnboardingActive = showOnboarding
    
    // Initialize sound state from preferences
    LaunchedEffect(onboardingViewModel) {
        onboardingViewModel?.let {
            isSoundEnabled = it.isSoundEnabled()
        }
    }
    
    // Cerrar drawer cuando termina el onboarding
    LaunchedEffect(showOnboarding) {
        if (!showOnboarding) {
            isDrawerControlledByOnboarding = false
            drawerState.close()
        }
    }
    
    // Reset screen to HOME when onboarding starts
    LaunchedEffect(showOnboarding) {
        if (showOnboarding) {
            currentScreen = Screen.HOME
        }
    }
    
    val isDarkTheme = isSystemInDarkTheme()
    val navBackgroundColor = if (isDarkTheme) Color(0xFF44F2F2) else Color(0xFF007A7A)
    val navTextColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFFFFFFF)

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = isOnboardingActive,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = navBackgroundColor,
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            componentPositions = componentPositions.copy(
                                drawerWidth = coordinates.size.width.toFloat()
                            )
                        }
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Home menu item
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInRoot()
                                val size = coordinates.size
                                componentPositions = componentPositions.copy(
                                    menuItemHome = Rect(
                                        left = position.x,
                                        top = position.y,
                                        right = position.x + size.width,
                                        bottom = position.y + size.height
                                    )
                                )
                            }
                    ) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Home", fontFamily = AdlamDisplay) },
                            selected = currentScreen == Screen.HOME,
                            onClick = {
                                if (!isOnboardingActive) {
                                    currentScreen = Screen.HOME
                                    scope.launch { drawerState.close() }
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = navTextColor,
                                unselectedContainerColor = Color.Transparent,
                                selectedIconColor = navBackgroundColor,
                                unselectedIconColor = navTextColor,
                                selectedTextColor = navBackgroundColor,
                                unselectedTextColor = navTextColor
                            ),
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    
                    // About menu item
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInRoot()
                                val size = coordinates.size
                                componentPositions = componentPositions.copy(
                                    menuItemAbout = Rect(
                                        left = position.x,
                                        top = position.y,
                                        right = position.x + size.width,
                                        bottom = position.y + size.height
                                    )
                                )
                            }
                    ) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Info, contentDescription = null) },
                            label = { Text("About", fontFamily = AdlamDisplay) },
                            selected = currentScreen == Screen.ABOUT,
                            onClick = {
                                if (!isOnboardingActive) {
                                    currentScreen = Screen.ABOUT
                                    scope.launch { drawerState.close() }
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = navTextColor,
                                unselectedContainerColor = Color.Transparent,
                                selectedIconColor = navBackgroundColor,
                                unselectedIconColor = navTextColor,
                                selectedTextColor = navBackgroundColor,
                                unselectedTextColor = navTextColor
                            ),
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            if (currentScreen == Screen.HOME) {
                                CapiScanLogo(fontSize = 22.sp)
                            } else {
                                Text("Acerca de", fontFamily = AdlamDisplay, color = navTextColor)
                            }
                        },
                        navigationIcon = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .onGloballyPositioned { coordinates ->
                                        val position = coordinates.positionInRoot()
                                        val size = coordinates.size
                                        componentPositions = componentPositions.copy(
                                            menuButton = Rect(
                                                left = position.x,
                                                top = position.y,
                                                right = position.x + size.width,
                                                bottom = position.y + size.height
                                            )
                                        )
                                    }
                            ) {
                                IconButton(
                                    onClick = { 
                                        if (!isOnboardingActive) {
                                            scope.launch { drawerState.open() }
                                        }
                                    },
                                    enabled = !isOnboardingActive
                                ) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = navTextColor)
                                }
                            }
                        },
                        actions = {
                            if (currentScreen == Screen.HOME) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .onGloballyPositioned { coordinates ->
                                            val position = coordinates.positionInRoot()
                                            val size = coordinates.size
                                            componentPositions = componentPositions.copy(
                                                soundButton = Rect(
                                                    left = position.x,
                                                    top = position.y,
                                                    right = position.x + size.width,
                                                    bottom = position.y + size.height
                                                )
                                            )
                                        }
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (!isOnboardingActive) {
                                                isSoundEnabled = onboardingViewModel?.toggleSound() ?: true
                                            }
                                        },
                                        enabled = !isOnboardingActive
                                    ) {
                                        Icon(
                                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                            contentDescription = if (isSoundEnabled) "Silenciar" else "Activar sonido",
                                            tint = navTextColor
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = navBackgroundColor,
                            titleContentColor = navTextColor,
                            navigationIconContentColor = navTextColor,
                            actionIconContentColor = navTextColor
                        ),
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            componentPositions = componentPositions.copy(
                                topBarHeight = coordinates.size.height.toFloat()
                            )
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .onGloballyPositioned { coordinates ->
                            if (currentScreen == Screen.HOME) {
                                val position = coordinates.positionInRoot()
                                val size = coordinates.size
                                componentPositions = componentPositions.copy(
                                    cameraArea = Rect(
                                        left = position.x,
                                        top = position.y,
                                        right = position.x + size.width,
                                        bottom = position.y + size.height
                                    )
                                )
                            }
                        }
                ) {
                    when (currentScreen) {
                        // DURANTE ONBOARDING: NO mostrar cámara, solo un placeholder
                        Screen.HOME -> {
                            if (isOnboardingActive) {
                                // Placeholder en lugar de cámara durante onboarding
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (isDarkTheme) Color(0xFF1A1A1A) else Color(0xFFE0E0E0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = if (isDarkTheme) Color(0xFF44F2F2) else Color(0xFF007A7A)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            } else {
                                HomeContent(mainViewModel, isSoundEnabled)
                            }
                        }
                        Screen.ABOUT -> AboutScreen()
                    }
                }
            }
        }
        
        // Overlay de Onboarding
        if (showOnboarding) {
            OnboardingScreen(
                callbacks = onboardingCallbacks,
                componentPositions = componentPositions,
                onComplete = onOnboardingComplete,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// Callbacks para que el onboarding controle la UI
data class OnboardingCallbacks(
    val openDrawer: () -> Unit = {},
    val closeDrawer: () -> Unit = {},
    val toggleSound: () -> Unit = {}
)

