import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudnotify.R
import com.example.cloudnotify.data.local.db.WeatherDao
import com.example.cloudnotify.data.local.db.WeatherDataBase
import com.example.cloudnotify.data.repo.WeatherRepository
import com.example.cloudnotify.databinding.FragmentHomeBinding
import com.example.cloudnotify.ui.adapters.HourWeatherItemAdapter
import com.example.cloudnotify.Utility.NetworkUtils
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var weatherRepo: WeatherRepository
    private lateinit var networkUtils: NetworkUtils
    private lateinit var weatherDao: WeatherDao
    private lateinit var hourWeatherAdapter: HourWeatherItemAdapter
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeViewModelFactory: HomeViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize WeatherDao
        weatherDao = WeatherDataBase.getInstance(requireActivity()).weatherDao

        // Initialize NetworkUtils
        networkUtils = NetworkUtils(requireContext())

        // Initialize WeatherRepository with dependencies
        weatherRepo = WeatherRepository(
            weatherDao,
            networkUtils,
            requireActivity().application

        )

        // Initialize ViewModel
        homeViewModelFactory = HomeViewModelFactory(weatherRepo, requireActivity().application)
        homeViewModel = homeViewModelFactory.create(HomeViewModel::class.java)
        checkLocationPermissions()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Use view binding to inflate the layout
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and adapter with binding
        hourWeatherAdapter = HourWeatherItemAdapter()
        binding.dayRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.dayRecyclerView.adapter = hourWeatherAdapter

        // Check location permissions
        checkLocationPermissions()

        // Set up the SearchView
        setupSearchView()


        // Fetch and observe weather data in the fragment
        observeViewModel()
    }

    private fun setupSearchView() {
        val searchView: SearchView = binding.root.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    performSearch(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun performSearch(query: String) {
        // Trigger the ViewModel method to fetch latitude and longitude for the city
        homeViewModel.getLatLongFromCity(query)
        Toast.makeText(context, "Searching for: $query", Toast.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Collect hourly weather data
            homeViewModel.hourlyWeather.collect { hourlyWeatherList ->
                hourWeatherAdapter.setList(hourlyWeatherList)
            }
        }

        lifecycleScope.launch {
            // Collect current weather data
            homeViewModel.currentWeather.collect { currentWeather ->
                // Update UI with current weather
                binding.currentWeather = currentWeather // Ensure this is bound correctly in XML
            }
        }
    }

    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions are already granted, start location updates
            homeViewModel.getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, start location updates
                homeViewModel.getCurrentLocation()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}