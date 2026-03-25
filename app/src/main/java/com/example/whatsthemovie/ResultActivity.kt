package com.example.whatsthemovie

import android.widget.Button
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsthemovie.data.MovieDatabase
import com.example.whatsthemovie.ui.GameMode
import com.example.whatsthemovie.utils.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {
    private lateinit var prefs: Prefs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        prefs = Prefs(this)

        val tvResult = findViewById<TextView>(R.id.tvResult)
        val tvBestScore = findViewById<TextView>(R.id.tvBestScore)
        val tvTotalQuestions = findViewById<TextView>(R.id.tvTotalQuestions)
        val btnBack = findViewById<Button>(R.id.btnBackToMenu)

        // Получаем счёт из Intent
        val score = intent.getIntExtra("SCORE", 0)
        val mode = intent.getSerializableExtra("GAME_MODE") as? GameMode ?: GameMode.FRAME
        // Получаем количество фильмов из базы
        CoroutineScope(Dispatchers.IO).launch {
            val totalMovies = MovieDatabase.getDatabase(this@ResultActivity).movieDao().getCount()
            runOnUiThread {
                tvTotalQuestions.text = "Всего вопросов: $totalMovies"
            }
        }
        //Получаем лучший счёт для этого режима
        val bestScore = prefs.getBestScore(mode)
        // Если текущий счёт больше лучшего — обновляем
        if (score > bestScore) {
            prefs.setBestScore(mode, score)
        }
        tvResult.text = "Твой счёт: $score"
        tvBestScore.text = "Лучший результат: ${prefs.getBestScore(mode)}"
        btnBack.setOnClickListener {
            finish() // возврат в главное меню
        }
    }
}