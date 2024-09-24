package com.example.cloudnotify.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmark_location")
data class BookmarkLocation(
    @PrimaryKey (autoGenerate = true)
    val id: Int = 0,
    val latitude: Int,
    val longitude: String,
    val cityName: String,
    val temperature: Int,
    val weatherDescription: String


)
