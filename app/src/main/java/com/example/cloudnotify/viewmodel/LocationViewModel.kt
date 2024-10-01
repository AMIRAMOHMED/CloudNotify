package com.example.cloudnotify.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cloudnotify.Utility.LocationSource
import com.example.cloudnotify.data.local.sharedPrefrence.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    private val _isManualUpdate = MutableLiveData<Boolean>()
    val isManualUpdate: LiveData<Boolean> get() = _isManualUpdate

    fun updateLocation(lat: Double, lon: Double, isManual: Boolean = false) {
        sharedPreferencesManager.deleteGpsLocation()
        _isManualUpdate.value = isManual
        sharedPreferencesManager.saveGpsLocationLat(lat)
        sharedPreferencesManager.saveGpsLocationLong(lon)
    }

    fun upDateSource(source: LocationSource) {
        sharedPreferencesManager.saveLocationSource(source.name)
    }
}