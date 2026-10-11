package com.visionaid.assistant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.visionaid.assistant.screens.*
import com.visionaid.assistant.viewmodel.VisionAidViewModel
import java.io.File

@Composable
fun NavGraph(
    viewModel: VisionAidViewModel,
    onCapture: () -> Unit,
    onSpeak: () -> Unit,
    onHint: (String) -> Unit
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()

    // Reactive Navigation from ViewModel (for Physical Buttons & State Changes)
    LaunchedEffect(state.currentScreen) {
        val currentRoute = navController.currentDestination?.route
        if (state.currentScreen != currentRoute) {
            when (state.currentScreen) {
                "player" -> navController.navigate("player") { popUpTo("home") }
                "processing" -> navController.navigate("processing")
                "camera_capture" -> navController.navigate("camera_capture")
                "focus" -> navController.navigate("focus")
                "home" -> navController.navigate("home") { popUpTo(0) }
            }
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                navController = navController,
                onSpeak = { 
                    viewModel.navigateTo("focus")
                    onSpeak() 
                },
                onCapture = { 
                    viewModel.navigateTo("camera_capture")
                }
            )
        }
        composable("camera_capture") {
            CameraCaptureScreen(
                viewModel = viewModel,
                onBack = { 
                    viewModel.navigateTo("home")
                    navController.popBackStack() 
                },
                onCaptured = { file ->
                    viewModel.uploadImage(file)
                    viewModel.navigateTo("processing")
                }
            )
        }
        composable("processing") {
            ProcessingScreen()
        }
        composable("focus") {
            FocusScreen(viewModel, onExit = { 
                viewModel.navigateTo("home")
                navController.popBackStack() 
            })
        }
        composable("player") {
            PlayerScreen(
                viewModel = viewModel, 
                navController = navController,
                onAskQuestion = {
                    viewModel.navigateTo("focus")
                    onSpeak()
                }
            )
        }
        composable("history") {
            HistoryScreen(viewModel, navController)
        }
        composable("settings") {
            SettingsScreen(viewModel, navController)
        }
        composable("languages") {
            LanguagesScreen(viewModel, navController)
        }
    }
}
