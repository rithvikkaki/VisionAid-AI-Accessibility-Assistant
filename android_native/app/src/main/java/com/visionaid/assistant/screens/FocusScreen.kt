package com.visionaid.assistant.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visionaid.assistant.theme.*
import com.visionaid.assistant.viewmodel.VisionAidViewModel

@Composable
fun FocusScreen(viewModel: VisionAidViewModel, onExit: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val ring1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearOutSlowInEasing), RepeatMode.Restart),
        label = "ring1"
    )
    val ring2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearOutSlowInEasing), RepeatMode.Restart, initialStartOffset = StartOffset(1000)),
        label = "ring2"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Restart),
        label = "alpha1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Restart, initialStartOffset = StartOffset(1000)),
        label = "alpha2"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Charcoal).padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = White40, modifier = Modifier.size(20.dp))
                Text("FOCUS MODE", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, color = White40)
            }
            OutlinedButton(
                onClick = onExit,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = White40),
                border = androidx.compose.foundation.BorderStroke(1.dp, White10)
            ) {
                Text("Exit", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Close, contentDescription = "Exit", modifier = Modifier.size(16.dp))
            }
        }

        // Center area
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing rings
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(ring1)
                    .border(1.dp, AccentYellow.copy(alpha = alpha1), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .scale(ring2)
                    .border(1.dp, AccentYellow.copy(alpha = alpha2), CircleShape)
            )

            // Mic circle
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, White10),
                shadowElevation = 16.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Mic, contentDescription = "Listening", tint = AccentYellow, modifier = Modifier.size(48.dp))
                }
            }
        }

        // Text below
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Listening...", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = White90)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Speak naturally. Ask for a summary, read text, or describe surroundings.",
                fontSize = 14.sp, color = White40, textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}
