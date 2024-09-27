
package com.example.cloudnotify.Utility

import com.example.cloudnotify.R
import com.example.cloudnotify.data.model.local.CurrentWeather
import com.example.cloudnotify.data.model.local.DailyWeather
import com.example.cloudnotify.data.model.local.HourlyWeather
import com.example.cloudnotify.data.model.remote.current.CurrentWeatherResponse
import com.example.cloudnotify.data.model.remote.forcast.WeatherForecastFor7DayResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Converter {

    fun formatDateTime(dt: Long, pattern: String): String {
        val date = Date(dt * 1000L)  // Convert timestamp from seconds to milliseconds
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }

    fun formattedDate(dt: Long): String {
        return formatDateTime(dt, "yyyy-MM-dd")  // Format the timestamp into "YYYY-MM-DD"
    }

    fun formattedHour(dt: Long): String {
        return formatDateTime(dt, "HH:mm")  // Format the timestamp into "HH:mm"
    }





    fun mapHourlyWeatherResponseToHourlyWeather(response: WeatherForecastFor7DayResponse): List<HourlyWeather> {
        return response.list.map { weatherData ->
            HourlyWeather(
                dt = weatherData.dt,
                hour = formattedHour(weatherData.dt.toLong()),  // Convert timestamp to formatted hour
                weatherDescription = weatherData.weather[0].description,  // Use the main weather description
                temperature = weatherData.main.temp.toInt(),  // Extract the temperature
                tempMax = weatherData.main.temp_max.toInt(),  // Extract the maximum temperature
                tempMin = weatherData.main.temp_min.toInt(),  // Extract the minimum temperature
                icon = weatherData.weather[0].icon  // Extract the weather icon
            )
        }
    }

    fun groupHourlyDataByDay(response: WeatherForecastFor7DayResponse): Map<String, List<HourlyWeather>> {
        return mapHourlyWeatherResponseToHourlyWeather(response)
            .groupBy { formattedDate(it.dt.toLong()) }  // Group by formatted date
    }
    fun formattedDayOfWeek(dt: Long): String {
        return formatDateTime(dt, "EEEE")  // "EEEE" gives the full name of the day (Monday, Tuesday, etc.)
    }

    fun mapWeatherResponseToDailyWithHourly(response: WeatherForecastFor7DayResponse): List<DailyWeather> {
        // Group hourly weather data by day
        val hourlyWeatherGroupedByDay = groupHourlyDataByDay(response)

        // Create a list of DailyWeather (one entry per day)
        return hourlyWeatherGroupedByDay.map { (day, hourlyWeatherList) ->

            DailyWeather(
                dayOfWeek =  formattedDayOfWeek(hourlyWeatherList[0].dt.toLong()),  // The date for this day
                weatherDescription = hourlyWeatherList[0].weatherDescription,  // Main weather description for the third hour
                tempMax = hourlyWeatherList[0].tempMax,  // Temperature for the third hour
                tempMin = hourlyWeatherList[0].tempMin,  // Temperature for the third hour
                icon = hourlyWeatherList[0].icon  // Use the icon of the third hour
            )
        }
    }


    fun getCurrentDayHourlyWeather(response: WeatherForecastFor7DayResponse): List<HourlyWeather> {
        // Get current date in "yyyy-MM-dd" format
        val currentDate = formattedDate(System.currentTimeMillis() / 1000L)

        // Get hourly weather grouped by day
        val hourlyWeatherGroupedByDay = groupHourlyDataByDay(response)

        // Return the hourly data for the current day
        return hourlyWeatherGroupedByDay[currentDate] ?: emptyList()
    }

    fun mapCurrentWeatherResponseToCurrentWeather(response: CurrentWeatherResponse): CurrentWeather {
        return CurrentWeather(
            temperature = response.main.temp.toInt(),
            tempMax = response.main.temp_max.toInt(),
            tempMin = response.main.temp_min.toInt(),
            weatherDescription =response.weather[0].description,
            icon =response.weather[0].icon,
            windSpeed = response.wind.speed.toInt(),
            rainPercentage =null,
            humidity =response.main.humidity,
            data =formattedDate(response.dt.toLong()),
            hour =formattedHour(response.dt.toLong()),
            sunriseTime =formattedHour(response.sys.sunrise.toLong()),
            sunsetTime =formattedHour(response.sys.sunset.toLong()),
            cityName = response.name,
            latitude=response.coord.lat,
            longitude=response.coord.lon,


            )
    }

    fun getWeatherIconResource(iconCode: String): Int {
        return when (iconCode) {
            // Clear sky
            "01d" -> R.drawable.clear_sky
            "01n" -> R.drawable.clear_sky_night

            // Few clouds
            "02d" -> R.drawable.few_cloud
            "02n" -> R.drawable.few_cloud_night

            // Scattered clouds
            "03d" -> R.drawable.cloudy
            "03n" -> R.drawable.cloudy_night

            // Broken clouds
            "04d" -> R.drawable.broken_cloud
            "04n" -> R.drawable.broken_cloud_night

            // Shower rain
            "09d" -> R.drawable.rain
            "09n" -> R.drawable.rain_night

            // Rain
            "10d" -> R.drawable.rain
            "10n" -> R.drawable.rain_night

            // Thunderstorm
            "11d" -> R.drawable.thunderstorm
            "11n" -> R.drawable.thunderstorm_night

            // Snow
            "13d" -> R.drawable.snow
            "13n" -> R.drawable.snow_night

            // Mist
            "50d" -> R.drawable.mist_night
            "50n" -> R.drawable.mist_night

            // Default
            else -> R.drawable.default_weather_icon
        }
    }
}

