package com.visionaid.assistant.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.visionaid.assistant.theme.*
import com.visionaid.assistant.viewmodel.VisionAidViewModel

data class LangOption(val code: String, val name: String, val native: String)

val ALL_LANGS = listOf(
    LangOption("en", "English", "English"),
    LangOption("te", "Telugu", "తెలుగు"),
    LangOption("hi", "Hindi", "हिन्दी"),
    LangOption("ta", "Tamil", "தமிழ்"),
    LangOption("kn", "Kannada", "ಕನ್ನಡ"),
    LangOption("ml", "Malayalam", "മലയാളം"),
    LangOption("mr", "Marathi", "मराठी"),
    LangOption("bn", "Bengali", "বাংলা"),
    LangOption("gu", "Gujarati", "ગુજરાતી"),
    LangOption("pa", "Punjabi", "ਪੰਜਾਬੀ"),
    LangOption("ur", "Urdu", "اردو"),
    LangOption("fr", "French", "Français"),
    LangOption("es", "Spanish", "Español"),
    LangOption("de", "German", "Deutsch"),
    LangOption("ja", "Japanese", "日本語")
)

@Composable
fun LanguagesScreen(viewModel: VisionAidViewModel, navController: NavController) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Charcoal)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = White90)
            }
            Text("Select Language", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White90)
            Spacer(modifier = Modifier.width(44.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(ALL_LANGS) { lang ->
                val isSelected = lang.code == state.currentLang
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.updateLanguage(lang.code)
                            navController.popBackStack()
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceDark,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AccentYellow)
                             else androidx.compose.foundation.BorderStroke(1.dp, White10)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(lang.name, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                                color = if (isSelected) AccentYellow else White90)
                            Text(lang.native, fontSize = 13.sp,
                                color = if (isSelected) AccentYellow.copy(alpha = 0.7f) else White40)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = null,
                            tint = if (isSelected) AccentYellow else White20
                        )
                    }
                }
            }
        }
    }
}
