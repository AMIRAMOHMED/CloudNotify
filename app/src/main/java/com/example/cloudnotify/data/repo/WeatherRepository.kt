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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class WeatherRepository(
    private val weatherDao: WeatherDao,
    private val networkUtils: NetworkUtils
) {
    private val converter = Converter()

    // Container class to hold all weather data types
    data class WeatherData(
        val currentWeather: CurrentWeather,
        val hourlyWeather: List<HourlyWeather>,
        val dailyWeather: List<DailyWeather>
    )

    fun getWeatherData(): Flow<WeatherData> {
        return networkUtils.observeNetworkState().flatMapLatest { isConnected ->
            if (isConnected) {
                flow {
                    // Fetch and process data from the remote API
                    val currentWeatherResponse = getCurrentWeatherFromRemote()  // Fetch current weather
                    val forecastResponse = getForecastWeatherFromRemote()       // Fetch weather forecast
                    // Log sizes of data lists
                    Log.d("WeatherRepository", "currentWeatherResponse: ${forecastResponse.list.size}")
                    // Map API response to local models
                    val currentWeather = converter.mapCurrentWeatherResponseToCurrentWeather(currentWeatherResponse)
                    val hourlyWeatherList = converter.getCurrentDayHourlyWeather(forecastResponse)
                    Log.i("WeatherRepository", "getWeatherData: "+hourlyWeatherList.size)
                    val dailyWeatherList = converter.mapWeatherResponseToDailyWithHourly(forecastResponse)

                    // Clear old data from the database
                    deleteCurrentWeather()
                    deleteHourlyWeather()
                    deleteDailyWeather()

                    // Insert new data into the database
                    insertCurrentWeather(currentWeather)
                    insertHourlyWeather(hourlyWeatherList)
                    insertDailyWeather(dailyWeatherList)

                    // Emit the new data
                    emit(
                        WeatherData(
                            currentWeather = currentWeather,
                            hourlyWeather = hourlyWeatherList,
                            dailyWeather = dailyWeatherList
                        )
                    )
                }
            } else {
                flow {
                    // Fetch and emit data from the local DB when offline
                    val currentWeather = weatherDao.getCurrentWeather().first() // Collect Flow<CurrentWeather> to get the value
                    val hourlyWeather = weatherDao.getHourlyWeather().first()   // Collect Flow<List<HourlyWeather>> to get the value
                    val dailyWeather = weatherDao.getDailyWeather().first()     // Collect Flow<List<DailyWeather>> to get the value

                    emit(
                        WeatherData(
                            currentWeather = currentWeather,
                            hourlyWeather = hourlyWeather,
                            dailyWeather = dailyWeather
                        )
                    )
                }
            }
        }
    }

    // Insert into the database
    suspend fun insertCurrentWeather(currentWeather: CurrentWeather) = weatherDao.insertCurrentWeather(currentWeather)
    suspend fun insertHourlyWeather(hourlyWeather: List<HourlyWeather>) = weatherDao.insertHourlyWeather(hourlyWeather)
    suspend fun insertDailyWeather(dailyWeather: List<DailyWeather>) = weatherDao.insertDailyWeather(dailyWeather)

    // Delete from the database
    suspend fun deleteCurrentWeather() = weatherDao.deleteCurrentWeather()
    suspend fun deleteHourlyWeather() = weatherDao.deleteAllHourlyWeather()
    suspend fun deleteDailyWeather() = weatherDao.deleteAllDailyWeather()

    // Remote interactions
    suspend fun getCurrentWeatherFromRemote(): CurrentWeatherResponse =
        RetrofitInstance.api.getCurrentWeather(31.205753, 29.924526)

    suspend fun getForecastWeatherFromRemote(): WeatherForecastFor7DayResponse =
        RetrofitInstance.api.getWeatherForecast(31.205753, 29.924526)
}
