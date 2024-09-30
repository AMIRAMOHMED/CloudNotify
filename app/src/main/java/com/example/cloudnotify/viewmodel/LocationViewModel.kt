package com.example.cloudnotify.viewmodel
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cloudnotify.Utility.LocationSource
import com.example.cloudnotify.data.local.sharedPrefrence.SharedPreferencesManager
import androidx.lifecycle.ViewModel

class LocationViewModel(application: Application) : ViewModel() {
    private val _isManualUpdate = MutableLiveData<Boolean>()
    val isManualUpdate: LiveData<Boolean> get() = _isManualUpdate
    private val sharedPreferencesManager = SharedPreferencesManager(application)



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


