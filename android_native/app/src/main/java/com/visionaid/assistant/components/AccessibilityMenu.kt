package com.visionaid.assistant.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visionaid.assistant.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityMenu(
    onDismiss: () -> Unit,
    onRepeat: () -> Unit,
    onCycleSpeed: () -> Unit,
    onChangeLanguage: () -> Unit,
    onCycleComplexity: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgCharcoal,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(48.dp)
                    .height(5.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.width(48.dp).height(5.dp),
                    shape = RoundedCornerShape(3.dp),
                    color = White20
                ) {}
            }
        }
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text(
                "Accessibility Menu",
                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                color = White90, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            // 2x2 Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SheetButton(Icons.Outlined.Refresh, "Repeat Last", Modifier.weight(1f), onRepeat)
                SheetButton(Icons.Outlined.Speed, "Cycle Speed", Modifier.weight(1f), onCycleSpeed)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SheetButton(Icons.Outlined.Language, "Language", Modifier.weight(1f), onChangeLanguage)
                SheetButton(Icons.Outlined.Description, "Complexity", Modifier.weight(1f), onCycleComplexity)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Close button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = White05, contentColor = White90),
                border = androidx.compose.foundation.BorderStroke(1.dp, White10)
            ) {
                Text("Close Menu", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SheetButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = White05,
        border = androidx.compose.foundation.BorderStroke(1.dp, White10)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = label, tint = PrimaryYellow, modifier = Modifier.size(32.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = White90)
        }
    }
}
