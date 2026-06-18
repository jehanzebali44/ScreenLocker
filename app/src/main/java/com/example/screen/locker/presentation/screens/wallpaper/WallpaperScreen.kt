package com.example.screen.locker.presentation.screens.wallpaper

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperScreen(
    onBack: () -> Unit,
    viewModel: WallpaperViewModel = koinViewModel()
) {
    val wallpapers by viewModel.wallpapers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showApplyDialog by remember { mutableStateOf(false) }
    var selectedWallpaperUrl by remember { mutableStateOf<String?>(null) }
    var isApplying by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                CenterAlignedTopAppBar(
                    title = { Text("Wallpapers", fontWeight = FontWeight.Bold, color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text("Back", color = Color.Cyan) }
                    }
                )

                if (availableCategories.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableCategories) { category ->
                            val isSelected = category == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color.Cyan else Color.White.copy(alpha = 0.1f))
                                    .clickable { viewModel.onCategorySelected(category) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Cyan)
                    }
                } else if (wallpapers.isEmpty() && availableCategories.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No online wallpapers found", color = Color.White.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.setCustomWallpaper(null); onBack() }) {
                                Text("Use System Default")
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .aspectRatio(0.6f)
                                    .clickable { viewModel.setCustomWallpaper(null); onBack() },
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("System Default", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        items(wallpapers) { photo ->
                            Card(
                                modifier = Modifier
                                    .aspectRatio(0.6f)
                                    .clickable { 
                                        selectedWallpaperUrl = photo.src.portrait
                                        showApplyDialog = true
                                    }
                            ) {
                                AsyncImage(
                                    model = photo.src.portrait,
                                    contentDescription = photo.alt,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            if (isApplying) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.Cyan)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Applying Wallpaper...", color = Color.White)
                    }
                }
            }
        }
    }

    if (showApplyDialog && selectedWallpaperUrl != null) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Apply Wallpaper") },
            text = { Text("Where would you like to apply this wallpaper?") },
            confirmButton = {},
            dismissButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showApplyDialog = false
                            scope.launch {
                                isApplying = true
                                applyWallpaper(context, selectedWallpaperUrl!!, home = true, lock = false)
                                viewModel.setCustomWallpaper(selectedWallpaperUrl)
                                isApplying = false
                                onBack()
                            }
                        }
                    ) { Text("Home Screen") }
                    
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showApplyDialog = false
                            scope.launch {
                                isApplying = true
                                applyWallpaper(context, selectedWallpaperUrl!!, home = false, lock = true)
                                viewModel.setCustomWallpaper(selectedWallpaperUrl)
                                isApplying = false
                                onBack()
                            }
                        }
                    ) { Text("Lock Screen") }
                    
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showApplyDialog = false
                            scope.launch {
                                isApplying = true
                                applyWallpaper(context, selectedWallpaperUrl!!, home = true, lock = true)
                                viewModel.setCustomWallpaper(selectedWallpaperUrl)
                                isApplying = false
                                onBack()
                            }
                        }
                    ) { Text("Both Screens") }
                    
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showApplyDialog = false }
                    ) { Text("Cancel") }
                }
            }
        )
    }
}

suspend fun applyWallpaper(context: android.content.Context, url: String, home: Boolean, lock: Boolean) {
    withContext(Dispatchers.IO) {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // Required for getting bitmap
            .build()
        
        val result = (loader.execute(request) as? SuccessResult)?.drawable
        val bitmap = result?.toBitmap()
        
        if (bitmap != null) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    val which = (if (home) WallpaperManager.FLAG_SYSTEM else 0) or
                                (if (lock) WallpaperManager.FLAG_LOCK else 0)
                    if (which != 0) {
                        wallpaperManager.setBitmap(bitmap, null, true, which)
                    }
                } else {
                    if (home || lock) {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
