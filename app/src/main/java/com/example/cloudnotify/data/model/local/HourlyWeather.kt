package com.example.cloudnotify.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "hourly_weather")
data class HourlyWeather(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,  // Auto-generated ID
val dt: Int,  // Original timestamp (as received from API)
val hour: String,  // Formatted hour (e.g., "15:00")
val weatherDescription: String,  // Weather description
val temperature: Double,  // Temperature for the hour
    val tempMax: Double,
    val tempMin: Double,
val icon: String  // Weather icon for the hour
)
