package com.example.whatsthemovie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.whatsthemovie.ui.GameMode

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val btnFrame = findViewById<Button>(R.id.btnFrame)
        val btnQuote = findViewById<Button>(R.id.btnQuote)
        val btnMusic = findViewById<Button>(R.id.btnMusic)

        //Запуск игры
        btnFrame.setOnClickListener {
            startGame(GameMode.FRAME)
        }

        btnQuote.setOnClickListener {
            startGame(GameMode.QUOTE)
        }

        btnMusic.setOnClickListener {
            startGame(GameMode.MUSIC)
        }
    }
    private fun startGame(mode: GameMode) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("GAME_MODE", mode)
        startActivity(intent)
    }
}