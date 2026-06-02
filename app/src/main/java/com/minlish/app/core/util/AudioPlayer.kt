package com.minlish.app.core.util

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

object AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun play(url: String) {
        if (url.isBlank()) return
        
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "Error playing audio: $what, $extra")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Exception in AudioPlayer: ${e.message}")
        }
    }
}
