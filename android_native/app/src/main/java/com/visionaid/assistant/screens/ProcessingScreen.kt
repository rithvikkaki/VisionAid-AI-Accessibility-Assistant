package com.visionaid.assistant.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.visionaid.assistant.theme.*

@Composable
fun ProcessingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    Box(
        modifier = Modifier.fillMaxSize().background(Charcoal),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "VISIONAID AI", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp, color = White20
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Wave bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(100.dp)
            ) {
                listOf(20, 45, 70, 50, 25).forEachIndexed { i, baseH ->
                    val anim by infiniteTransition.animateFloat(
                        initialValue = 1f, targetValue = 1.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(i * 150)
                        ), label = "bar$i"
                    )
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height((baseH * anim).dp)
                            .background(AccentYellow, RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            Text("Processing...", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White90)
        }
    }
}
