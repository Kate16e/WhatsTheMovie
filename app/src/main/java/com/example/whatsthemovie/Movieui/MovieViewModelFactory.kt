package com.example.whatsthemovie.Movieui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.whatsthemovie.data.MovieRepository
import com.example.whatsthemovie.ui.GameMode
//Фабрика для создания MovieViewModel с параметрами
class MovieViewModelFactory(private val repository: MovieRepository, private val mode: GameMode
) : ViewModelProvider.Factory {
    /**
     * Создаёт экземпляр ViewModel
     * @param modelClass — класс ViewModel, которую нужно создать
     * @return T — созданный экземпляр ViewModel
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel(repository, mode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}