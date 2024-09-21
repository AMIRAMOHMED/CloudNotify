package com.example.cloudnotify.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_weather")
data class CurrentWeather(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val temperature: Int,
    val tempMax: Int,
    val tempMin: Int,      // Store as Int
    val weatherDescription: String,
    val icon: String,
    val windSpeed: Int,    // Store as Int
    val rainPercentage: Int?,
    val humidity: Int,
    val data: String,
    val hour: String,
    val sunriseTime: String,
    val sunsetTime: String
)