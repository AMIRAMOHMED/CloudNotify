package com.example.cloudnotify.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_weather")
data class CurrentWeather(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Auto-generated ID
    val temperature: Double,
    val weatherDescription: String,
    val icon: String,
    val windSpeed: Double,
    val rainPercentage: Int?,
    val humidity: Int,
    val dateTime: String,
    val sunriseTime: String,
    val sunsetTime: String
)