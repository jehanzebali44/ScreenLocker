package com.example.screen.locker.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screen.locker.domain.repository.HookStyle
import com.example.screen.locker.domain.repository.LockType
import com.example.screen.locker.domain.repository.ZipperStyle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSetupPin: () -> Unit,
    onSetupPattern: () -> Unit,
    onSelectWallpaper: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val lockType by viewModel.lockType.collectAsState()
    val zipperStyle by viewModel.zipperStyle.collectAsState()
    val hookStyle by viewModel.hookStyle.collectAsState()

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
                    title = {
                        Text(
                            "Security Settings",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        LockStatusCard(lockType)
                    }

                    item { SectionHeader("Basic Protection") }

                    item {
                        SelectionCard(
                            title = "None",
                            description = "No custom lock screen",
                            icon = Icons.Default.Lock,
                            isSelected = lockType == LockType.NONE,
                            onClick = { viewModel.disableLock() }
                        )
                    }

                    item {
                        SelectionCard(
                            title = if (lockType == LockType.PIN) "Change PIN" else "PIN Code",
                            description = "Standard 4-digit security",
                            icon = Icons.Default.Face,
                            isSelected = lockType == LockType.PIN,
                            onClick = onSetupPin
                        )
                    }

                    item {
                        SelectionCard(
                            title = if (lockType == LockType.PATTERN) "Change Pattern" else "Pattern Drawing",
                            description = "Draw to unlock device",
                            icon = Icons.Default.Edit,
                            isSelected = lockType == LockType.PATTERN,
                            onClick = onSetupPattern
                        )
                    }

                    item { SectionHeader("Zipper Protection") }

                    item {
                        SelectionCard(
                            title = "Zipper Only",
                            description = "Fun slide-to-unlock style",
                            icon = Icons.Default.KeyboardArrowDown,
                            isSelected = lockType == LockType.ZIPPER,
                            onClick = { viewModel.setLockType(LockType.ZIPPER) }
                        )
                    }

                    item {
                        SelectionCard(
                            title = "Zipper + PIN",
                            description = "Slide first, then enter PIN",
                            icon = Icons.Default.Add,
                            isSelected = lockType == LockType.ZIP_PIN,
                            onClick = { viewModel.setLockType(LockType.ZIP_PIN) }
                        )
                    }

                    item {
                        SelectionCard(
                            title = "Zipper + Pattern",
                            description = "Slide first, then draw pattern",
                            icon = Icons.Default.Build,
                            isSelected = lockType == LockType.ZIP_PATTERN,
                            onClick = { viewModel.setLockType(LockType.ZIP_PATTERN) }
                        )
                    }

                    item { SectionHeader("Appearance") }

                    item {
                        SelectionCard(
                            title = "Lock Screen Wallpaper",
                            description = "Select categories wise wallpapers",
                            icon = Icons.Default.Info,
                            isSelected = false,
                            onClick = onSelectWallpaper
                        )
                    }

                    if (lockType == LockType.ZIPPER || lockType == LockType.ZIP_PIN || lockType == LockType.ZIP_PATTERN) {
                        item { SectionHeader("Zipper Customization") }
                        
                        item {
                            CustomizationCard(
                                title = "Zipper Style",
                                options = ZipperStyle.entries.map { it.name },
                                selectedOption = zipperStyle.name,
                                onOptionSelected = { viewModel.setZipperStyle(ZipperStyle.valueOf(it)) }
                            )
                        }

                        item {
                            CustomizationCard(
                                title = "Hook Style",
                                options = HookStyle.entries.map { it.name },
                                selectedOption = hookStyle.name,
                                onOptionSelected = { viewModel.setHookStyle(HookStyle.valueOf(it)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(alpha = 0.5f),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun LockStatusCard(lockType: LockType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (lockType != LockType.NONE) Color.Cyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (lockType != LockType.NONE) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (lockType != LockType.NONE) Color.Cyan else Color.White.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (lockType != LockType.NONE) "Active Protection" else "Device Unprotected",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (lockType != LockType.NONE) "Method: ${lockType.name.replace("_", " + ")}" else "Choose a method to enable protection",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.Cyan.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Cyan else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.Cyan else Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = Color.Cyan)
                )
            }
        }
    }
}

@Composable
fun CustomizationCard(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color.Cyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Cyan else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
