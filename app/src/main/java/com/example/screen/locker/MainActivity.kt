package com.example.screen.locker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.screen.locker.data.service.LockService
import com.example.screen.locker.domain.repository.LockRepository
import com.example.screen.locker.domain.repository.LockType
import com.example.screen.locker.presentation.screens.home.HomeScreen
import com.example.screen.locker.presentation.screens.permission.PermissionScreen
import com.example.screen.locker.presentation.screens.setup.PatternSetupScreen
import com.example.screen.locker.presentation.screens.setup.PinSetupScreen
import com.example.screen.locker.presentation.screens.wallpaper.WallpaperScreen
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val lockRepository: LockRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        observeLockState()

        setContent {
            val navController = rememberNavController()
            val hasPermission = Settings.canDrawOverlays(this)
            val startDest = if (hasPermission) "home" else "permission"

            NavHost(navController = navController, startDestination = startDest) {
                composable("permission") {
                    PermissionScreen(onAllPermissionsGranted = {
                        navController.navigate("home") {
                            popUpTo("permission") { inclusive = true }
                        }
                    })
                }
                composable("home") {
                    HomeScreen(
                        onSetupPin = { navController.navigate("setup_pin") },
                        onSetupPattern = { navController.navigate("setup_pattern") },
                        onSelectWallpaper = { navController.navigate("wallpaper") }
                    )
                }
                composable("setup_pin") {
                    PinSetupScreen(onBack = { navController.popBackStack() })
                }
                composable("setup_pattern") {
                    PatternSetupScreen(onBack = { navController.popBackStack() })
                }
                composable("wallpaper") {
                    WallpaperScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }

    private fun observeLockState() {
        val intent = Intent(this, LockService::class.java)
        lifecycleScope.launch {
            lockRepository.getLockType().collect { type ->
                if (type != LockType.NONE) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                } else {
                    stopService(intent)
                }
            }
        }
    }
}
