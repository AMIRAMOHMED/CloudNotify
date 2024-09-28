import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cloudnotify.data.repo.WeatherRepository
import com.example.cloudnotify.viewmodel.LocationViewModel

class HomeViewModelFactory(
    private val repository: WeatherRepository,
    private val application: Application,  // Add the application parameter,
    private val locationViewModel: LocationViewModel // Inject the shared LocationViewModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // Pass both repository, application, and locationViewModel to the HomeViewModel constructor
            return HomeViewModel(repository, application, locationViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}