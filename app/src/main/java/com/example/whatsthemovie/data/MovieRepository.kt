package com.example.whatsthemovie.data

import com.example.whatsthemovie.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class MovieRepository(private val movieDao: MovieDao) {
    fun getRandomMovieExcluding(excludeIds: List<Int>): Flow<MovieEntity?> = flow {
        val movies = movieDao.getAllMovies().filter { it.id !in excludeIds }
        if (movies.isNotEmpty()) {
            val randomIndex = (0 until movies.size).random()
            emit(movies[randomIndex])
        } else {
            emit(null)
        }
    }

    //метод для получения 3 случайных вариантов
    suspend fun getRandomOptionsForMovie(currentMovieId: Int): List<String> {
        val otherMovies = movieDao.getRandomOtherMovies(currentMovieId)
        return otherMovies.map { it.name }
    }

    suspend fun initializeMovies() {
        if (movieDao.getCount() == 0) {
            val movies = createMovieList()
            movieDao.insertAll(movies)
        }
    }

    private fun createMovieList(): List<MovieEntity> {
        return listOf(
            MovieEntity(
                imageId = R.drawable.avatar,
                name = "Аватар"
            ),
            MovieEntity(
                imageId = R.drawable.inception,
                name = "Начало"
            ),
            MovieEntity(
                imageId = R.drawable.interstellar,
                name = "Интерстеллар"
            ),
            MovieEntity(
                imageId = R.drawable.avengers,
                name = "Мстители"
            ),
            MovieEntity(
                imageId = R.drawable.transformers,
                name = "Трансформеры"
            ),
            MovieEntity(
                imageId = R.drawable.titanic,
                name = "Титаник"
            )

        )
    }
}