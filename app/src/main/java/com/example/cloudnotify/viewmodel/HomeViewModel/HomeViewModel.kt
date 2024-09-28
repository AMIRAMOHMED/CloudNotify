import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cloudnotify.Utility.LocationSource
import com.example.cloudnotify.data.repo.WeatherRepository
import com.example.cloudnotify.viewmodel.LocationViewModel
import com.example.cloudnotify.wrapper.WeatherDataState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class HomeViewModel(
    private val repo: WeatherRepository,
    application: Application,
    private val locationViewModel: LocationViewModel // Inject this
) : AndroidViewModel(application) {

    private val _weatherDataFlow = MutableStateFlow<WeatherDataState>(WeatherDataState.Loading)
    val weatherDataFlow: StateFlow<WeatherDataState> = _weatherDataFlow


    // LiveData to hold location and address data
    private val _locationData = MutableLiveData<Pair<Double, Double>>()
    val locationData: LiveData<Pair<Double, Double>> get() = _locationData

    private val _addressData = MutableLiveData<String>()
    val addressData: LiveData<String> get() = _addressData

    // FusedLocationProviderClient for getting the location
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    private var lastKnownLocation: Pair<Double, Double>? = null

    init {
        fetchWeatherData()
    }

    // Fetch current location once
    fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        updateLocation(location)
                    } else {
                        _addressData.postValue("Location not available")
                    }
                }
                .addOnFailureListener {
                    _addressData.postValue("Error retrieving location")
                }
        } catch (e: SecurityException) {
            _addressData.postValue("Location permission not granted")
        }
    }

    // Get location from city name
    fun getLatLongFromCity(cityName: String) {
        val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocationName(cityName, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val latitude = address.latitude
                val longitude = address.longitude

                locationViewModel.updateLocation(latitude.toLong(), longitude.toLong(), isManual = true)
                locationViewModel.upDateSource(LocationSource.SEARCH)

                Log.d("Geocoding", "City: $cityName, Lat: $latitude, Long: $longitude")
                _locationData.postValue(Pair(latitude, longitude))

                fetchWeatherData()
            } else {
                Log.d("Geocoding", "No location found for city: $cityName")
            }
        } catch (e: Exception) {
            Log.e("GeocodingError", "Error getting lat/lon for city: $cityName", e)
        }
    }

    // Update location logic
    private fun updateLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        locationViewModel.updateLocation(latitude.toLong(), longitude.toLong(), isManual = false)
        locationViewModel.upDateSource(LocationSource.GPS)

        val currentLat = lastKnownLocation?.first ?: 0.0
        val currentLon = lastKnownLocation?.second ?: 0.0

        if (hasLocationChanged(currentLat, currentLon, latitude, longitude)) {
            lastKnownLocation = Pair(latitude, longitude)
            _locationData.postValue(Pair(latitude, longitude))

            // Fetch weather data only if the update is manual or first time location is set
            if (locationViewModel.isManualUpdate.value == true || lastKnownLocation == null) {
                fetchWeatherData()
            } else {
                Log.d("Location", "Automatic location update ignored")
            }
        } else {
            Log.d("Location", "Location unchanged, skipping weather fetch")
        }
    }

    // Helper function to check if location has significantly changed
    private fun hasLocationChanged(currentLat: Double, currentLon: Double, newLat: Double, newLon: Double): Boolean {
        val threshold = 0.01 // Adjust this threshold as necessary
        return (Math.abs(currentLat - newLat) > threshold || Math.abs(currentLon - newLon) > threshold)
    }

    // Fetch weather data

    fun fetchWeatherData() {
        viewModelScope.launch {
            repo.getWeatherData().collectLatest { state ->
                _weatherDataFlow.value = state // Update the StateFlow with the latest state
            }
        }

    }
}
