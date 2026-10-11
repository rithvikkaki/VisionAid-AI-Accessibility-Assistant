package com.visionaid.assistant.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.visionaid.assistant.components.AccessibilityMenu
import com.visionaid.assistant.theme.*
import com.visionaid.assistant.utils.I18n
import com.visionaid.assistant.viewmodel.VisionAidViewModel

@Composable
fun HomeScreen(
    viewModel: VisionAidViewModel,
    navController: NavController,
    onSpeak: () -> Unit,
    onCapture: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    // Swipe up detection
    val swipeThreshold = with(LocalDensity.current) { 50.dp.toPx() }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    offsetY += delta
                },
                onDragStopped = {
                    if (offsetY < -swipeThreshold) {
                        showMenu = true
                    }
                    offsetY = 0f
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val infiniteTransition = rememberInfiniteTransition(label = "glow")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.6f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                        label = "dot_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                                CircleShape
                            )
                            .shadow(
                                elevation = 8.dp, 
                                shape = CircleShape, 
                                ambientColor = MaterialTheme.colorScheme.primary,
                                spotColor = MaterialTheme.colorScheme.primary
                            )
                    )
                    Text(
                        "VISIONAID", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = 3.sp, 
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
                    )
                }

                // Status
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (state.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), 
                                CircleShape
                            )
                    )
                    Text(
                        if (state.isOnline) "ONLINE" else "OFFLINE",
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = 2.sp,
                        color = if (state.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                IconButton(onClick = { 
                    viewModel.speakHint(I18n.get(state.currentLang, "nav_settings"))
                    navController.navigate("settings") 
                }) {
                    Icon(
                        Icons.Default.Settings, 
                        contentDescription = "Settings", 
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            // ── Main Content ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AccessibilePillButton(
                    icon = Icons.Default.Mic,
                    label = I18n.get(state.currentLang, "btn_speak"),
                    onHint = { viewModel.speakHint(it) },
                    onClick = onSpeak
                )
                Spacer(modifier = Modifier.height(24.dp))
                AccessibilePillButton(
                    icon = Icons.Default.PhotoCamera,
                    label = I18n.get(state.currentLang, "btn_capture"),
                    onHint = { viewModel.speakHint(it) },
                    onClick = onCapture
                )
            }

            // ── Footer ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp)
                    .clickable { 
                        viewModel.speakHint(I18n.get(state.currentLang, "swipe_menu"))
                        showMenu = true 
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    I18n.get(state.currentLang, "swipe_menu"),
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold, 
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Bottom Navigation (Simplified Overlay for now)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(88.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val scanLabel = I18n.get(state.currentLang, "nav_scan")
                NavItem(Icons.Default.Mic, scanLabel, true) { 
                    viewModel.speakHint(scanLabel)
                    navController.navigate("home") 
                }
                val historyLabel = I18n.get(state.currentLang, "nav_history")
                NavItem(Icons.Outlined.History, historyLabel, false) { 
                    viewModel.speakHint(historyLabel)
                    navController.navigate("history") 
                }
                val settingsLabel = I18n.get(state.currentLang, "nav_settings")
                NavItem(Icons.Outlined.Settings, settingsLabel, false) { 
                    viewModel.speakHint(settingsLabel)
                    navController.navigate("settings") 
                }
            }
        }

        if (showMenu) {
            AccessibilityMenu(
                onDismiss = { showMenu = false },
                onRepeat = {
                    viewModel.repeatLast()
                    showMenu = false
                },
                onCycleSpeed = { viewModel.cycleSpeed() },
                onChangeLanguage = {
                    showMenu = false
                    navController.navigate("languages")
                },
                onCycleComplexity = { viewModel.cycleReadingLevel() }
            )
        }
    }

    LaunchedEffect(Unit) { viewModel.checkHealth() }
}

@Composable
fun NavItem(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            icon, 
            contentDescription = label,
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(28.dp)
        )
        Text(
            label.uppercase(), 
            fontSize = 10.sp, 
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            letterSpacing = 1.sp
        )
        if (isActive) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(20.dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
fun AccessibilePillButton(
    icon: ImageVector,
    label: String,
    onHint: (String) -> Unit,
    onClick: () -> Unit
) {
    var lastTapTime by remember { mutableLongStateOf(0L) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 500) {
                            onClick()
                        } else {
                            onHint(label)
                        }
                        lastTapTime = now
                    }
                )
            },
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, 
                contentDescription = label, 
                tint = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                label, 
                fontSize = 32.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
