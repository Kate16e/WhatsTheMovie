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
    private lateinit var musicPlayer: MusicPlayer          // Плеер для воспроизведения саундтреков
    private var currentMode: GameMode = GameMode.FRAME     // Текущий режим игры (кадр/цитата/музыка)
    private lateinit var binding: ActivityMainBinding      // ViewBinding для доступа к элементам интерфейса
    private lateinit var viewModel: MovieViewModel         // ViewModel с логикой игры
    private var pulseAnimation: Animation? = null          // Анимация пульсации для кнопки музыки
    private var isMusicPlaying = false                     // Флаг: играет ли сейчас музыка

    //Вызывается при создании активности
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем режим игры из Intent (передан из MainMenuActivity)
        currentMode = intent.getSerializableExtra("GAME_MODE") as? GameMode ?: GameMode.FRAME
        musicPlayer = MusicPlayer(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Загружаем анимацию пульсации из ресурсов
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse)
        // Настраиваем ViewModel, наблюдателей, обработчики кнопок и базу данных
        setupViewModel()
        setupObservers()
        setupClickListeners()
        initializeDatabase()
    }
    //Создаёт и настраивает ViewModel
    private fun setupViewModel() {
        val database = MovieDatabase.getDatabase(this)
        val repository = MovieRepository(database.movieDao())
        val factory = MovieViewModelFactory(repository, currentMode)
        viewModel = ViewModelProvider(this, factory)[MovieViewModel::class.java]
    }
    //Инициализирует базу данных фильмов в фоновом потоке
    private fun initializeDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            val database = MovieDatabase.getDatabase(this@MainActivity)
            val repository = MovieRepository(database.movieDao())
            repository.initializeMovies()
        }
    }
    //Настраивает наблюдатели (Observers) за изменениями данных в ViewModel
    private fun setupObservers() {
        viewModel.currentMode.observe(this) { mode ->
            when (mode) {
                GameMode.FRAME -> showFrameMode()
                GameMode.QUOTE -> showQuoteMode()
                GameMode.MUSIC -> showMusicMode()
            }
        }
        //Наблюдатель за текущим фильмом

        viewModel.currentMovie.observe(this) { movie ->
            stopPulse()
            musicPlayer.stop()
            isMusicPlaying = false
            movie?.let {
                when (viewModel.currentMode.value) {
                    GameMode.FRAME -> {
                        // Загружаем изображение через Glide и помещаем в ImageView
                        Glide.with(this).load(it.imageId).centerCrop().into(binding.ivMovieFrame)
                    }
                    GameMode.QUOTE -> {
                        binding.tvQuote.text = it.quote
                    }
                    GameMode.MUSIC -> {
                        // Показываем кнопку музыки и сохраняем ID саундтрека в tag
                        binding.btnPlayMusic.visibility = View.VISIBLE
                        binding.btnPlayMusic.tag = it.musicId
                    }
                    null -> {
                        // Если режим не определён — показываем изображение (значение по умолчанию)
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
        //Получает перемешанный список из 4 названий фильмов и устанавливает их на кнопки
        viewModel.isAnswered.observe(this) { isAnswered ->
            binding.btnNext.isEnabled = isAnswered
            if (isAnswered) {
                highlightAnswer()
                disableOptionButtons()
                stopPulse()
                isMusicPlaying = false
            }
        }

        // Наблюдатель за счётом — обновляет отображение счёта на экране
        viewModel.score.observe(this) { score ->
            binding.tvScore.text = "Счет: $score"
        }
        //Наблюдатель за переходом на экран результатов
        viewModel.navigateToResult.observe(this) { score ->
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("SCORE", score)
            intent.putExtra("GAME_MODE", currentMode)
            startActivity(intent)
            finish()
        }
    }
    //Настраивает обработчики нажатия на кнопки:
    private fun setupClickListeners() {
        binding.btnOption1.setOnClickListener { checkAnswer(binding.btnOption1.text.toString()) }
        binding.btnOption2.setOnClickListener { checkAnswer(binding.btnOption2.text.toString()) }
        binding.btnOption3.setOnClickListener { checkAnswer(binding.btnOption3.text.toString()) }
        binding.btnOption4.setOnClickListener { checkAnswer(binding.btnOption4.text.toString()) }
        // Кнопка "Следующий вопрос" — загружает новый вопрос и восстанавливает интерфейс
        binding.btnNext.setOnClickListener {
            viewModel.nextQuestion()
            resetButtonColors()
            enableOptionButtons()
        }
        // Кнопка воспроизведения музыки
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
        // Кнопка выхода (крестик) — досрочное завершение игры
        binding.btnClose.setOnClickListener {
            viewModel.exitGame()
        }
    }
    //Передаёт выбранный вариант ответа во ViewModel для проверки
    private fun checkAnswer(selectedOption: String) {
        viewModel.checkAnswer(selectedOption)
    }
    //Подсвечивает правильные и неправильные ответы после того, как пользователь сделал выбор
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
    //Разблокирует кнопки вариантов ответов (после загрузки нового вопроса)
    private fun enableOptionButtons() {
        binding.btnOption1.isEnabled = true
        binding.btnOption2.isEnabled = true
        binding.btnOption3.isEnabled = true
        binding.btnOption4.isEnabled = true
    }
    //Блокирует кнопки вариантов ответов (после того, как пользователь ответил)
    private fun disableOptionButtons() {
        binding.btnOption1.isEnabled = false
        binding.btnOption2.isEnabled = false
        binding.btnOption3.isEnabled = false
        binding.btnOption4.isEnabled = false
    }
    //Переключает интерфейс в режим "По кадру"
    private fun showFrameMode() {
        binding.ivMovieFrame.visibility = View.VISIBLE
        binding.tvQuote.visibility = View.GONE
        binding.btnPlayMusic.visibility = View.GONE
        stopPulse()
        isMusicPlaying = false
    }
    //Переключает интерфейс в режим "По цитате"
    private fun showQuoteMode() {
        binding.ivMovieFrame.visibility = View.GONE
        binding.tvQuote.visibility = View.VISIBLE
        binding.btnPlayMusic.visibility = View.GONE
        stopPulse()
        isMusicPlaying = false
    }
    //Переключает интерфейс в режим "По музыке"
    private fun showMusicMode() {
        binding.ivMovieFrame.visibility = View.GONE
        binding.tvQuote.visibility = View.GONE
        binding.btnPlayMusic.visibility = View.VISIBLE
    }
    //Запускает анимацию пульсации на кнопке воспроизведения музыки
    private fun startPulse() {
        pulseAnimation?.let {
            binding.btnPlayMusic.startAnimation(it)
        }
    }
    //Останавливает анимацию пульсации на кнопке воспроизведения музыки
    private fun stopPulse() {
        binding.btnPlayMusic.clearAnimation()
    }

    //Вызывается при уничтожении активности, станавливает музыку и анимацию, чтобы освободить ресурс
    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.stop()
        stopPulse()
    }
}