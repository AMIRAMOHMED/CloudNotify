package com.example.cloudnotify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cloudnotify.R
import com.example.cloudnotify.data.local.WeatherDao
import com.example.cloudnotify.network.WeatherService
import com.example.cloudnotify.data.repo.WeatherRepository
import com.example.cloudnotify.Utility.NetworkUtils
import com.example.cloudnotify.data.local.WeatherDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var weatherRepo: WeatherRepository
    private lateinit var networkUtils: NetworkUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize your dependencies here

        val dao = WeatherDataBase.getInstance(requireActivity()).weatherDao


            networkUtils = NetworkUtils(requireContext()) // Pass context for network utils

        // Initialize the repository by passing the dependencies
        weatherRepo = WeatherRepository(
            dao,
            networkUtils
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch and observe weather data in an IO thread
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                weatherRepo.getWeatherData().collect { weatherData ->
                    withContext(Dispatchers.Main) {
                        // Update your UI here with the collected weather data
                    }
                }
            }
        }
    }
}