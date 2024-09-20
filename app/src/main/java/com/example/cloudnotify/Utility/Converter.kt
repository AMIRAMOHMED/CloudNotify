
package com.example.cloudnotify.Utility

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
                weatherDescription = weatherData.weather[0].main,  // Use the main weather description
                temperature = weatherData.main.temp,  // Extract the temperature
                tempMax = weatherData.main.temp_max,  // Extract the maximum temperature
                tempMin = weatherData.main.temp_min,  // Extract the minimum temperature
                icon = weatherData.weather[0].icon  // Extract the weather icon
            )
        }
    }

    fun groupHourlyDataByDay(response: WeatherForecastFor7DayResponse): Map<String, List<HourlyWeather>> {
        return mapHourlyWeatherResponseToHourlyWeather(response)
            .groupBy { formattedDate(it.dt.toLong()) }  // Group by formatted date
    }


    fun mapWeatherResponseToDailyWithHourly(response: WeatherForecastFor7DayResponse): List<DailyWeather> {
        // Group hourly weather data by day
        val hourlyWeatherGroupedByDay = groupHourlyDataByDay(response)

        // Create a list of DailyWeather (one entry per day)
        return hourlyWeatherGroupedByDay.map { (day, hourlyWeatherList) ->

            DailyWeather(
                dayOfWeek = day,  // The date for this day
                weatherDescription = hourlyWeatherList[2].weatherDescription,  // Main weather description for the third hour
                tempMax = hourlyWeatherList[2].tempMax,  // Temperature for the third hour
                tempMin = hourlyWeatherList[2].tempMin,  // Temperature for the third hour
                icon = hourlyWeatherList[2].icon  // Use the icon of the third hour
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
            temperature = response.main.temp,
            tempMax = response.main.temp_max,
            tempMin = response.main.temp_min,
            weatherDescription =response.weather[0].main,
            icon =response.weather[0].icon,
            windSpeed = response.wind.speed,
            rainPercentage =null,
            humidity =response.main.humidity,
            data =formattedDate(response.dt.toLong()),
            hour =formattedHour(response.dt.toLong()),
            sunriseTime =formattedHour(response.sys.sunrise.toLong()),
            sunsetTime =formattedHour(response.sys.sunset.toLong()),


            )
    }

}