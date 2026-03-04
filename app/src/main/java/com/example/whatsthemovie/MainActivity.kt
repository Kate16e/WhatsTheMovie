package com.example.whatsthemovie

import android.R.*
import android.R.color.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.whatsthemovie.Movieui.MovieViewModel
import com.example.whatsthemovie.Movieui.MovieViewModelFactory
import com.example.whatsthemovie.data.MovieDatabase
import com.example.whatsthemovie.data.MovieEntity
import com.example.whatsthemovie.data.MovieRepository
import com.example.whatsthemovie.databinding.ActivityMainBinding
import com.example.whatsthemovie.ui.theme.WhatsTheMovieTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Используем ViewBinding для удобного доступа к элементам
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupViewModel()
        setupObservers()
        setupClickListeners()
        initializeDatabase()


    }

    private fun setupViewModel() {
        val database = MovieDatabase.getDatabase(this)
        val repository = MovieRepository(database.movieDao())
        val factory = MovieViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MovieViewModel::class.java]
    }

    private fun initializeDatabase() {
        // Запускаем инициализацию базы в фоновом потоке
        CoroutineScope(Dispatchers.IO).launch {
            val database = MovieDatabase.getDatabase(this@MainActivity)
            val repository = MovieRepository(database.movieDao())
            repository.initializeMovies()
        }
    }

    private fun setupObservers() {
        // Наблюдаем за текущим фильмом
        viewModel.currentMovie.observe(this) { movie ->
            movie?.let {
                binding.tvScore.text = "Счет: ${viewModel.score.value}"

                Glide.with(this)
                    .load(it.imageId)
                    .centerCrop()
                    .into(binding.ivMovieFrame)
            }
        }

        // Наблюдаем за перемешанными вариантами
        viewModel.shuffledOptions.observe(this) { options ->
            if (options.size == 4) {
                binding.btnOption1.text = options[0]
                binding.btnOption2.text = options[1]
                binding.btnOption3.text = options[2]
                binding.btnOption4.text = options[3]
            }
        }

        // Наблюдаем за состоянием ответа
        viewModel.isAnswered.observe(this) { isAnswered ->
            binding.btnNext.isEnabled = isAnswered
            if (isAnswered) {
                highlightAnswer()
                disableOptionButtons()
            }
        }

        // Наблюдаем за счётом
        viewModel.score.observe(this) { score ->
            binding.tvScore.text = "Счет: $score"
        }

        viewModel.navigateToResult.observe(this) { score ->
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("SCORE", score)
            startActivity(intent)
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnOption1.setOnClickListener { checkAnswer(binding.btnOption1.text.toString()) }
        binding.btnOption2.setOnClickListener { checkAnswer(binding.btnOption2.text.toString()) }
        binding.btnOption3.setOnClickListener { checkAnswer(binding.btnOption3.text.toString()) }
        binding.btnOption4.setOnClickListener { checkAnswer(binding.btnOption4.text.toString()) }

        binding.btnNext.setOnClickListener {
            viewModel.nextQuestion()
            resetButtonColors()
            enableOptionButtons()
        }
    }

    private fun checkAnswer(selectedOption: String) {
        viewModel.checkAnswer(selectedOption)
    }

    private fun highlightAnswer() {
        val currentMovie = viewModel.currentMovie.value
        val selectedAnswer = viewModel.selectedAnswer.value

        currentMovie?.let { movie ->
            // Зелёным подсвечиваем правильный ответ
            when (movie.name) {
                binding.btnOption1.text -> binding.btnOption1.setBackgroundColor(getColor(
                    android.R.color.holo_green_dark
                ))
                binding.btnOption2.text -> binding.btnOption2.setBackgroundColor(getColor(
                    android.R.color.holo_green_dark
                ))
                binding.btnOption3.text -> binding.btnOption3.setBackgroundColor(getColor(
                    android.R.color.holo_green_dark
                ))
                binding.btnOption4.text -> binding.btnOption4.setBackgroundColor(getColor(
                    android.R.color.holo_green_dark
                ))
            }

            // Если ответ неправильный, подсвечиваем выбранный красным
            if (selectedAnswer != movie.name) {
                when (selectedAnswer) {
                    binding.btnOption1.text -> binding.btnOption1.setBackgroundColor(getColor(
                        android.R.color.holo_red_dark
                    ))
                    binding.btnOption2.text -> binding.btnOption2.setBackgroundColor(getColor(
                        android.R.color.holo_red_dark
                    ))
                    binding.btnOption3.text -> binding.btnOption3.setBackgroundColor(getColor(
                        android.R.color.holo_red_dark
                    ))
                    binding.btnOption4.text -> binding.btnOption4.setBackgroundColor(getColor(
                        android.R.color.holo_red_dark
                    ))
                }
            }
        }
    }

    private fun resetButtonColors() {
        val defaultColor = getColor(android.R.color.darker_gray)
        binding.btnOption1.setBackgroundColor(defaultColor)
        binding.btnOption2.setBackgroundColor(defaultColor)
        binding.btnOption3.setBackgroundColor(defaultColor)
        binding.btnOption4.setBackgroundColor(defaultColor)
    }

    private fun enableOptionButtons() {
        binding.btnOption1.isEnabled = true
        binding.btnOption2.isEnabled = true
        binding.btnOption3.isEnabled = true
        binding.btnOption4.isEnabled = true
    }

    private fun disableOptionButtons() {
        binding.btnOption1.isEnabled = false
        binding.btnOption2.isEnabled = false
        binding.btnOption3.isEnabled = false
        binding.btnOption4.isEnabled = false
    }
}


/*
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 1. СНАЧАЛА инициализируем БД (синхронно, ждём завершения!)
        initializeDatabaseSync()

        // ✅ 2. ПОТОМ создаём ViewModel и настраиваем UI
        setupViewModel()
        setupObservers()
        setupClickListeners()

        // ✅ 3. Гарантируем отображение начального счёта
        updateScoreDisplay()
    }

    // ✅ Синхронная инициализация БД — ждём завершения перед продолжением
    private fun initializeDatabaseSync() {
        runBlocking {
            withContext(Dispatchers.IO) {
                val database = MovieDatabase.getDatabase(this@MainActivity)
                val repository = MovieRepository(database.movieDao())
                repository.initializeMovies()
            }
        }
        Log.d("DB_INIT", "✅ База данных инициализирована")
    }

    private fun setupViewModel() {
        val database = MovieDatabase.getDatabase(this)
        val repository = MovieRepository(database.movieDao())
        val factory = MovieViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MovieViewModel::class.java]
    }

    private fun setupObservers() {
        // 🔹 Наблюдаем за текущим фильмом
        viewModel.currentMovie.observe(this) { movie ->
            if (movie != null) {
                Log.d("GAME_DEBUG", "🎬 Загружен фильм: ${movie.name}")

                Glide.with(this)
                    .load(movie.imageId)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(binding.ivMovieFrame)

                // Показываем элементы игры, скрываем финальный экран
                showGameUI()
            } else {
                // ✅ Игра окончена — показываем финальный счёт
                Log.d("GAME_DEBUG", "🏁 Игра окончена, фильмов больше нет")
                showFinalScore()
            }
        }

        // 🔹 Наблюдаем за вариантами ответов
        viewModel.shuffledOptions.observe(this) { options ->
            if (options.size == 4) {
                binding.btnOption1.text = options[0]
                binding.btnOption2.text = options[1]
                binding.btnOption3.text = options[2]
                binding.btnOption4.text = options[3]
                Log.d("GAME_DEBUG", "🎲 Варианты: $options")
            }
        }

        // 🔹 Наблюдаем за состоянием ответа
        viewModel.isAnswered.observe(this) { isAnswered ->
            binding.btnNext.isEnabled = isAnswered
            if (isAnswered) {
                highlightAnswer()
                disableOptionButtons()
            }
        }

        // 🔹 Наблюдаем за счётом (ОДИН наблюдатель, без дубликатов!)
        viewModel.score.observe(this) { score ->
            updateScoreDisplay()
            Log.d("SCORE_DEBUG", "📊 Счёт обновлён: $score")
        }
    }

    // ✅ Вынесенный метод для обновления отображения счёта
    private fun updateScoreDisplay() {
        val score = viewModel.score.value ?: 0
        binding.tvScore.text = "Счет: $score"
    }

    // ✅ Показывает финальный экран с результатом
    private fun showFinalScore() {
        val finalScore = viewModel.score.value ?: 0
        binding.tvScore.text = "🏆 Игра окончена!\nВаш счёт: $finalScore"
        binding.ivMovieFrame.setImageResource(android.R.drawable.ic_menu_gallery)
        binding.btnNext.text = "Начать заново"
        binding.btnNext.isEnabled = true
        binding.btnNext.setOnClickListener {
            viewModel.restartGame()
            resetButtonColors()
            enableOptionButtons()
            showGameUI()
        }
        // Скрываем кнопки вариантов
        binding.btnOption1.visibility = android.view.View.GONE
        binding.btnOption2.visibility = android.view.View.GONE
        binding.btnOption3.visibility = android.view.View.GONE
        binding.btnOption4.visibility = android.view.View.GONE
    }

    // ✅ Показывает обычный интерфейс игры
    private fun showGameUI() {
        binding.btnOption1.visibility = android.view.View.VISIBLE
        binding.btnOption2.visibility = android.view.View.VISIBLE
        binding.btnOption3.visibility = android.view.View.VISIBLE
        binding.btnOption4.visibility = android.view.View.VISIBLE
        binding.btnNext.text = "Следующий вопрос"
        binding.btnNext.setOnClickListener {
            viewModel.nextQuestion()
            resetButtonColors()
            enableOptionButtons()
        }
        updateScoreDisplay()
    }

    private fun setupClickListeners() {
        binding.btnOption1.setOnClickListener { checkAnswer(binding.btnOption1.text.toString()) }
        binding.btnOption2.setOnClickListener { checkAnswer(binding.btnOption2.text.toString()) }
        binding.btnOption3.setOnClickListener { checkAnswer(binding.btnOption3.text.toString()) }
        binding.btnOption4.setOnClickListener { checkAnswer(binding.btnOption4.text.toString()) }
    }

    private fun checkAnswer(selectedOption: String) {
        viewModel.checkAnswer(selectedOption)
    }

    private fun highlightAnswer() {
        val currentMovie = viewModel.currentMovie.value
        val selectedAnswer = viewModel.selectedAnswer.value

        currentMovie?.let { movie ->
            // 🟢 Подсвечиваем правильный ответ зелёным
            highlightButtonIfTextMatches(movie.name, android.R.color.holo_green_dark)

            // 🔴 Если ответ неправильный, подсвечиваем выбранный красным
            if (selectedAnswer != movie.name && selectedAnswer != null) {
                highlightButtonIfTextMatches(selectedAnswer, android.R.color.holo_red_dark)
            }
        }
    }

    // ✅ Вспомогательный метод для подсветки кнопок
    private fun highlightButtonIfTextMatches(targetText: String, colorRes: Int) {
        val color = getColor(colorRes)
        when (targetText) {
            binding.btnOption1.text -> binding.btnOption1.setBackgroundColor(color)
            binding.btnOption2.text -> binding.btnOption2.setBackgroundColor(color)
            binding.btnOption3.text -> binding.btnOption3.setBackgroundColor(color)
            binding.btnOption4.text -> binding.btnOption4.setBackgroundColor(color)
        }
    }

    private fun resetButtonColors() {
        val defaultColor = getColor(android.R.color.darker_gray)
        binding.btnOption1.setBackgroundColor(defaultColor)
        binding.btnOption2.setBackgroundColor(defaultColor)
        binding.btnOption3.setBackgroundColor(defaultColor)
        binding.btnOption4.setBackgroundColor(defaultColor)
    }

    private fun enableOptionButtons() {
        binding.btnOption1.isEnabled = true
        binding.btnOption2.isEnabled = true
        binding.btnOption3.isEnabled = true
        binding.btnOption4.isEnabled = true
    }

    private fun disableOptionButtons() {
        binding.btnOption1.isEnabled = false
        binding.btnOption2.isEnabled = false
        binding.btnOption3.isEnabled = false
        binding.btnOption4.isEnabled = false
    }
}
*/
