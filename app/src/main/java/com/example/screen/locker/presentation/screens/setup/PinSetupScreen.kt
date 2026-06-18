package com.example.screen.locker.presentation.screens.setup

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screen.locker.presentation.screens.lock.LockViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    onBack: () -> Unit,
    viewModel: LockViewModel = koinViewModel()
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val currentInput = if (isConfirming) confirmPin else pin

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Set Lock PIN", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Step Indicator
            Text(
                text = if (isConfirming) "Confirm your PIN" else "Enter a 4-digit PIN",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMsg ?: if (isConfirming) "Make sure it matches the first one" else "This PIN will be used to unlock your device",
                style = MaterialTheme.typography.bodyMedium,
                color = if (errorMsg != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < currentInput.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Custom Keypad
            PinKeypad(
                onDigitClick = { digit ->
                    if (currentInput.length < 4) {
                        errorMsg = null
                        if (isConfirming) confirmPin += digit else pin += digit
                        
                        if (!isConfirming && pin.length == 4) {
                            isConfirming = true
                        } else if (isConfirming && confirmPin.length == 4) {
                            if (pin == confirmPin) {
                                viewModel.onPinSetup(pin)
                                onBack()
                            } else {
                                errorMsg = "PINs do not match. Try again."
                                confirmPin = ""
                            }
                        }
                    }
                },
                onDeleteClick = {
                    if (isConfirming) {
                        if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                        else isConfirming = false // Go back to first PIN entry
                    } else {
                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    }
                }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun PinKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(null, "0", "delete")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                row.forEach { key ->
                    when (key) {
                        null -> Spacer(modifier = Modifier.size(72.dp))
                        "delete" -> KeypadButton(
                            content = { Icon(Icons.Default.Close, contentDescription = "Delete") },
                            onClick = onDeleteClick,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        else -> KeypadButton(
                            content = { Text(key, fontSize = 28.sp, fontWeight = FontWeight.Medium) },
                            onClick = { onDigitClick(key) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    content: @Composable () -> Unit,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.surface
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = color,
        shape = CircleShape,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
