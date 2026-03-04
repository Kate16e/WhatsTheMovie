package com.example.whatsthemovie.Movieui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsthemovie.data.MovieEntity
import com.example.whatsthemovie.data.MovieRepository
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _currentMovie = MutableLiveData<MovieEntity?>()
    val currentMovie: LiveData<MovieEntity?> = _currentMovie

    private val _shuffledOptions = MutableLiveData<List<String>>()
    val shuffledOptions: LiveData<List<String>> = _shuffledOptions

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _isAnswered = MutableLiveData(false)
    val isAnswered: LiveData<Boolean> = _isAnswered

    private val _selectedAnswer = MutableLiveData<String?>()
    val selectedAnswer: LiveData<String?> = _selectedAnswer

    private val answeredMovies = mutableSetOf<Int>()

    private val _navigateToResult = MutableLiveData<Int>()
    val navigateToResult: LiveData<Int> = _navigateToResult

    init {
        loadNewMovie()
    }

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

    fun nextQuestion() {
        loadNewMovie()
    }

    fun restartGame() {
        answeredMovies.clear()
        _score.value = 0
        loadNewMovie()
    }
}