package com.example.cloudnotify.data.local

import DailyWeather
import android.content.Context
import androidx.room.Database
import androidx.room.Room

import androidx.room.RoomDatabase
import com.example.cloudnotify.data.model.local.CurrentWeather
import com.example.cloudnotify.data.model.local.HourlyWeather

@Database(
    entities = [CurrentWeather::class, HourlyWeather::class, DailyWeather::class],
    version = 1,
    exportSchema = false
)


abstract  class WeatherDataBase: RoomDatabase() {
abstract  val weatherDao: WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDataBase? = null
        fun getInstance(context: Context): WeatherDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDataBase::class.java,
                    "news_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}