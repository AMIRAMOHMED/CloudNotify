package com.example.cloudnotify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cloudnotify.Utility.LocationSource
import com.example.cloudnotify.data.local.sharedPrefrence.SharedPreferencesManager

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesManager = SharedPreferencesManager(application)

    // LiveData to hold latitude and longitude separately
    private val _latitude = MutableLiveData<Long>()
    val latitude: LiveData<Long> get() = _latitude

    private val _longitude = MutableLiveData<Long>()
    val longitude: LiveData<Long> get() = _longitude

    private val _locationSource = MutableLiveData<LocationSource>()
    val locationSource: LiveData<LocationSource> get() = _locationSource


    fun updateLocation(lat: Long, lon: Long) {
        _latitude.postValue(lat)
        _longitude.postValue(lon)
        sharedPreferencesManager.saveGpsLocationLat(lat)
        sharedPreferencesManager.saveGpsLocationLong(lon)
    }


    fun upDateSource(source: LocationSource) {
        _locationSource.postValue(source)
        sharedPreferencesManager.saveLocationSource(source.toString())

    }

}