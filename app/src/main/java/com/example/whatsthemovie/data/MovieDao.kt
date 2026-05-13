package com.example.whatsthemovie.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies") // Получить список всех фильмов из базы данных
    suspend fun getAllMovies(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :id") //Получить фильм по его уникальному идентификатору
    suspend fun getMovieById(id: Int): MovieEntity?

    @Query("SELECT COUNT(*) FROM movies") // Получить общее количество фильмов в базе данных
    suspend fun getCount(): Int

    //получить случайные фильмы, исключая указанный
    @Query("SELECT * FROM movies WHERE id != :id ORDER BY RANDOM() LIMIT 3")
    suspend fun getRandomOtherMovies(id: Int): List<MovieEntity>

    @Insert // Вставить список фильмов в базу данных
    suspend fun insertAll(movies: List<MovieEntity>)
}