package com.example.cloudnotify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cloudnotify.data.model.local.CurrentWeather
import com.example.cloudnotify.data.model.local.DailyWeather
import com.example.cloudnotify.data.model.local.HourlyWeather
import kotlinx.coroutines.flow.Flow
@Dao
interface WeatherDao {

    // Insert CurrentWeather
    @Insert
     fun insertCurrentWeather(currentWeather: CurrentWeather)

    // Get CurrentWeather
    @Query("SELECT * FROM current_weather")
    fun getCurrentWeather(): Flow<CurrentWeather>

    // Delete CurrentWeather
    @Query("DELETE FROM current_weather")
     fun deleteCurrentWeather() : Void

    // Insert HourlyWeather
    @Insert
     fun insertHourlyWeather(hourlyWeather: List<HourlyWeather>)

    // Get HourlyWeather
    @Query("SELECT * FROM hourly_weather")
    fun getHourlyWeather(): Flow<List<HourlyWeather>>

    // Delete all HourlyWeather
    @Query("DELETE FROM hourly_weather")
     fun deleteAllHourlyWeather() : Void

    // Insert DailyWeather
    @Insert
     fun insertDailyWeather(dailyWeather: List<DailyWeather>)

    // Get DailyWeather
    @Query("SELECT * FROM daily_weather")
    fun getDailyWeather(): Flow<List<DailyWeather>>

    // Delete all DailyWeather
    @Query("DELETE FROM daily_weather")
     fun deleteAllDailyWeather(): Void
}