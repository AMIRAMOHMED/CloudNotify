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
import com.example.cloudnotify.viewmodel.map.BookmarkViewModel
import com.example.cloudnotify.viewmodel.map.BookmarkViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
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

        // Initialize WeatherRepository with dependencies
        weatherRepo = WeatherRepository(
            weatherDao,
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
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.weatherData.collectLatest { weatherData ->
                if (weatherData != null) {
                    val weatherIconRes =
                        converter.getWeatherIconResource(weatherData.currentWeather?.icon ?: "")
                    // Update UI with weather data
                    Log.i("nourrr", "observeViewModel: "+weatherData.currentWeather)
                    binding.currentWeather = weatherData.currentWeather
                    binding.imgWeather.setImageResource(weatherIconRes)


                    val currentWeatherItem = BookmarkLocation(
                        id = weatherData.currentWeather.id,
                        latitude = weatherData.currentWeather.latitude,
                        longitude = weatherData.currentWeather.longitude,
                        cityName = weatherData.currentWeather.cityName,
                        weatherDescription = weatherData.currentWeather.weatherDescription,
                        temperature = weatherData.currentWeather.temperature
                    )
                    viewLifecycleOwner.lifecycleScope.launch {
                        bookmarkViewModel.isBookmarkFavorite(currentWeatherItem.id)
                            .collect { isFavorite ->
                                binding.imgFavorite.setImageResource(
                                    if (isFavorite) R.drawable.bookmark_remove else R.drawable.bookmark_add
                                )


                                binding.imgFavorite.setOnClickListener {
                                    viewLifecycleOwner.lifecycleScope.launch {
                                        bookmarkViewModel.toggleBookmark(
                                            currentWeatherItem,
                                            isFavorite
                                        ).collect { newIsFavorite ->
                                            binding.imgFavorite.setImageResource(
                                                if (newIsFavorite) R.drawable.bookmark else R.drawable.bookmark_remove
                                            )

                                            val favouriteFragment = FavouriteFragment()
                                            val transaction =
                                                parentFragmentManager.beginTransaction()
                                            transaction.replace(
                                                R.id.fragment_container,
                                                favouriteFragment
                                            )
                                            transaction.addToBackStack(null)
                                            transaction.commit()
                                            dismiss()
                                        }
                                    }
                                }
                            }
                    }


                } else {
                    // Handle no data case
                }
            }
        }
    }
}