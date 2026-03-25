package com.example.whatsthemovie.Movieui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.whatsthemovie.data.MovieRepository
import com.example.whatsthemovie.ui.GameMode

class MovieViewModelFactory(private val repository: MovieRepository, private val mode: GameMode
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel(repository, mode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}