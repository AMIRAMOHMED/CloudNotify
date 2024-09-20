package com.example.cloudnotify.data.model.remote.forcast


data class WeatherData(
    val clouds: Clouds?,
    val dt: Int,
    val dt_txt: String?,
    val main: Main,
    val pop: Double?,
    val rain: Rain?,
    val sys: Sys?,
    val visibility: Int?,
    val weather: List<Weather>,
    val wind: Wind?
)
data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)

data class Rain(
    val `3h`: Double
)

data class Wind(
    val deg: Int,
    val gust: Double,
    val speed: Double
)

data class Sys(
    val pod: String
)

data class Clouds(
    val all: Int
)