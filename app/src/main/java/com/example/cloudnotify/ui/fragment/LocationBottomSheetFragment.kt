package com.example.cloudnotify.ui.fragment

import HomeViewModel
import HomeViewModelFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.cloudnotify.R
import com.example.cloudnotify.Utility.NetworkUtils
import com.example.cloudnotify.data.local.db.BookmarkLocationDao
import com.example.cloudnotify.data.local.db.WeatherDao
import com.example.cloudnotify.data.local.db.WeatherDataBase
import com.example.cloudnotify.data.model.local.BookmarkLocation
import com.example.cloudnotify.data.repo.BookmarkRepository
import com.example.cloudnotify.data.repo.WeatherRepository
import com.example.cloudnotify.databinding.FragmentLocationBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// Initialize WeatherDao
        weatherDao = WeatherDataBase.getInstance(requireActivity()).weatherDao
// Initialize BookmarkLocationDao
        bookmarkLocationDao = WeatherDataBase.getInstance(requireActivity()).bookmarkLocationDao

        // Initialize NetworkUtils
        networkUtils = NetworkUtils(requireContext())

        // Initialize WeatherRepository with dependencies
        weatherRepo = WeatherRepository(
            weatherDao,
            networkUtils,
            requireActivity().application

        )
        // Initialize BookmarkRepository with dependencies
        bookmarkRepository = BookmarkRepository(
            bookmarkLocationDao,

        )

        // Initialize ViewModel
        homeViewModelFactory = HomeViewModelFactory(weatherRepo, requireActivity().application)
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
    Log.i("TAG", "onViewCreated: "+"clicked")
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
        lifecycleScope.launch {
            homeViewModel.currentWeather.collect { currentWeather ->
                // Check if currentWeather is null
                currentWeather?.let {
                    // Update UI with current weather
                    Log.i("TAG", "observeViewModel: $currentWeather")
                    binding.currentWeather = currentWeather

                    // Create BookmarkLocation item from current weather
                    val currentWeatherItem = BookmarkLocation(
                        id = currentWeather.id,
                        latitude = currentWeather.latitude,
                        longitude = currentWeather.longitude,
                        cityName = currentWeather.cityName,
                        weatherDescription = currentWeather.weatherDescription,
                        temperature = currentWeather.temperature
                    )

                    // Launch a coroutine to check if item is in favorites
                    val isFavorite = bookmarkRepository.isBookmarkFavorite(currentWeatherItem.id)



                    // Set up click listener for the favorite button
                    binding.imgFavorite.setOnClickListener {
                        launch(Dispatchers.IO) {
                            // Toggle favorite status or add it as needed
                            if (!isFavorite) {
                                bookmarkRepository.insertBookmark(currentWeatherItem)
                                Log.i("bookmark", "observeViewModel: "+"added")
                                   val favouriteFragment=FavouriteFragment()
                                val transaction = parentFragmentManager.beginTransaction()
                                transaction.replace(R.id.fragment_container, favouriteFragment)
                                transaction.addToBackStack(null)
                                transaction.commit()

dismiss()

                            } else {
                                bookmarkRepository.deleteBookmarkById(currentWeatherItem.id)
                                Log.i("bookmark", "observeViewModel: "+"deleted")
                                dismiss()
                            }
                        }
                    }
                } ?: Log.e("TAG", "Current weather data is null")
            }
        }
    }
}