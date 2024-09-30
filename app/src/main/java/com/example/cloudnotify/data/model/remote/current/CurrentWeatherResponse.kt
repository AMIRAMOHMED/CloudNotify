package com.example.cloudnotify.data.model.remote.current
import com.example.cloudnotify.data.model.remote.forcast.Clouds
import com.example.cloudnotify.data.model.remote.forcast.Coord
import com.example.cloudnotify.data.model.remote.forcast.Weather
import com.example.cloudnotify.data.model.remote.forcast.Wind

data class CurrentWeatherResponse(
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Int,
    val id: Int,
    val main: Main,
    val name: String,
    val sys: Sys,
    val timezone: Int,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
)

data class Sys(
    val country: String,
    val id: Int,
    val sunrise: Int,
    val sunset: Int,
    val type: Int
)