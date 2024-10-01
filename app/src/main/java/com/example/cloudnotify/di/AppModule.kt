//package com.example.cloudnotify.di
//import android.app.Application
//import android.content.Context
//import com.example.cloudnotify.Utility.NetworkUtils
//import com.example.cloudnotify.data.local.db.WeatherDao
//import com.example.cloudnotify.data.local.db.WeatherDataBase
//import com.example.cloudnotify.data.local.sharedPrefrence.SharedPreferencesManager
//import com.example.cloudnotify.data.repo.WeatherRepository
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object AppModule {
//
//    @Provides
//    @Singleton
//    fun provideWeatherDao(application: Application): WeatherDao {
//        // Provides a singleton instance of WeatherDao from the WeatherDataBase
//        return WeatherDataBase.getInstance(application).weatherDao
//    }
//    @Provides
//    @Singleton
//    fun provideWeatherRepository(
//        weatherDao: WeatherDao,
//        sharedPreferencesManager: SharedPreferencesManager
//    ): WeatherRepository {
//        return WeatherRepository(weatherDao, sharedPreferencesManager)
//    }
//
//
//    @Provides
//    @Singleton
//    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
//        // Provides a singleton instance of NetworkUtils
//        return NetworkUtils(context)
//    }
//
//    @Provides
//    @Singleton
//    fun provideSharedPreferencesManager(@ApplicationContext context: Context): SharedPreferencesManager {
//        // Provides a singleton instance of SharedPreferencesManager
//        return SharedPreferencesManager(context)
//    }
//}
