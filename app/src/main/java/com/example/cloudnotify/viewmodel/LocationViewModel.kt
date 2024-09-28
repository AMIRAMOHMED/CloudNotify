package com.example.cloudnotify.viewmodel

import android.app.Application
import androidx.compose.ui.window.application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cloudnotify.Utility.LocationSource
import com.example.cloudnotify.data.local.sharedPrefrence.SharedPreferencesManager

import androidx.lifecycle.ViewModel

class LocationViewModel(application: Application) : ViewModel() {
    private val _isManualUpdate = MutableLiveData<Boolean>()
    val isManualUpdate: LiveData<Boolean> get() = _isManualUpdate
    private val sharedPreferencesManager = SharedPreferencesManager(application)


    private val _location = MutableLiveData<Pair<Long, Long>>()
    val location: LiveData<Pair<Long, Long>> get() = _location

    private val _locationSource = MutableLiveData<LocationSource>()
    val locationSource: LiveData<LocationSource> get() = _locationSource

    fun updateLocation(lat: Long, lon: Long, isManual: Boolean = false) {
        _isManualUpdate.value = isManual
        _location.value = Pair(lat, lon)

      sharedPreferencesManager.saveGpsLocationLat(lat)
        sharedPreferencesManager.saveGpsLocationLong(lon)
    }

    fun upDateSource(source: LocationSource) {
        _locationSource.value = source
    }
}


