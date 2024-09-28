package com.example.cloudnotify.wrapper

import com.example.cloudnotify.data.repo.WeatherRepository


sealed class WeatherDataState {
    object Loading : WeatherDataState()
    data class Success(val data: WeatherRepository.WeatherData) : WeatherDataState()
    data class Error(val message: String) : WeatherDataState()
}
