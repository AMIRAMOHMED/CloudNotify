import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cloudnotify.Utility.LocationSource
import com.example.cloudnotify.data.model.local.CurrentWeather
import com.example.cloudnotify.data.model.local.DailyWeather
import com.example.cloudnotify.data.model.local.HourlyWeather
import com.example.cloudnotify.data.repo.WeatherRepository
import com.example.cloudnotify.viewmodel.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class HomeViewModel(private val repo: WeatherRepository, application: Application) : AndroidViewModel(application) {

    private val locationViewModel = LocationViewModel(application)


    private val _weatherData = MutableStateFlow<WeatherRepository.WeatherData?>(null)
    val weatherData = _weatherData.asStateFlow()
    // LiveData to hold location and address data
    private val _locationData = MutableLiveData<Pair<Double, Double>>()
    val locationData: LiveData<Pair<Double, Double>> get() = _locationData

    private val _addressData = MutableLiveData<String>()
    val addressData: LiveData<String> get() = _addressData

    // FusedLocationProviderClient for getting the location
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

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
//get loction from city
fun getLatLongFromCity(cityName: String) {
    val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())

    try {
        // The second argument '1' limits the result to 1 location
        val addresses = geocoder.getFromLocationName(cityName, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val latitude = address.latitude
            val longitude = address.longitude
            locationViewModel.updateLocation(latitude.toLong(), longitude.toLong())
            locationViewModel.upDateSource(LocationSource.SEARCH)

            // Log or use the latitude and longitude as needed
            Log.d("Geocoding", "City: $cityName, Lat: $latitude, Long: $longitude")


            // You could also update LiveData or StateFlow to expose this to the UI
            _locationData.postValue(Pair(latitude, longitude))
            fetchWeatherData()

        } else {
            Log.d("Geocoding", "No location found for city: $cityName")
        }
    } catch (e: Exception) {
        Log.e("GeocodingError", "Error getting lat/lon for city: $cityName", e)
    }
}

    private fun updateLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude


        // Save location in SharedPreferences
        locationViewModel.updateLocation(latitude.toLong(), longitude.toLong())
        locationViewModel.upDateSource(LocationSource.GPS)

        Log.d("Location", "updateLocation: " + latitude + "long " + longitude)
        _locationData.postValue(Pair(latitude, longitude))


        // Reverse geocode the location to get the address
        val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                _addressData.postValue(addresses[0].getAddressLine(0))
            } else {
                _addressData.postValue("Address not found")
            }
        } catch (e: Exception) {
            _addressData.postValue("Error retrieving address")
        }
    }

    fun fetchWeatherData() {
        viewModelScope.launch {
            repo.getWeatherData().collectLatest { data ->
                _weatherData.value = data
            }
        }
    }

}
