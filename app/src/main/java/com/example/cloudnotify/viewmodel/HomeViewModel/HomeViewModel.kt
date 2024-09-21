package com.example.cloudnotify.viewmodel.HomeViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudnotify.data.model.local.CurrentWeather
import com.example.cloudnotify.data.model.local.DailyWeather
import com.example.cloudnotify.data.model.local.HourlyWeather
import com.example.cloudnotify.data.repo.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: WeatherRepository) : ViewModel() {

    private val _hourlyWeather = MutableStateFlow<List<HourlyWeather>>(emptyList())
    val hourlyWeather: StateFlow<List<HourlyWeather>> get() = _hourlyWeather

    private val _dailyWeather = MutableStateFlow<List<DailyWeather>>(emptyList())
    val dailyWeather: StateFlow<List<DailyWeather>> get() = _dailyWeather

    private val _currentWeather = MutableStateFlow<CurrentWeather?>(null)
    val currentWeather: StateFlow<CurrentWeather?> get() = _currentWeather

    init {
        fetchWeatherData()
    }

    private fun fetchWeatherData() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getWeatherData().collect { weatherData ->
                Log.d("WeatherData", "Weather Data: $weatherData") // Log the whole weather data

                weatherData?.let { data ->
                    // Collect hourly weather in a new coroutine
                    launch {
                        data.hourlyWeather.collect { hourlyWeatherList ->
                            _hourlyWeather.value = hourlyWeatherList
                            Log.d("HourlyWeather", "Hourly Weather: $hourlyWeatherList")
                        }
                    }

                    // Collect daily weather in a new coroutine
                    launch {
                        data.dailyWeather.collect { dailyWeatherList ->
                            _dailyWeather.value = dailyWeatherList
                            Log.d("DailyWeather", "Daily Weather: $dailyWeatherList")
                        }
                    }

                    // Update current weather
                    Log.d("CurrentWeather", "Current Weather: ${data.currentWeather}")
                    _currentWeather.value = data.currentWeather
                }
            }
        }
    }}