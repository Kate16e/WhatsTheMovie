package com.example.whatsthemovie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val btnFrame = findViewById<Button>(R.id.btnFrame)
        val btnQuote = findViewById<Button>(R.id.btnQuote)
        val btnMusic = findViewById<Button>(R.id.btnMusic)

        // Запуск игры (рабочая кнопка)
        btnFrame.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Пока ничего не делают
        btnQuote.setOnClickListener {
            // Заглушка
        }

        btnMusic.setOnClickListener {
            // Заглушка
        }
    }
}