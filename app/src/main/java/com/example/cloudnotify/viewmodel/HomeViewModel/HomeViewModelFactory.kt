package com.example.cloudnotify.viewmodel.HomeViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cloudnotify.data.repo.WeatherRepository

class HomeViewModelFactory(private val repository: WeatherRepository)  : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeVeiwModel::class.java)) {
            return HomeVeiwModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}