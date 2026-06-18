package com.example.screen.locker.presentation.screens.lock

import android.app.WallpaperManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.screen.locker.domain.repository.LockType
import com.example.screen.locker.presentation.components.PatternLockView
import com.example.screen.locker.presentation.components.ZipperUnlockView
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LockScreenOverlay(
    onUnlock: () -> Unit,
    viewModel: LockViewModel = koinViewModel()
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    val pinState by viewModel.pinState.collectAsState()
    val unlockStatus by viewModel.unlockStatus.collectAsState()
    val lockType by viewModel.lockType.collectAsState()
    val zipperStyle by viewModel.zipperStyle.collectAsState()
    val hookStyle by viewModel.hookStyle.collectAsState()
    val zipperUnlocked by viewModel.zipperUnlocked.collectAsState()
    val customWallpaper by viewModel.customWallpaper.collectAsState()
    var wallpaperBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Fetch system wallpaper as fallback
    val wallpaperManager = WallpaperManager.getInstance(context)
    val systemWallpaper: Drawable? = remember {
        try {
            // On some versions, getDrawable() might fail or require permissions
            // but usually it works for the current system wallpaper.
            wallpaperManager.drawable
        } catch (e: Exception) {
            null
        }
    }

    // Prepare bitmap for zipper effect
    LaunchedEffect(customWallpaper, systemWallpaper) {
        if (customWallpaper != null) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(customWallpaper)
                .allowHardware(false)
                .build()
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            wallpaperBitmap = result?.toBitmap(config = Bitmap.Config.ARGB_8888)?.asImageBitmap()
        } else {
            wallpaperBitmap = systemWallpaper?.toBitmap(config = Bitmap.Config.ARGB_8888)?.asImageBitmap()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance().time
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(now)
            kotlinx.coroutines.delay(1000)
        }
    }

    LaunchedEffect(unlockStatus) {
        if (unlockStatus == true) {
            onUnlock()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Wallpaper (Blurred behind zipper)
        val isZipperActive = (lockType == LockType.ZIPPER || lockType == LockType.ZIP_PIN || lockType == LockType.ZIP_PATTERN) && !zipperUnlocked
        val shouldBlur = pinState.isNotEmpty() || zipperUnlocked || isZipperActive

        if (wallpaperBitmap != null) {
            Image(
                bitmap = wallpaperBitmap!!,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(if (shouldBlur) 20.dp else 0.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        }

        // 2. Dim Layer
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (isZipperActive) 0.6f else 0.4f)))

        // 3. Zipper Layer
        if (isZipperActive) {
            ZipperUnlockView(
                onUnlock = { viewModel.onZipperComplete() },
                zipperStyle = zipperStyle,
                hookStyle = hookStyle,
                wallpaper = wallpaperBitmap,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 4. Content Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Time & Date
            Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentTime,
                    fontSize = 90.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = currentDate,
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Dynamic Unlock UI
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val showSecondStage = (lockType == LockType.ZIP_PIN || lockType == LockType.ZIP_PATTERN) && zipperUnlocked
                val showDirect = lockType == LockType.PIN || lockType == LockType.PATTERN
                
                if (showSecondStage || showDirect) {
                    val actualType = if (lockType == LockType.ZIP_PIN) LockType.PIN 
                                    else if (lockType == LockType.ZIP_PATTERN) LockType.PATTERN 
                                    else lockType
                    
                    when (actualType) {
                        LockType.PIN -> PinPadUI(pinState, viewModel)
                        LockType.PATTERN -> PatternLockView(
                            onPatternComplete = { viewModel.validatePattern(it) },
                            modifier = Modifier.size(300.dp)
                        )
                        else -> {}
                    }
                }
            }

            // Removed Emergency button as requested
        }
    }
}

@Composable
fun PinPadUI(pinState: String, viewModel: LockViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) { index ->
                val isFilled = index < pinState.length
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isFilled) Color.White else Color.White.copy(alpha = 0.3f))
                )
            }
        }

        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "C")
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            keys.chunked(3).forEach { rowKeys ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    rowKeys.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(76.dp))
                        } else {
                            KeyButton(
                                text = key,
                                onClick = {
                                    if (key == "C") viewModel.clearPin()
                                    else viewModel.onPinInput(key)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.15f),
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 32.sp, fontWeight = FontWeight.Normal)
        }
    }
}
