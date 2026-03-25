package com.example.whatsthemovie.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.whatsthemovie.ui.GameMode

class Prefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    //Получаем ключ для режима
    private fun getKeyForMode(mode: GameMode): String {
        return when (mode) {
            GameMode.FRAME -> "best_score_frame"
            GameMode.QUOTE -> "best_score_quote"
            GameMode.MUSIC -> "best_score_music"
        }
    }

    //Получить лучший результат для конкретного режима
    fun getBestScore(mode: GameMode): Int {
        val key = getKeyForMode(mode)
        return prefs.getInt(key, 0)
    }

    //Сохранить лучший результат для конкретного режима
    fun setBestScore(mode: GameMode, score: Int) {
        val key = getKeyForMode(mode)
        val currentBest = getBestScore(mode)
        if (score > currentBest) {
            prefs.edit().putInt(key, score).apply()
        }
    }
}