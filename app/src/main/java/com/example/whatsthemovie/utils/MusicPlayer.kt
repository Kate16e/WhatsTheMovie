package com.example.whatsthemovie.utils
import android.content.Context
import android.media.MediaPlayer

class MusicPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun play(resId: Int) {
        stop()
        mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }
}