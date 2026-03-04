package com.example.whatsthemovie.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.*
@Dao
interface MovieDao {
    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Int): MovieEntity?

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getCount(): Int

    //получить случайные фильмы, исключая указанный
    @Query("SELECT * FROM movies WHERE id != :id ORDER BY RANDOM() LIMIT 3")
    suspend fun getRandomOtherMovies(id: Int): List<MovieEntity>

    @Insert
    suspend fun insertAll(movies: List<MovieEntity>)
}