package com.example.cloudnotify.network
import com.example.cloudnotify.BuildConfig
import com.example.cloudnotify.data.model.remote.current.CurrentWeatherResponse
import com.example.cloudnotify.data.model.remote.forcast.WeatherForecastFor7DayResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    // Forecast API endpoint using suspend function
    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en",
        @Query("appid") apiKey: String = BuildConfig.API_KEY): WeatherForecastFor7DayResponse


    // Current weather API endpoint using suspend function
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en",
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ):CurrentWeatherResponse
}
