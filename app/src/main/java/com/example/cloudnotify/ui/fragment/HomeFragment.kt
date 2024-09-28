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
import com.example.cloudnotify.Utility.Converter
import com.example.cloudnotify.data.local.db.WeatherDao
import com.example.cloudnotify.data.local.db.WeatherDataBase
import com.example.cloudnotify.data.repo.WeatherRepository
import com.example.cloudnotify.databinding.FragmentHomeBinding
import com.example.cloudnotify.ui.adapters.HourWeatherItemAdapter
import com.example.cloudnotify.Utility.NetworkUtils
import com.example.cloudnotify.ui.adapters.DailyWeatherItemAdapter
import com.example.cloudnotify.ui.fragment.MapFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var weatherRepo: WeatherRepository
    private lateinit var networkUtils: NetworkUtils
    private lateinit var weatherDao: WeatherDao
    private lateinit var hourWeatherAdapter: HourWeatherItemAdapter
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeViewModelFactory: HomeViewModelFactory
    private lateinit var  dailyWeatherItemAdapter: DailyWeatherItemAdapter
    private val converter = Converter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize WeatherDao
        weatherDao = WeatherDataBase.getInstance(requireActivity()).weatherDao

        // Initialize NetworkUtils
        networkUtils = NetworkUtils(requireContext())

        // Initialize WeatherRepository with dependencies
        weatherRepo = WeatherRepository(
            weatherDao,
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
        dailyWeatherItemAdapter= DailyWeatherItemAdapter()

        binding.hourRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.hourRecyclerView.adapter = hourWeatherAdapter
        binding.dayRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.dayRecyclerView.adapter = dailyWeatherItemAdapter



        // Add a click listener to the add location button
        binding.btnAddLocation.setOnClickListener {
            // Create an instance of the destination fragment (MapFragment)
            val mapFragment = MapFragment()

            // Get the FragmentManager and start a transaction to replace the current fragment
            val transaction = parentFragmentManager.beginTransaction()

            // Replace the fragment and add the transaction to the back stack (so user can navigate back)
            transaction.replace(R.id.fragment_container, mapFragment)
            transaction.addToBackStack(null)  // Add this if you want the user to be able to go back

            // Commit the transaction
            transaction.commit()
        }

        // Check location permissions
        checkLocationPermissions()

        // Set up the SearchView
        setupSearchView()


        // Fetch and observe weather data in the fragment
        observeWeatherData()
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

    private fun observeWeatherData() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.weatherData.collectLatest { weatherData ->
                if (weatherData != null) {
                    // Update UI with weather data
                    binding.currentWeather = weatherData.currentWeather
                    val weatherIconRes = converter.getWeatherIconResource(weatherData.currentWeather.icon)
                    binding.imgWeather.setImageResource(weatherIconRes)
                    hourWeatherAdapter.setList(weatherData.hourlyWeather)
                    dailyWeatherItemAdapter.setList(weatherData.dailyWeather)


                } else {
                    // Handle no data case
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
    }
}