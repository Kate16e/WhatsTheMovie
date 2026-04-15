package com.example.whatsthemovie

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private var pulseAnimation: Animation? = null
    private var isMusicPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentMode = intent.getSerializableExtra("GAME_MODE") as? GameMode ?: GameMode.FRAME
        musicPlayer = MusicPlayer(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse)

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
        CoroutineScope(Dispatchers.IO).launch {
            val database = MovieDatabase.getDatabase(this@MainActivity)
            val repository = MovieRepository(database.movieDao())
            repository.initializeMovies()
        }
    }

    private fun setupObservers() {
        viewModel.currentMode.observe(this) { mode ->
            when (mode) {
                GameMode.FRAME -> showFrameMode()
                GameMode.QUOTE -> showQuoteMode()
                GameMode.MUSIC -> showMusicMode()
            }
        }

        viewModel.currentMovie.observe(this) { movie ->
            stopPulse()
            musicPlayer.stop()
            isMusicPlaying = false
            movie?.let {
                when (viewModel.currentMode.value) {
                    GameMode.FRAME -> {
                        Glide.with(this).load(it.imageId).centerCrop().into(binding.ivMovieFrame)
                    }
                    GameMode.QUOTE -> {
                        binding.tvQuote.text = it.quote
                    }
                    GameMode.MUSIC -> {
                        binding.btnPlayMusic.visibility = View.VISIBLE
                        binding.btnPlayMusic.tag = it.musicId
                    }
                    null -> {
                        Glide.with(this).load(it.imageId).centerCrop().into(binding.ivMovieFrame)
                    }
                }
            }
        }

        viewModel.shuffledOptions.observe(this) { options ->
            if (options.size == 4) {
                binding.btnOption1.text = options[0]
                binding.btnOption2.text = options[1]
                binding.btnOption3.text = options[2]
                binding.btnOption4.text = options[3]
            }
        }

        viewModel.isAnswered.observe(this) { isAnswered ->
            binding.btnNext.isEnabled = isAnswered
            if (isAnswered) {
                highlightAnswer()
                disableOptionButtons()
                stopPulse()
                isMusicPlaying = false
            }
        }

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
                if (!isMusicPlaying) {
                    musicPlayer.play(resId)
                    startPulse()
                    isMusicPlaying = true
                }
            }
        }

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
            // Зелёным подсвечиваем правильный ответ (через backgroundTint, чтобы сохранить форму)
            when (movie.name) {
                binding.btnOption1.text -> binding.btnOption1.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_dark)
                binding.btnOption2.text -> binding.btnOption2.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_dark)
                binding.btnOption3.text -> binding.btnOption3.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_dark)
                binding.btnOption4.text -> binding.btnOption4.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_dark)
            }

            // Если ответ неправильный, подсвечиваем выбранный красным
            if (selectedAnswer != movie.name) {
                when (selectedAnswer) {
                    binding.btnOption1.text -> binding.btnOption1.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)
                    binding.btnOption2.text -> binding.btnOption2.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)
                    binding.btnOption3.text -> binding.btnOption3.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)
                    binding.btnOption4.text -> binding.btnOption4.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)
                }
            }
        }
    }

    private fun resetButtonColors() {
        // Возвращаем светло-жёлтый цвет через backgroundTint
        val yellowColor = ContextCompat.getColorStateList(this, android.R.color.holo_orange_light)
        binding.btnOption1.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFACD"))
        binding.btnOption2.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFACD"))
        binding.btnOption3.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFACD"))
        binding.btnOption4.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFACD"))
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
        stopPulse()
        isMusicPlaying = false
    }

    private fun showQuoteMode() {
        binding.ivMovieFrame.visibility = View.GONE
        binding.tvQuote.visibility = View.VISIBLE
        binding.btnPlayMusic.visibility = View.GONE
        stopPulse()
        isMusicPlaying = false
    }

    private fun showMusicMode() {
        binding.ivMovieFrame.visibility = View.GONE
        binding.tvQuote.visibility = View.GONE
        binding.btnPlayMusic.visibility = View.VISIBLE
    }

    private fun startPulse() {
        pulseAnimation?.let {
            binding.btnPlayMusic.startAnimation(it)
        }
    }

    private fun stopPulse() {
        binding.btnPlayMusic.clearAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.stop()
        stopPulse()
    }
}