package com.example.screen.locker.presentation.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screen.locker.presentation.components.PatternLockView
import com.example.screen.locker.presentation.screens.lock.PatternViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PatternSetupScreen(
    onBack: () -> Unit,
    viewModel: PatternViewModel = koinViewModel()
) {
    var step by remember { mutableIntStateOf(1) }
    var firstPattern by remember { mutableStateOf<List<Int>>(emptyList()) }

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
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (step == 1) "Create Pattern" else "Confirm Pattern",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (step == 1) "Connect at least 4 dots to secure your device" else "Draw the pattern again to confirm",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.size(320.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        PatternLockView(
                            onPatternComplete = { pattern ->
                                if (step == 1) {
                                    if (pattern.size >= 4) {
                                        firstPattern = pattern
                                        step = 2
                                    }
                                } else {
                                    if (pattern == firstPattern) {
                                        viewModel.onPatternSetup(pattern)
                                        onBack()
                                    } else {
                                        step = 1
                                        firstPattern = emptyList()
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                TextButton(onClick = onBack) {
                    Text("Cancel", color = Color.Cyan)
                }
            }
        }
    }
}
