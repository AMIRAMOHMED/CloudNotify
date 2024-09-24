package com.example.cloudnotify.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room

import androidx.room.RoomDatabase
import com.example.cloudnotify.data.model.local.BookmarkLocation
import com.example.cloudnotify.data.model.local.CurrentWeather
import com.example.cloudnotify.data.model.local.DailyWeather
import com.example.cloudnotify.data.model.local.HourlyWeather
@Database(
    entities = [CurrentWeather::class, HourlyWeather::class, DailyWeather::class, BookmarkLocation::class],
    version = 3
)
abstract class WeatherDataBase : RoomDatabase() {
    abstract val weatherDao: WeatherDao
    abstract val bookmarkLocationDao : BookmarkLocationDao


    companion object {
        @Volatile
        private var INSTANCE: WeatherDataBase? = null
        fun getInstance(context: Context): WeatherDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDataBase::class.java,
                    "weather_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}