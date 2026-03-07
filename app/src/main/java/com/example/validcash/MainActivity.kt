
package com.example.validcash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.validcash.ui.AboutScreen
import com.example.validcash.ui.HomeContent
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

                if (showSplash) {
                    SplashScreen(onDismiss = { showSplash = false })
                } else {
                    MainContent()
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
                if (!success) {
                    return false
                }
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        } else {
            return false
        }
    }
}

enum class Screen {
    HOME, ABOUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    mainViewModel: MainViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    
    val isDarkTheme = isSystemInDarkTheme()
    val navBackgroundColor = if (isDarkTheme) Color(0xFF44F2F2) else Color(0xFF007A7A)
    val navTextColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFFFFFFF)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = navBackgroundColor
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            Icons.Default.Home, 
                            contentDescription = null
                        ) 
                    },
                    label = { 
                        Text(
                            "Home", 
                            fontFamily = AdlamDisplay
                        ) 
                    },
                    selected = currentScreen == Screen.HOME,
                    onClick = {
                        currentScreen = Screen.HOME
                        scope.launch { drawerState.close() }
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
                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = null
                        ) 
                    },
                    label = { 
                        Text(
                            "About", 
                            fontFamily = AdlamDisplay
                        ) 
                    },
                    selected = currentScreen == Screen.ABOUT,
                    onClick = {
                        currentScreen = Screen.ABOUT
                        scope.launch { drawerState.close() }
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
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        if (currentScreen == Screen.HOME) {
                            CapiScanLogo(fontSize = 22.sp)
                        } else {
                            Text(
                                "Acerca de",
                                fontFamily = AdlamDisplay,
                                color = navTextColor
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu, 
                                contentDescription = "Menu",
                                tint = navTextColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = navBackgroundColor,
                        titleContentColor = navTextColor,
                        navigationIconContentColor = navTextColor
                    )
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    Screen.HOME -> HomeContent(mainViewModel)
                    Screen.ABOUT -> AboutScreen()
                }
            }
        }
    }
}
