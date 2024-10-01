package com.example.cloudnotify.ui.fragment
import HomeViewModel
import HomeViewModelFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.cloudnotify.R
import com.example.cloudnotify.Utility.Converter
import com.example.cloudnotify.Utility.NetworkUtils
import com.example.cloudnotify.data.local.db.BookmarkLocationDao
import com.example.cloudnotify.data.local.db.WeatherDao
import com.example.cloudnotify.data.local.db.WeatherDataBase
import com.example.cloudnotify.data.model.local.BookmarkLocation
import com.example.cloudnotify.data.repo.BookmarkRepository
import com.example.cloudnotify.data.repo.WeatherRepository
import com.example.cloudnotify.databinding.FragmentLocationBottomSheetBinding
import com.example.cloudnotify.viewmodel.LocationViewModel
import com.example.cloudnotify.viewmodel.LocationViewModelFactory
import com.example.cloudnotify.viewmodel.map.BookmarkViewModel
import com.example.cloudnotify.viewmodel.map.BookmarkViewModelFactory
import com.example.cloudnotify.wrapper.WeatherDataState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
class LocationBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var weatherRepo: WeatherRepository
    private lateinit var networkUtils: NetworkUtils
    private lateinit var weatherDao: WeatherDao
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeViewModelFactory: HomeViewModelFactory
    private lateinit var binding: FragmentLocationBottomSheetBinding
    private lateinit var bookmarkLocationDao: BookmarkLocationDao
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var locationViewModel: LocationViewModel
    private val converter = Converter()

    private val bookmarkViewModel: BookmarkViewModel by viewModels {
        BookmarkViewModelFactory(bookmarkRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// Initialize WeatherDao
        weatherDao = WeatherDataBase.getInstance(requireActivity()).weatherDao
// Initialize BookmarkLocationDao
        bookmarkLocationDao = WeatherDataBase.getInstance(requireActivity()).bookmarkLocationDao

        // Initialize NetworkUtils
        networkUtils = NetworkUtils(requireContext())
        // Initialize LocationViewModel
        val locationViewModelFactory = LocationViewModelFactory(requireActivity().application)
        locationViewModel = locationViewModelFactory.create(LocationViewModel::class.java)

        // Initialize WeatherRepository with dependencies
        weatherRepo = WeatherRepository(
            weatherDao,
            requireActivity().application,

        )
        // Initialize BookmarkRepository with dependencies
        bookmarkRepository = BookmarkRepository(
            bookmarkLocationDao,

            )

        // Initialize ViewModel
        homeViewModelFactory =
            HomeViewModelFactory(weatherRepo, requireActivity().application)
        homeViewModel = homeViewModelFactory.create(HomeViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLocationBottomSheetBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        binding.imgFavorite.setOnClickListener {
            Log.i("TAG", "onViewCreated: " + "clicked")
        }

    }

    companion object {
        fun newInstance(locationDetails: String): LocationBottomSheetFragment {
            val fragment = LocationBottomSheetFragment()
            val args = Bundle().apply {
                putString("LOCATION_DETAILS", locationDetails)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private fun observeViewModel() {
        homeViewModel.weatherDataFlow
            .onEach { state ->
                when (state) {
                    is WeatherDataState.Loading -> {
                    }

                    is WeatherDataState.Success -> {

                        // Bind the current weather data to the UI
                        val weatherIconRes =
                            converter.getWeatherIconResource(state.data.currentWeather.icon)
                        binding.currentWeather = state.data.currentWeather
                        binding.imgWeather.setImageResource(weatherIconRes)
                        state.data.dailyWeather
                        state.data.hourlyWeather

                        // Create a BookmarkLocation object with the current weather data
                        val currentWeatherItem = BookmarkLocation(
                            id = state.data.currentWeather.id,
                            latitude = state.data.currentWeather.latitude,
                            longitude = state.data.currentWeather.longitude,
                            cityName = state.data.currentWeather.cityName,
                            weatherDescription = state.data.currentWeather.weatherDescription,
                            temperature = state.data.currentWeather.temperature
                        )

                        // Observe the bookmark favorite state
                        viewLifecycleOwner.lifecycleScope.launch {
                            bookmarkViewModel.isBookmarkFavorite(currentWeatherItem.id)
                                .collect { isFavorite ->
                                    // Update the favorite icon based on the current state
                                    binding.imgFavorite.setImageResource(
                                        if (isFavorite) R.drawable.bookmark_remove else R.drawable.bookmark_add
                                    )

                                    // Set a click listener to toggle the bookmark state
                                    binding.imgFavorite.setOnClickListener {
                                        viewLifecycleOwner.lifecycleScope.launch {
                                            bookmarkViewModel.toggleBookmark(
                                                currentWeatherItem,
                                                isFavorite
                                            )
                                                .collect { newIsFavorite ->
                                                    // Update the favorite icon after toggling
                                                    binding.imgFavorite.setImageResource(
                                                        if (newIsFavorite) R.drawable.bookmark else R.drawable.bookmark_remove
                                                    )

                                                    // Navigate to the FavouriteFragment after toggling the bookmark
                                                    val favouriteFragment = FavouriteFragment()
                                                    parentFragmentManager.beginTransaction()
                                                        .replace(
                                                            R.id.fragment_container,
                                                            favouriteFragment
                                                        )
                                                        .addToBackStack(null)
                                                        .commit()
                                                    dismiss() // Dismiss the current fragment or dialog
                                                }
                                        }
                                    }
                                }
                        }
                    }

                    is WeatherDataState.Error -> {
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope) // Make sure this is scoped properly
    }
}