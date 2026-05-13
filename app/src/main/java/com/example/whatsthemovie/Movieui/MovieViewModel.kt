package com.example.whatsthemovie.Movieui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsthemovie.data.MovieEntity
import com.example.whatsthemovie.data.MovieRepository
import com.example.whatsthemovie.ui.GameMode
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: MovieRepository, private val mode: GameMode) : ViewModel() {

    private val _currentMode = MutableLiveData<GameMode>()
    val currentMode: LiveData<GameMode> = _currentMode // Текущий режим игры
    private val _currentMovie = MutableLiveData<MovieEntity?>()
    val currentMovie: LiveData<MovieEntity?> = _currentMovie // Текущий фильм, который нужно угадать

    private val _shuffledOptions = MutableLiveData<List<String>>()
    val shuffledOptions: LiveData<List<String>> = _shuffledOptions // Перемешанные варианты ответов

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score // Текущий счёт игрока

    private val _isAnswered = MutableLiveData(false)
    val isAnswered: LiveData<Boolean> = _isAnswered  // Был ли дан ответ на текущий вопрос

    private val _selectedAnswer = MutableLiveData<String?>()
    val selectedAnswer: LiveData<String?> = _selectedAnswer // Какой вариант выбрал пользователь

    private val answeredMovies = mutableSetOf<Int>() // Множество ID уже отгаданных фильмов

    private val _navigateToResult = MutableLiveData<Int>()
    val navigateToResult: LiveData<Int> = _navigateToResult // Сигнал для перехода на экран результатов

    init {
        _currentMode.value = mode
        loadNewMovie()
    }

    //Загрузить новый вопрос (фильм)
    fun loadNewMovie() {
        viewModelScope.launch {
            repository.getRandomMovieExcluding(answeredMovies.toList()).collect { movie ->
                if (movie != null) {
                    _currentMovie.postValue(movie)

                    //Получаем случайные варианты из ДРУГИХ фильмов
                    val wrongOptions = repository.getRandomOptionsForMovie(movie.id)

                    //Создаем все варианты: правильный + 3 неправильных
                    val allOptions = (wrongOptions + movie.name).shuffled()

                    _shuffledOptions.postValue(allOptions)
                    _isAnswered.postValue(false)
                    _selectedAnswer.postValue(null)
                } else {
                    //Игра окончена
                    _navigateToResult.postValue(_score.value ?: 0)
                }
            }
        }
    }
 //Проверить ответ пользователя
    fun checkAnswer(selectedOption: String) {
        val current = _currentMovie.value
        if (current != null && !_isAnswered.value!!) {
            _selectedAnswer.value = selectedOption
            _isAnswered.value = true

            if (selectedOption == current.name) {
                _score.value = (_score.value ?: 0) + 1
            }

            answeredMovies.add(current.id)
        }
    }
    //Перейти к следующему вопросу
    fun nextQuestion() {
        loadNewMovie()
    }
    //Перезапустить игру
    fun restartGame() {
        answeredMovies.clear()
        _score.value = 0
        loadNewMovie()
    }
    //Досрочно завершить игру
    fun exitGame() {
        _navigateToResult.postValue(_score.value ?: 0)  //показываем результат с текущим счётом
    }
}