package com.example.cloudnotify.data.repo

import android.util.Log
import com.example.cloudnotify.Utility.Converter
import com.example.cloudnotify.Utility.NetworkUtils
import com.example.cloudnotify.data.local.WeatherDao
import com.example.cloudnotify.data.model.local.CurrentWeather
import com.example.cloudnotify.data.model.local.DailyWeather
import com.example.cloudnotify.data.model.local.HourlyWeather
import com.example.cloudnotify.data.model.remote.current.CurrentWeatherResponse
import com.example.cloudnotify.data.model.remote.forcast.WeatherForecastFor7DayResponse
import com.example.cloudnotify.network.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class WeatherRepository(
    private val weatherDao: WeatherDao,
    private val networkUtils: NetworkUtils
)
{

    private val converter = Converter()

    // Container class to hold all weather data types
    data class WeatherData(
        val currentWeather: CurrentWeather,
        val hourlyWeather: Flow<List<HourlyWeather>>, // Using Flow<List<HourlyWeather>> directly
        val dailyWeather: Flow<List<DailyWeather>>    // Using Flow<List<DailyWeather>> directly
    )

    fun getWeatherData(): Flow<WeatherData?> {
        return weatherDao.getCurrentWeather() // Start by getting the current weather from the local database
            .flatMapLatest { localCurrentWeather ->
                if (localCurrentWeather != null) {
                    // Emit local data first
                    val localData = fetchDataFromLocal() // Fetch local hourly and daily weather
                    flow {
                        emit(localData) // Emit local data
                        // Fetch from remote after emitting local data
                        try {
                            val remoteData = fetchDataFromRemote()
                            emit(remoteData) // Emit remote data once fetched
                        } catch (e: Exception) {
                            Log.e("WeatherRepository", "Error fetching remote data: ${e.message}")
                        }
                    }
                } else {
                    // If no local data, fetch only from remote
                    flow {
                        try {
                            val remoteData = fetchDataFromRemote()
                            emit(remoteData) // Emit remote data
                        } catch (e: Exception) {
                            Log.e("WeatherRepository", "Error fetching remote data: ${e.message}")
                            emit(null) // Emit null in case of an error
                        }
                    }
                }
            }
    }

    // Helper function to fetch local weather data
    private suspend fun fetchDataFromLocal(): WeatherData? {
        val currentWeather = weatherDao.getCurrentWeather().firstOrNull()
        val hourlyWeatherFlow = weatherDao.getHourlyWeather()
        val dailyWeatherFlow = weatherDao.getDailyWeather()

        return if (currentWeather != null) {
            WeatherData(
                currentWeather = currentWeather,
                hourlyWeather = hourlyWeatherFlow,
                dailyWeather = dailyWeatherFlow
            )
        } else {
            null
        }
    }


    private suspend fun fetchDataFromRemote(): WeatherData {
        // Fetch and process data from the remote API
        val currentWeatherResponse = getCurrentWeatherFromRemote()
        val forecastResponse = getForecastWeatherFromRemote()
//mapper
        val currentWeather = converter.mapCurrentWeatherResponseToCurrentWeather(currentWeatherResponse)
        val hourlyWeatherList = converter.getCurrentDayHourlyWeather(forecastResponse)
        val dailyWeatherList = converter.mapWeatherResponseToDailyWithHourly(forecastResponse)


// Clear old data and insert new data
        deleteCurrentWeather()
        deleteHourlyWeather()
        deleteDailyWeather()
        insertCurrentWeather(currentWeather)
        insertHourlyWeather(hourlyWeatherList)
        insertDailyWeather(dailyWeatherList)

        // Return WeatherData
        return WeatherData(
            currentWeather = currentWeather,
            hourlyWeather = weatherDao.getHourlyWeather(),
            dailyWeather = weatherDao.getDailyWeather()
        )
    }
    // Get data from the database
     fun getCurrentWeather(): Flow<CurrentWeather> = weatherDao.getCurrentWeather()
     fun getHourlyWeather(): Flow<List<HourlyWeather>> = weatherDao.getHourlyWeather()
     fun getDailyWeather(): Flow<List<DailyWeather>> = weatherDao.getDailyWeather()



    // Insert into the database
     fun insertCurrentWeather(currentWeather: CurrentWeather) = weatherDao.insertCurrentWeather(currentWeather)
     fun insertHourlyWeather(hourlyWeather: List<HourlyWeather>) = weatherDao.insertHourlyWeather(hourlyWeather)
     fun insertDailyWeather(dailyWeather: List<DailyWeather>) = weatherDao.insertDailyWeather(dailyWeather)

    // Delete from the database
     fun deleteCurrentWeather() = weatherDao.deleteCurrentWeather()
     fun deleteHourlyWeather() = weatherDao.deleteAllHourlyWeather()
     fun deleteDailyWeather() = weatherDao.deleteAllDailyWeather()

    // Remote interactions
    suspend fun getCurrentWeatherFromRemote(): CurrentWeatherResponse =
        RetrofitInstance.api.getCurrentWeather(31.205753, 29.924526)

    suspend fun getForecastWeatherFromRemote(): WeatherForecastFor7DayResponse =
        RetrofitInstance.api.getWeatherForecast(31.205753, 29.924526)
}
