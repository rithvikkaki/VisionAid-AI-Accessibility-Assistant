package com.visionaid.assistant.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TtsHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                Log.d("TtsHelper", "TTS Initialized successfully")
            } else {
                Log.e("TtsHelper", "TTS Initialization failed with status: $status")
            }
        }
    }

    fun speak(text: String, langCode: String = "en", speed: Float = 1.0f) {
        if (!isInitialized) {
            Log.w("TtsHelper", "Speak called before initialization")
            return
        }
        
        val locale = when(langCode) {
            "te" -> Locale("te", "IN")
            "hi" -> Locale("hi", "IN")
            "ta" -> Locale("ta", "IN")
            "kn" -> Locale("kn", "IN")
            "ml" -> Locale("ml", "IN")
            "mr" -> Locale("mr", "IN")
            "bn" -> Locale("bn", "IN")
            "gu" -> Locale("gu", "IN")
            "pa" -> Locale("pa", "IN")
            "ur" -> Locale("ur", "PK")
            "fr" -> Locale.FRANCE
            "es" -> Locale("es", "ES")
            "de" -> Locale.GERMANY
            "ja" -> Locale.JAPAN
            else -> Locale(langCode)
        }

        tts?.apply {
            val result = setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TtsHelper", "Language $langCode not supported or missing data. Result: $result")
                // Try generic locale if specific one fails
                setLanguage(Locale(langCode))
            }
            setSpeechRate(speed)
            Log.d("TtsHelper", "Speaking ($langCode): $text")
            speak(text, TextToSpeech.QUEUE_FLUSH, null, "VisionAidTts")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
