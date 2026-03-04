package com.example.whatsthemovie

import android.widget.Button
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val tvResult = findViewById<TextView>(R.id.tvResult)
        val btnBack = findViewById<Button>(R.id.btnBackToMenu)

        // Получаем счёт из Intent
        val score = intent.getIntExtra("SCORE", 0)
        tvResult.text = "Твой счёт: $score"

        btnBack.setOnClickListener {
            finish() // возврат в главное меню
        }
    }
}