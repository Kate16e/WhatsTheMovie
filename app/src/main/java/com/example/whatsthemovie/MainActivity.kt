package com.example.whatsthemovie

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.whatsthemovie.Movieui.MovieViewModel
import com.example.whatsthemovie.Movieui.MovieViewModelFactory
import com.example.whatsthemovie.data.MovieDatabase
import com.example.whatsthemovie.data.MovieRepository
import com.example.whatsthemovie.databinding.ActivityMainBinding
import com.example.whatsthemovie.ui.GameMode
import com.example.whatsthemovie.utils.MusicPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var musicPlayer: MusicPlayer

    private var currentMode: GameMode = GameMode.FRAME

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //получаем режим игры из Intent (из меню)
        currentMode = intent.getSerializableExtra("GAME_MODE") as? GameMode ?: GameMode.FRAME

        //создаём музыкальный плеер
        musicPlayer = MusicPlayer(this)

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
        val factory = MovieViewModelFactory(repository, currentMode)
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
        //наблюдаем за режимом
        viewModel.currentMode.observe(this) { mode ->
            when (mode) {
                GameMode.FRAME -> showFrameMode()
                GameMode.QUOTE -> showQuoteMode()
                GameMode.MUSIC -> showMusicMode()
            }
        }
        // Наблюдаем за текущим фильмом
        viewModel.currentMovie.observe(this) { movie ->
            musicPlayer.stop() //останавливаем музыку перед новым вопросом
            movie?.let {
                when (viewModel.currentMode.value) {
                    GameMode.FRAME -> {
                        // Показываем картинку
                        Glide.with(this).load(it.imageId).centerCrop().into(binding.ivMovieFrame)
                    }
                    GameMode.QUOTE -> {
                        // Показываем цитату
                        binding.tvQuote.text = it.quote
                    }
                    GameMode.MUSIC -> {
                        // Показываем кнопку для музыки и сохраняем ID
                        binding.btnPlayMusic.visibility = View.VISIBLE
                        binding.btnPlayMusic.tag = it.musicId
                    }
                    null -> {
                        //Если режим не задан, показываем картинку по умолчанию
                        Glide.with(this).load(it.imageId).centerCrop().into(binding.ivMovieFrame)
                    }
                }
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
            intent.putExtra("GAME_MODE", currentMode)
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

        binding.btnPlayMusic.setOnClickListener {
            val musicResId = it.tag as? Int
            musicResId?.let { resId ->
                musicPlayer.play(resId)
            }
        }
        //кнопка выхода
        binding.btnClose.setOnClickListener {
            viewModel.exitGame()
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
    private fun showFrameMode() {
        binding.ivMovieFrame.visibility = View.VISIBLE
        binding.tvQuote.visibility = View.GONE
        binding.btnPlayMusic.visibility = View.GONE
    }

    private fun showQuoteMode() {
        binding.ivMovieFrame.visibility = View.GONE
        binding.tvQuote.visibility = View.VISIBLE
        binding.btnPlayMusic.visibility = View.GONE
    }

    private fun showMusicMode() {
        binding.ivMovieFrame.visibility = View.GONE
        binding.tvQuote.visibility = View.GONE
        binding.btnPlayMusic.visibility = View.VISIBLE
    }
    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.stop()
    }
}

