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
import kotlinx.coroutines.flow.flowOf

class WeatherRepository(
    private val weatherDao: WeatherDao,
    private val networkUtils: NetworkUtils
) {
    private val converter = Converter()

    // Container class to hold all weather data types
    data class WeatherData(
        val currentWeather: CurrentWeather,
        val hourlyWeather: Flow<List<HourlyWeather>>, // Using Flow<List<HourlyWeather>> directly
        val dailyWeather: Flow<List<DailyWeather>>    // Using Flow<List<DailyWeather>> directly
    )

    fun getWeatherData(): Flow<WeatherData?> {
        return flow {
            val hasNetworkConnection = networkUtils.hasNetworkConnection()

            val weatherData = if (hasNetworkConnection) {
                try {
                    fetchDataFromRemote()
                } catch (e: Exception) {
                    Log.e("WeatherRepository", "Error fetching weather data: ${e.message}")
                    throw e
                }
            } else {
                fetchDataFromLocal()
            }

            emit(weatherData)
        }.catch { throwable ->
            Log.e("WeatherRepository", "Uncaught exception: ${throwable.message}")
        }
    }


//    fun getWeatherData(): Flow<WeatherData?> {
//        return networkUtils.observeNetworkState() // Observe network state as a Flow
//            .flatMapLatest { hasNetworkConnection ->
//                flow {
//                    val weatherData = if (hasNetworkConnection) {
//                        try {
//                            fetchDataFromRemote()
//                        } catch (e: Exception) {
//                            Log.e("WeatherRepository", "Error fetching weather data: ${e.message}")
//                            throw e
//                        }
//                    } else {
//                        fetchDataFromLocal()
//                    }
//
//                    emit(weatherData)
//                }
//            }
//            .catch { throwable ->
//                Log.e("WeatherRepository", "Uncaught exception: ${throwable.message}")
//            }
//    }

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
    private suspend fun fetchDataFromLocal(): WeatherData? {
        // Get local data
        val currentWeather = weatherDao.getCurrentWeather().firstOrNull()
        val hourlyWeatherFlow = weatherDao.getHourlyWeather()
        val dailyWeatherFlow = weatherDao.getDailyWeather()

        if (currentWeather != null) {
            return WeatherData(
                currentWeather = currentWeather,
                hourlyWeather = hourlyWeatherFlow,
                dailyWeather = dailyWeatherFlow
            )
        } else {
            // Handle the case where no local data exists
            return null
        }
    }


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
