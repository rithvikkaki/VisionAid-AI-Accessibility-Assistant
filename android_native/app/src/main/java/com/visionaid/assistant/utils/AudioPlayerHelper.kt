package com.visionaid.assistant.utils

import android.media.MediaPlayer
import android.os.Build

class AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun play(url: String, speed: Float = 1.0f, onCompletion: () -> Unit = {}) {
        stop()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    playbackParams = playbackParams.setSpeed(speed)
                }
                start()
            }
            setOnCompletionListener { onCompletion() }
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: return
        }
    }

    val isPlaying: Boolean get() = mediaPlayer?.isPlaying == true
}
