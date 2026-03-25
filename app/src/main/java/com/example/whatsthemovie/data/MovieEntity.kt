package com.example.whatsthemovie.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageId: Int, //id изображения
    val name: String, //название фильма
    val quote: String,    //цитата
    val musicId: Int   //музыка
)