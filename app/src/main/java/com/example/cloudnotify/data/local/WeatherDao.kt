package com.example.cloudnotify.data.local

import DailyWeather
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.cloudnotify.data.model.local.CurrentWeather
import com.example.cloudnotify.data.model.local.HourlyWeather
import kotlinx.coroutines.flow.Flow

@Dao
interface  WeatherDao {
//CurrentWeather
    @Insert
    suspend fun insertCurrentWeather(currentWeather: CurrentWeather)

    @Query("SELECT * FROM current_weather")
    suspend fun getCurrentWeather(): Flow<CurrentWeather>

    @Delete
    suspend fun deleteCurrentWeather(currentWeather: CurrentWeather)

    //HourlyWeather

    @Insert
    suspend fun insertHourlyWeather(hourlyWeather: HourlyWeather)

    @Query("SELECT * FROM hourly_weather")
    suspend fun getHourlyWeather(): Flow<HourlyWeather>

@Delete
suspend fun deleteHourlyWeather(hourlyWeather: HourlyWeather)

//DailyWeather

    @Insert
    suspend fun insertDailyWeather(dailyWeather: DailyWeather)

    @Query("SELECT * FROM daily_weather")
    suspend fun getDailyWeather(): Flow<DailyWeather>

    @Delete
    suspend fun deleteDailyWeather(dailyWeather: DailyWeather)

}