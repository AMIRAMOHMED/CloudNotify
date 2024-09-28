import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import com.example.cloudnotify.viewmodel.LocationViewModel
import com.example.cloudnotify.viewmodel.LocationViewModelFactory
import com.example.cloudnotify.wrapper.WeatherDataState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private lateinit var locationViewModel: LocationViewModel
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
// Initialize LocationViewModel
        val locationViewModelFactory = LocationViewModelFactory(requireActivity().application)
        locationViewModel = locationViewModelFactory.create(LocationViewModel::class.java)
        // Initialize ViewModel
        homeViewModelFactory = HomeViewModelFactory(weatherRepo, requireActivity().application, locationViewModel)
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
        homeViewModel.weatherDataFlow
            .onEach { state ->
                when (state) {
                    is WeatherDataState.Loading -> {
                        binding.lottieAnimationView.visibility = View.VISIBLE
                        binding.lottieAnimationView.playAnimation()
// Show the animation and play it
                        binding.lottieAnimationView.visibility = View.VISIBLE
                        binding.lottieAnimationView.playAnimation()
                        binding.scrollView.visibility=View.GONE
                    }

                    is WeatherDataState.Success -> {
                        // Hide loading animation and display data
                        binding.scrollView.visibility=View.VISIBLE

                        binding.lottieAnimationView.visibility = View.GONE


                        // Update UI with the weather data
                        binding.currentWeather = state.data.currentWeather
                        val weatherIconRes = converter.getWeatherIconResource(state.data.currentWeather.icon)
                        binding.imgWeather.setImageResource(weatherIconRes)

                        // Update adapters with the list of hourly and daily weather
                        hourWeatherAdapter.setList(state.data.hourlyWeather)
                        dailyWeatherItemAdapter.setList(state.data.dailyWeather)
                    }

                    is WeatherDataState.Error -> {
                        // Hide loading and content, show error message
                        binding.lottieAnimationView.visibility = View.GONE
                    }
                }
            }
            .launchIn(lifecycleScope)
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