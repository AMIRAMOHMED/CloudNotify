package com.example.cloudnotify.data.model.local



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_weather")
data class DailyWeather(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,  // Auto-generated ID
    val dayOfWeek: String,
    val weatherDescription: String,
    val tempMax: Int,
    val tempMin: Int,
    val icon: String
)
