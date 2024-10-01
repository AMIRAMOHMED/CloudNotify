package com.example.cloudnotify.di
import android.content.Context
import com.example.cloudnotify.Utility.NetworkUtils
import com.example.cloudnotify.data.local.db.AlertNotificationDao
import com.example.cloudnotify.data.local.db.BookmarkLocationDao
import com.example.cloudnotify.data.local.db.WeatherDao
import com.example.cloudnotify.data.local.db.WeatherDataBase
import com.example.cloudnotify.data.local.sharedPrefrence.SharedPreferencesManager
import com.example.cloudnotify.data.repo.ALertNotificationRepo
import com.example.cloudnotify.data.repo.BookmarkRepository
import com.example.cloudnotify.data.repo.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWeatherDao(
        @ApplicationContext context: Context
    ): WeatherDao {
        // Provides a singleton instance of WeatherDao from the WeatherDatabase
        return WeatherDataBase.getInstance(context).weatherDao
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherDao: WeatherDao,
        sharedPreferencesManager: SharedPreferencesManager
    ): WeatherRepository {
        return WeatherRepository(weatherDao, sharedPreferencesManager)
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
        // Provides a singleton instance of NetworkUtils
        return NetworkUtils(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferencesManager(
        @ApplicationContext context: Context
    ): SharedPreferencesManager {
        return SharedPreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideAlertNotificationDao(
        @ApplicationContext context: Context
    ): AlertNotificationDao {
        // Assuming the AlertNotificationDao is retrieved similarly from the WeatherDataBase
        return WeatherDataBase.getInstance(context).alertNotificationDao
    }

    @Provides
    @Singleton
    fun provideAlertNotificationRepo(
        alertNotificationDao: AlertNotificationDao
    ): ALertNotificationRepo {
        return ALertNotificationRepo(alertNotificationDao)
    }
    @Provides
    @Singleton
    fun provideBookmarkLocationDao(
        @ApplicationContext context: Context
    ): BookmarkLocationDao {
        return WeatherDataBase.getInstance(context).bookmarkLocationDao
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(
        bookmarkLocationDao: BookmarkLocationDao
    ): BookmarkRepository {
        return BookmarkRepository(bookmarkLocationDao)
    }
}
