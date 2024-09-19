package com.example.cloudnotify.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "hourly_weather")
data class HourlyWeather(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Auto-generated ID
    val time: String,
    val weatherDescription: String,
    val temperature: Double,
    val icon: String
)