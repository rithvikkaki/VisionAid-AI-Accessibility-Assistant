package com.visionaid.assistant

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.visionaid.assistant.navigation.NavGraph
import com.visionaid.assistant.theme.VisionAidTheme
import com.visionaid.assistant.utils.CameraHelper
import com.visionaid.assistant.utils.I18n
import com.visionaid.assistant.utils.TtsHelper
import com.visionaid.assistant.viewmodel.VisionAidViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private val viewModel: VisionAidViewModel by viewModels()
    private lateinit var ttsHelper: TtsHelper
    private lateinit var cameraHelper: CameraHelper
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var audioManager: AudioManager
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var proximity: Sensor? = null
    private var lightSensor: Sensor? = null
    
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var lastLevelTick = 0L
    private var isTorchOn = false
    private var isTorchAlerted = false

    private var shakeCount = 0
    private var lastShakeTime = 0L
    private val mainHandler = Handler(Looper.getMainLooper())

    private var lastVolumeUpTime = 0L
    private var lastVolumeDownTime = 0L

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ttsHelper = TtsHelper(this)
        cameraHelper = CameraHelper(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        setContent {
            val state by viewModel.uiState.collectAsState()
            VisionAidTheme(darkTheme = state.isDarkTheme) {
                NavGraph(
                    viewModel = viewModel,
                    onCapture = { launchCamera() },
                    onSpeak = { startInternalSpeechRecognition(state.targetLang) },
                    onHint = { viewModel.speakHint(it) }
                )
            }
        }

        permissionLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.VIBRATE
        ))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val now = System.currentTimeMillis()
        val state = viewModel.uiState.value
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (now - lastVolumeUpTime < 500) {
                    viewModel.stopAudio()
                    viewModel.speakHint(I18n.get(state.currentLang, "voice_mode_started"), state.currentLang)
                    // Small delay to let TTS finish "Voice mode started" before listening
                    mainHandler.postDelayed({ startInternalSpeechRecognition(state.targetLang) }, 800)
                    lastVolumeUpTime = 0
                } else {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    lastVolumeUpTime = now
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (now - lastVolumeDownTime < 500) {
                    viewModel.stopAudio()
                    viewModel.speakHint(I18n.get(state.currentLang, "capture_mode_started"), state.currentLang)
                    launchCamera()
                    lastVolumeDownTime = 0
                } else {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    lastVolumeDownTime = now
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun routeAudioToEarpiece(isEarpiece: Boolean) {
        if (isEarpiece) {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                val earpiece = devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
                if (earpiece != null) audioManager.setCommunicationDevice(earpiece)
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = false
            }
        } else {
            audioManager.mode = AudioManager.MODE_NORMAL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.clearCommunicationDevice()
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = true
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val state = viewModel.uiState.value

        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]
            if (distance < 4.0f) {
                viewModel.updatePlaybackVolume(0.2f)
                routeAudioToEarpiece(true)
                tick()
            } else {
                viewModel.updatePlaybackVolume(1.0f)
                routeAudioToEarpiece(false)
            }
        }

        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            if (state.currentScreen == "camera_capture") {
                if (lux < 10f && !isTorchOn) {
                    toggleTorch(true)
                    if (!isTorchAlerted) {
                        viewModel.speakHint(I18n.get(state.currentLang, "hint_low_light"))
                        isTorchAlerted = true
                    }
                } else if (lux > 30f && isTorchOn) {
                    toggleTorch(false)
                }
            } else {
                if (isTorchOn) toggleTorch(false)
                isTorchAlerted = false
            }
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            if (z < -9f && abs(x) < 2f && abs(y) < 2f && viewModel.isPlaying) {
                viewModel.stopAudio()
                tick()
            }

            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt(x * x + y * y + z * z)
            val delta = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 13) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > 400) {
                    shakeCount++
                    lastShakeTime = now
                    mainHandler.removeCallbacksAndMessages(null)
                    mainHandler.postDelayed({
                        if (shakeCount >= 3) {
                            provideStatusUpdate()
                        } else if (shakeCount == 2) {
                            viewModel.stopAudio()
                            viewModel.navigateTo("home")
                            viewModel.speakHint(I18n.get(state.currentLang, "hint_returning_home"))
                        }
                        shakeCount = 0
                    }, 900)
                }
            }
        }

        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val deviation = abs(event.values[0]) + abs(event.values[1])
            if (deviation < 0.4f) {
                val now = System.currentTimeMillis()
                val dynamicInterval = (deviation * 1500).toLong().coerceIn(150, 1000)
                if (now - lastLevelTick > dynamicInterval) { 
                    tick()
                    lastLevelTick = now
                }
            }
        }
    }

    private fun toggleTorch(enabled: Boolean) {
        val camManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = camManager.cameraIdList[0]
            camManager.setTorchMode(cameraId, enabled)
            isTorchOn = enabled
        } catch (_: Exception) { }
    }

    private fun tick() {
        val vibrator = getVibrator()
        vibrator.vibrate(VibrationEffect.createOneShot(25, 60))
    }

    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun provideStatusUpdate() {
        val state = viewModel.uiState.value
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        
        val batteryLabel = I18n.get(state.currentLang, "status_battery")
        val percentLabel = I18n.get(state.currentLang, "status_percent")
        val timeLabel = I18n.get(state.currentLang, "status_time")
        val onlineLabel = I18n.get(state.currentLang, "status_online")
        
        viewModel.speakHint("$batteryLabel $level $percentLabel. $timeLabel $time. $onlineLabel.")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun launchCamera() {
        viewModel.navigateTo("camera_capture")
    }

    private fun startInternalSpeechRecognition(langCode: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        
        viewModel.stopAudio()
        ttsHelper.stop()
        
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        
        viewModel.navigateTo("focus")
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        
        val fullLangCode = when(langCode) {
            "te" -> "te-IN"
            "hi" -> "hi-IN"
            "ta" -> "ta-IN"
            "kn" -> "kn-IN"
            "ml" -> "ml-IN"
            "mr" -> "mr-IN"
            "bn" -> "bn-IN"
            "gu" -> "gu-IN"
            "pa" -> "pa-IN"
            "ur" -> "ur-PK"
            else -> langCode
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, fullLangCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, fullLangCode)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { tick() }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { 
                val state = viewModel.uiState.value
                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    viewModel.speakHint(I18n.get(state.currentLang, "error_no_speech"))
                } else {
                    viewModel.navigateTo("home")
                }
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()
                if (!text.isNullOrEmpty()) viewModel.processVoice(text)
                else viewModel.navigateTo("home")
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
        speechRecognizer?.destroy()
        sensorManager.unregisterListener(this)
    }
}
