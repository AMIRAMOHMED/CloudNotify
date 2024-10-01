package com.example.cloudnotify.data.repo
import android.util.Log
import com.example.cloudnotify.Utility.Converter
import com.example.cloudnotify.Utility.LocationSource
import com.example.cloudnotify.data.local.db.WeatherDao
import com.example.cloudnotify.data.local.sharedPrefrence.SharedPreferencesManager
import com.example.cloudnotify.data.model.local.CurrentWeather
import com.example.cloudnotify.data.model.local.DailyWeather
import com.example.cloudnotify.data.model.local.HourlyWeather
import com.example.cloudnotify.data.model.remote.current.CurrentWeatherResponse
import com.example.cloudnotify.data.model.remote.forcast.WeatherForecastFor7DayResponse
import com.example.cloudnotify.network.RetrofitInstance
import com.example.cloudnotify.wrapper.WeatherDataState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherDao: WeatherDao,
    private val sharedPreferencesManager: SharedPreferencesManager
) {
    private val converter = Converter()

    // Container class to hold all weather data types
    data class WeatherData(
        val currentWeather: CurrentWeather,
        val hourlyWeather: List<HourlyWeather>,
        val dailyWeather: List<DailyWeather>
    )



    fun getWeatherData(): Flow<WeatherDataState> {
        return flow {
            emit(WeatherDataState.Loading) // Emit loading state
            try {
                // Fetch remote weather data
                val remoteWeatherData = fetchDataFromRemote()
                emit(WeatherDataState.Success(remoteWeatherData)) // Emit success with data

                // Save to database if using GPS as the location source
                if (sharedPreferencesManager.getLocationSource() == "GPS") {
                    saveWeatherDataToDatabase(remoteWeatherData)
                    Log.i("WeatherRepository", "Saved weather data to database")
                }

            } catch (e: Exception) {
                Log.e("WeatherRepository", "Error fetching remote weather data: ${e.message}")
                emit(WeatherDataState.Error(e.message ?: "Unknown error")) // Emit error state

                // Fallback to local data
                val localData = fetchDataFromLocal()
                if (localData != null) {
                    emit(WeatherDataState.Success(localData)) // Emit success with local data
                } else {
                    emit(WeatherDataState.Error("No local data found")) // Emit error if no local data
                }
            }
        }.flowOn(Dispatchers.IO) // Handle on IO thread for performance
    }




    private suspend fun fetchDataFromRemote(): WeatherData {
        // Fetch current and forecast weather from remote API
        val currentWeatherResponse = getCurrentWeatherFromRemote()
        val forecastResponse = getForecastWeatherFromRemote()

        // Map remote responses to local data models
        val currentWeather = converter.mapCurrentWeatherResponseToCurrentWeather(currentWeatherResponse)
        val hourlyWeatherList = converter.getCurrentDayHourlyWeather(forecastResponse)
        val dailyWeatherList = converter.mapWeatherResponseToDailyWithHourly(forecastResponse)

        // Return all weather data (current, hourly, and daily)
        return WeatherData(
            currentWeather = currentWeather,
            hourlyWeather = hourlyWeatherList,
            dailyWeather = dailyWeatherList
        )
    }

    // Save weather data to the database only when LocationSource is "GPS"
    private fun saveWeatherDataToDatabase(weatherData: WeatherData) {
        // Clear old data before saving new weather data
        weatherDao.deleteCurrentWeather()
        weatherDao.deleteAllHourlyWeather()
        weatherDao.deleteAllDailyWeather()

        // Insert new weather data
        weatherDao.insertCurrentWeather(weatherData.currentWeather)
        weatherDao.insertHourlyWeather(weatherData.hourlyWeather)
        weatherDao.insertDailyWeather(weatherData.dailyWeather)
    }    private suspend fun fetchDataFromLocal(): WeatherData? {
        val currentWeather = weatherDao.getCurrentWeather().firstOrNull()
        val hourlyWeatherList = weatherDao.getHourlyWeather().first()  // Collect the flow here
        val dailyWeatherList = weatherDao.getDailyWeather().first()    // Collect the flow here

        return if (currentWeather != null) {
            WeatherData(
                currentWeather = currentWeather,
                hourlyWeather = hourlyWeatherList,
                dailyWeather = dailyWeatherList
            )
        } else {
            null
        }
    }



    // SharedPreferences getters for location, language, and unit settings
    fun getGpsLocationLat() = sharedPreferencesManager.getGpsLocationLat()
    fun getGpsLocationLong() = sharedPreferencesManager.getGpsLocationLong()
    private fun getLanguage() = sharedPreferencesManager.getLanguage()
    fun getUnit() = sharedPreferencesManager.getUnit()



// Location related functions
    fun updateLocation(lat: Double, lon: Double) {
        sharedPreferencesManager.deleteGpsLocation()

        sharedPreferencesManager.saveGpsLocationLat(lat)
        sharedPreferencesManager.saveGpsLocationLong(lon)
    }
    // Location source related functions
    fun upDateSource(source: LocationSource) {
        sharedPreferencesManager.saveLocationSource(source.name)
    }

    // Remote interactions (fetching from APIs)
    suspend fun getCurrentWeatherFromRemote(): CurrentWeatherResponse  {
        return RetrofitInstance.api.getCurrentWeather(
            lat = getGpsLocationLat().toDouble(),
            lon = getGpsLocationLong().toDouble(),
            units = getUnit(),
            lang = getLanguage()
        )
    }


    suspend fun getForecastWeatherFromRemote(): WeatherForecastFor7DayResponse {
        return RetrofitInstance.api.getWeatherForecast(
            lat = getGpsLocationLat().toDouble(),
            lon = getGpsLocationLong().toDouble(),
            units = getUnit(),
            lang = getLanguage()
        )
    }
}
