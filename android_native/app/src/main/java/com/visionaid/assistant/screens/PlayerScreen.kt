package com.visionaid.assistant.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.visionaid.assistant.theme.*
import com.visionaid.assistant.utils.I18n
import com.visionaid.assistant.viewmodel.VisionAidViewModel

@Composable
fun PlayerScreen(
    viewModel: VisionAidViewModel, 
    navController: NavController,
    onAskQuestion: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val langs = listOf(
        "en" to "EN", "te" to "TE", "hi" to "HI", "ta" to "TA",
        "kn" to "KN", "ml" to "ML", "mr" to "MR", "bn" to "BN",
        "gu" to "GU", "pa" to "PA", "ur" to "UR", "fr" to "FR",
        "es" to "ES", "de" to "DE", "ja" to "JA"
    )
    var isPlaying by remember { mutableStateOf(true) }

    // Stop audio when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAudio()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Charcoal)
    ) {
        // ── Language Bar Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgDark)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { 
                navController.popBackStack() 
            }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = White90, modifier = Modifier.size(28.dp))
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                langs.forEach { (code, label) ->
                    val isActive = code == state.targetLang
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = if (isActive) AccentYellow else White40,
                        modifier = Modifier
                            .clickable { 
                                viewModel.updateTargetLang(code)
                                viewModel.stopAudio()
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            }
            IconButton(onClick = { viewModel.shareCurrentSummary(context) }) {
                Icon(Icons.Default.Share, "Share", tint = White90, modifier = Modifier.size(28.dp))
            }
            IconButton(onClick = { navController.navigate("languages") }) {
                Icon(Icons.Default.Language, "Languages", tint = White90, modifier = Modifier.size(28.dp))
            }
        }

        // ── Player Body ──
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.isProcessing) {
                CircularProgressIndicator(color = AccentYellow)
                Spacer(modifier = Modifier.height(20.dp))
                // Localized processing text
                Text(
                    text = I18n.get(state.currentLang, "text_processing"),
                    color = White40, 
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = state.currentSummary.ifEmpty { I18n.get(state.currentLang, "player_ready") },
                    fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = White90, textAlign = TextAlign.Center,
                    maxLines = 8, overflow = TextOverflow.Ellipsis,
                    lineHeight = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Play button
            Button(
                onClick = {
                    viewModel.togglePlayPause()
                    isPlaying = !isPlaying
                },
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
            ) {
                Icon(
                    if (state.isProcessing) Icons.Default.HourglassEmpty 
                    else if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Charcoal,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Speed controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                IconButton(onClick = { viewModel.updateSpeed(state.currentSpeed - 0.1f) }) {
                    Icon(Icons.Default.Remove, "Slower", tint = White20, modifier = Modifier.size(32.dp))
                }
                Text("${state.currentSpeed}x", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White90)
                IconButton(onClick = { viewModel.updateSpeed(state.currentSpeed + 0.1f) }) {
                    Icon(Icons.Default.Add, "Faster", tint = White20, modifier = Modifier.size(32.dp))
                }
            }
        }

        // ── Ask a Question ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            val label = I18n.get(state.targetLang, "btn_ask_question")

            OutlinedButton(
                onClick = { 
                    // Single tap for responsive feel, context-aware language
                    onAskQuestion()
                    viewModel.speakHint(label)
                },
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, White10),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = White90),
                modifier = Modifier.height(56.dp).padding(horizontal = 24.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
            }
        }
    }
}
