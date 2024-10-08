package com.example.cloudnotify.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object{

        private val retrofit by lazy{
            // to log responses of retrofit
            val logging  = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val client = OkHttpClient.Builder().addInterceptor(logging).build()
            Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/").
            addConverterFactory(GsonConverterFactory.create()).
            client(client).build()
        }

        // we will use this to make api calls
        val api by lazy{

            retrofit.create(WeatherService::class.java)
        }
    }
}