package com.example.whatsthemovie.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MovieEntity::class], version = 3, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    //Получить DAO для работы с фильмами
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile // Обеспечивает видимость изменений между потоками
        private var INSTANCE: MovieDatabase? = null

        //Получить экземпляр базы данных
        fun getDatabase(context: Context): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                ).fallbackToDestructiveMigration().build() // При смене версии — пересоздаёт базу
                INSTANCE = instance
                instance
            }
        }
    }
}