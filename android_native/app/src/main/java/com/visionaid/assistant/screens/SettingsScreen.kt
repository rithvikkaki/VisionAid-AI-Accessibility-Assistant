package com.visionaid.assistant.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.visionaid.assistant.theme.*
import com.visionaid.assistant.utils.I18n
import com.visionaid.assistant.viewmodel.VisionAidViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: VisionAidViewModel, navController: NavController) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    viewModel.navigateTo("home")
                    navController.popBackStack() 
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack, 
                    "Back", 
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                I18n.get(state.currentLang, "nav_settings"), 
                fontSize = 30.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.height(48.dp))

        // ── Reading Level ──
        Text(
            I18n.get(state.currentLang, "label_reading_level"), 
            fontSize = 18.sp, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.clickable { viewModel.speakHint(I18n.get(state.currentLang, "label_reading_level")) }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(16.dp), 
            color = MaterialTheme.colorScheme.surface, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                listOf("simple", "medium", "detailed").forEach { level ->
                    val isActive = state.readingLevel == level
                    var lastTapTime by remember { mutableLongStateOf(0L) }
                    val label = I18n.get(state.currentLang, "rl_$level")
                    
                    FilterChip(
                        selected = isActive,
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < 500) {
                                viewModel.updateReadingLevel(level)
                                viewModel.speakHint(label + " selected")
                            } else {
                                viewModel.speakHint(label)
                            }
                            lastTapTime = now
                        },
                        label = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    label,
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (state.isDarkTheme) White05 else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            containerColor = Color.Transparent
                        ),
                        border = if (isActive) FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = true,
                            borderColor = Color.Transparent,
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 0.dp,
                            selectedBorderWidth = 2.dp
                        ) else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
        Text(
            I18n.get(state.currentLang, "reading_hint"),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clickable { 
                viewModel.speakHint(I18n.get(state.currentLang, "reading_hint")) 
            },
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Voice Speed ──
        Row(
            modifier = Modifier.fillMaxWidth().clickable { viewModel.speakHint(I18n.get(state.currentLang, "label_voice_speed")) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                I18n.get(state.currentLang, "label_voice_speed"), 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "${state.currentSpeed}x", 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Slider(
            value = state.currentSpeed,
            onValueChange = { viewModel.updateSpeed(it) },
            valueRange = 0.5f..2.0f,
            steps = 14,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = if (state.isDarkTheme) White10 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
            modifier = Modifier.padding(horizontal = 8.dp).pointerInput(Unit) {
                detectTapGestures(onTap = { viewModel.speakHint("${state.currentSpeed}x") })
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Interface ──
        Text(
            I18n.get(state.currentLang, "label_interface"), 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Bold, 
            letterSpacing = 2.sp, 
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.padding(bottom = 24.dp).clickable { viewModel.speakHint(I18n.get(state.currentLang, "label_interface")) }
        )

        // Language row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                I18n.get(state.currentLang, "label_lang"), 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Medium, 
                color = MaterialTheme.colorScheme.onBackground
            )
            var lastLangTap by remember { mutableLongStateOf(0L) }
            TextButton(onClick = { 
                val now = System.currentTimeMillis()
                if (now - lastLangTap < 500) {
                    navController.navigate("languages") 
                } else {
                    viewModel.speakHint(I18n.get(state.currentLang, "label_lang"))
                }
                lastLangTap = now
            }) {
                Text(
                    I18n.get(state.currentLang, "btn_change"), 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Icon(
                    Icons.Default.ChevronRight, 
                    null, 
                    tint = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        HorizontalDivider(color = if (state.isDarkTheme) White05 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

        // Dark mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    I18n.get(state.currentLang, "label_dark_mode"), 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Medium, 
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    I18n.get(state.currentLang, "label_high_contrast"), 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold, 
                    letterSpacing = 2.sp, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            var lastDarkTap by remember { mutableLongStateOf(0L) }
            Switch(
                checked = state.isDarkTheme,
                onCheckedChange = { 
                    val now = System.currentTimeMillis()
                    if (now - lastDarkTap < 500) {
                        viewModel.toggleTheme(it) 
                    } else {
                        viewModel.speakHint(I18n.get(state.currentLang, "label_dark_mode"))
                    }
                    lastDarkTap = now
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BgCharcoal,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = Color.Black,
                    uncheckedTrackColor = if (state.isDarkTheme) White10 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Server Configuration ──
        Text(
            I18n.get(state.currentLang, "label_server_config"), 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Bold, 
            letterSpacing = 2.sp, 
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp).clickable { 
                viewModel.speakHint(I18n.get(state.currentLang, "label_server_config")) 
            }
        )

        var tempUrl by remember(state.serverUrl) { mutableStateOf(state.serverUrl) }
        
        OutlinedTextField(
            value = tempUrl,
            onValueChange = { tempUrl = it },
            label = { Text(I18n.get(state.currentLang, "label_server_url")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (state.isDarkTheme) White10 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                viewModel.updateServerUrl(tempUrl)
                viewModel.speakHint(I18n.get(state.currentLang, "btn_update_server"))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                I18n.get(state.currentLang, "btn_update_server"),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Reset
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            var lastResetTap by remember { mutableLongStateOf(0L) }
            TextButton(onClick = { 
                val now = System.currentTimeMillis()
                if (now - lastResetTap < 500) {
                    viewModel.resetSettings() 
                } else {
                    viewModel.speakHint(I18n.get(state.currentLang, "btn_reset"))
                }
                lastResetTap = now
            }) {
                Text(
                    I18n.get(state.currentLang, "btn_reset"), 
                    fontWeight = FontWeight.Bold, 
                    color = Color(0xFFFF5252),
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Text(
            I18n.get(state.currentLang, "app_version"), 
            fontSize = 10.sp, 
            fontWeight = FontWeight.Bold, 
            letterSpacing = 3.sp, 
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth().clickable { viewModel.speakHint(I18n.get(state.currentLang, "app_version")) },
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}
