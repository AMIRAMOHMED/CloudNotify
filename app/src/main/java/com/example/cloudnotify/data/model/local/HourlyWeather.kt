package com.example.cloudnotify.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "hourly_weather")
data class HourlyWeather(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,  // Auto-generated ID
val dt: Int,
val hour: String,
val weatherDescription: String,
val temperature: Int,
    val tempMax: Int,
    val tempMin: Int,
val icon: String
)
