import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cloudnotify.data.repo.WeatherRepository

class HomeViewModelFactory(
    private val repository: WeatherRepository,
    private val application: Application // Add the application parameter
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // Pass both repository and application to the HomeViewModel constructor
            return HomeViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
