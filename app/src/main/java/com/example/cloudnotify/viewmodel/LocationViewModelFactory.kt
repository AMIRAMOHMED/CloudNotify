package com.example.cloudnotify.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cloudnotify.data.repo.WeatherRepository

class LocationViewModelFactory( private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel( application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}