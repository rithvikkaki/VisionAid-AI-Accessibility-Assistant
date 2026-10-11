package com.visionaid.assistant.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.visionaid.assistant.api.RetrofitClient
import com.visionaid.assistant.api.TextRequest
import com.visionaid.assistant.api.TranslateRequest
import com.visionaid.assistant.api.VisionResponse
import com.visionaid.assistant.utils.I18n
import com.visionaid.assistant.utils.TtsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class HistoryItem(
    val id: String,
    val title: String,
    val summary: String,
    val audioUrl: String?,
    val time: String
)

data class UiState(
    val currentLang: String = "en",
    val targetLang: String = "en",
    val readingLevel: String = "medium",
    val currentSpeed: Float = 1.0f,
    val isProcessing: Boolean = false,
    val isDarkTheme: Boolean = true,
    val currentSummary: String = "",
    val originalSummary: String = "",
    val originalLang: String = "en",
    val audioUrl: String? = null,
    val originalAudioUrl: String? = null,
    val error: String? = null,
    val currentScreen: String = "home",
    val isOnline: Boolean = true,
    val isInternetAvailable: Boolean = true,
    val history: List<HistoryItem> = emptyList(),
    val serverUrl: String = ""
)

class VisionAidViewModel(application: Application) : AndroidViewModel(application) {
    private val api = RetrofitClient.instance
    private val prefs = application.getSharedPreferences("visionaid", Context.MODE_PRIVATE)
    private var mediaPlayer: MediaPlayer? = null
    private val tts = TtsHelper(application)
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _ui = MutableStateFlow(loadState())
    val uiState = _ui.asStateFlow()

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    init {
        monitorInternet()
    }

    private fun loadState(): UiState {
        return UiState(
            currentLang = prefs.getString("lang", "en") ?: "en",
            targetLang = prefs.getString("target", "en") ?: "en",
            readingLevel = prefs.getString("level", "medium") ?: "medium",
            currentSpeed = prefs.getFloat("speed", 1.0f),
            isDarkTheme = prefs.getBoolean("dark", true),
            serverUrl = prefs.getString("server_url", RetrofitClient.BASE_URL) ?: RetrofitClient.BASE_URL
        ).also {
            RetrofitClient.updateBaseUrl(it.serverUrl)
        }
    }

    private fun monitorInternet() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _ui.update { it.copy(isInternetAvailable = true) }
                checkHealth()
            }
            override fun onLost(network: Network) {
                _ui.update { it.copy(isInternetAvailable = false, isOnline = false) }
            }
        })
    }

    fun checkHealth() {
        viewModelScope.launch {
            try {
                api.checkHealth()
                _ui.update { it.copy(isOnline = true) }
            } catch (e: Exception) {
                _ui.update { it.copy(isOnline = false) }
            }
        }
    }

    fun speakHint(text: String, lang: String? = null) {
        tts.speak(text, lang ?: _ui.value.currentLang, _ui.value.currentSpeed)
    }

    fun updateLanguage(lang: String) {
        _ui.update { it.copy(currentLang = lang, targetLang = lang) }
        prefs.edit().putString("lang", lang).putString("target", lang).apply()
        speakHint(I18n.get(lang, "lang_changed"), lang)
    }

    fun updateTargetLang(lang: String) {
        val state = _ui.value
        if (state.targetLang == lang) return

        _ui.update { it.copy(targetLang = lang) }
        prefs.edit().putString("target", lang).apply()
        
        if (state.currentScreen == "player" && state.originalSummary.isNotEmpty()) {
            if (lang == state.originalLang) {
                _ui.update { it.copy(currentSummary = state.originalSummary, audioUrl = state.originalAudioUrl) }
                state.originalAudioUrl?.let { playAudio(it) } ?: speakHint(state.originalSummary, lang)
            } else {
                translateContent(state.originalSummary, lang)
            }
        }
    }

    private fun translateContent(text: String, targetLang: String) {
        if (text.isEmpty()) return
        _ui.update { it.copy(isProcessing = true) }
        viewModelScope.launch {
            try {
                val resp = api.translate(TranslateRequest(text, targetLang))
                
                val rawAudio = resp.audio_url ?: resp.audio_file ?: resp.audio_path
                val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
                val audioUrl = rawAudio?.let {
                    val path = it.removePrefix("/")
                    if (it.startsWith("http")) it else "$baseUrl/$path"
                }
                
                _ui.update { state ->
                    val updatedHistory = state.history.toMutableList()
                    if (updatedHistory.isNotEmpty()) {
                        updatedHistory[0] = updatedHistory[0].copy(summary = resp.translated_text, audioUrl = audioUrl)
                    }
                    state.copy(
                        isProcessing = false,
                        currentSummary = resp.translated_text,
                        audioUrl = audioUrl,
                        history = updatedHistory
                    )
                }
                
                if (audioUrl != null) {
                    playAudio(audioUrl)
                } else {
                    speakHint(resp.translated_text, targetLang)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun updateReadingLevel(level: String) {
        _ui.update { it.copy(readingLevel = level) }
        prefs.edit().putString("level", level).apply()
    }

    fun cycleReadingLevel() {
        val levels = listOf("simple", "medium", "detailed")
        val currentIndex = levels.indexOf(_ui.value.readingLevel)
        val nextIndex = (currentIndex + 1) % levels.size
        val nextLevel = levels[nextIndex]
        updateReadingLevel(nextLevel)
        
        val lang = _ui.value.currentLang
        val levelLabel = I18n.get(lang, "rl_$nextLevel")
        val selectedLabel = I18n.get(lang, "label_selected")
        speakHint("$levelLabel $selectedLabel")
    }

    fun updateSpeed(speed: Float) {
        val s = (speed * 10).toInt() / 10f
        val boundedS = s.coerceIn(0.5f, 2.0f)
        _ui.update { it.copy(currentSpeed = boundedS) }
        prefs.edit().putFloat("speed", boundedS).apply()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(boundedS) ?: return
        }
    }

    fun cycleSpeed() {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        val current = _ui.value.currentSpeed
        val next = speeds.firstOrNull { it > current } ?: speeds.first()
        updateSpeed(next)
        speakHint("${next}x")
    }

    fun toggleTheme(dark: Boolean) {
        _ui.update { it.copy(isDarkTheme = dark) }
        prefs.edit().putBoolean("dark", dark).apply()
    }

    fun navigateTo(screen: String) {
        _ui.update { it.copy(currentScreen = screen) }
    }

    fun processVoice(text: String) {
        if (_ui.value.isProcessing) return
        _ui.update { it.copy(isProcessing = true, currentScreen = "processing", error = null) }
        
        viewModelScope.launch {
            try {
                val resp = api.processText(TextRequest(text, _ui.value.targetLang, _ui.value.readingLevel))
                handleResponse(resp)
            } catch (e: Exception) {
                handleException(e) { navigateTo("home") }
            } finally {
                _ui.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun uploadImage(imageFile: File) {
        if (_ui.value.isProcessing) return
        _ui.update { it.copy(isProcessing = true, currentScreen = "processing", error = null) }
        
        viewModelScope.launch {
            try {
                val reqFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", imageFile.name, reqFile)
                val langBody = _ui.value.targetLang.toRequestBody("text/plain".toMediaTypeOrNull())
                val levelBody = _ui.value.readingLevel.toRequestBody("text/plain".toMediaTypeOrNull())
                
                val resp = api.uploadImage(body, langBody, levelBody)
                handleResponse(resp)
            } catch (e: Exception) {
                handleException(e) { navigateTo("home") }
            } finally {
                _ui.update { it.copy(isProcessing = false) }
            }
        }
    }

    private fun handleException(e: Exception, onDone: () -> Unit = {}) {
        Log.e("VisionAid", "Backend Error", e)
        _ui.update { it.copy(isProcessing = false) }
        onDone()
        val errorMsg = if (e is retrofit2.HttpException && e.code() == 500) {
            I18n.get(_ui.value.currentLang, "error_server")
        } else {
            I18n.get(_ui.value.currentLang, "error_connection")
        }
        speakHint(errorMsg)
    }

    private fun handleResponse(resp: VisionResponse) {
        try {
            val summary = resp.ai_summary ?: resp.response ?: resp.text ?: "No result found."
            val rawAudio = resp.audio_url ?: resp.audio_file ?: resp.audio_path
            
            val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
            val audioUrl = rawAudio?.let {
                val path = it.removePrefix("/")
                if (it.startsWith("http")) it else "$baseUrl/$path"
            }
            
            val item = HistoryItem(
                id = System.currentTimeMillis().toString(),
                title = if (summary.length > 40) summary.take(40) + "..." else summary,
                summary = summary,
                audioUrl = audioUrl,
                time = java.text.SimpleDateFormat("MMM dd • HH:mm", java.util.Locale.US).format(java.util.Date())
            )
            
            _ui.update {
                it.copy(
                    isProcessing = false,
                    currentSummary = summary,
                    originalSummary = summary,
                    originalLang = it.targetLang,
                    audioUrl = audioUrl,
                    originalAudioUrl = audioUrl,
                    currentScreen = "player",
                    history = listOf(item) + it.history
                )
            }
            
            if (audioUrl != null) {
                playAudio(audioUrl)
            } else {
                speakHint(summary, _ui.value.targetLang)
            }
        } catch (e: Exception) {
            Log.e("VisionAid", "Parsing Error", e)
            _ui.update { it.copy(isProcessing = false, currentScreen = "home") }
        }
    }

    fun playAudio(url: String) {
        stopAudio()
        try {
            Log.d("VisionAid", "Playing audio: $url")
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        playbackParams = PlaybackParams().setSpeed(_ui.value.currentSpeed)
                    }
                    start()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("VisionAid", "MediaPlayer error: $what, $extra")
                    // Fallback to TTS if audio fails
                    speakHint(_ui.value.currentSummary, _ui.value.targetLang)
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("VisionAid", "Audio Error", e)
            speakHint(_ui.value.currentSummary, _ui.value.targetLang)
        }
    }

    fun updatePlaybackVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        tts.stop()
    }

    fun repeatLast() {
        val state = _ui.value
        if (state.currentSummary.isNotEmpty()) {
            state.audioUrl?.let { playAudio(it) } ?: speakHint(state.currentSummary, state.targetLang)
        }
    }

    fun updateServerUrl(url: String) {
        _ui.update { it.copy(serverUrl = url) }
        prefs.edit().putString("server_url", url).apply()
        RetrofitClient.updateBaseUrl(url)
        checkHealth()
    }

    fun resetSettings() {
        prefs.edit().clear().apply()
        _ui.update { loadState() }
        updateServerUrl("https://visionaid-api.onrender.com/")
        speakHint(I18n.get(_ui.value.currentLang, "settings_reset"))
    }

    fun shareCurrentSummary(context: Context) {
        val summary = _ui.value.currentSummary
        if (summary.isEmpty()) return
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Summary"))
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause() else it.start()
        } ?: run {
            // If no media player, maybe try to play the current summary with TTS
            val state = _ui.value
            state.audioUrl?.let { playAudio(it) } ?: speakHint(state.currentSummary, state.targetLang)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        tts.shutdown()
    }
}
