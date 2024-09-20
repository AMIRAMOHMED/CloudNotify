package com.example.cloudnotify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudnotify.data.local.WeatherDao
import com.example.cloudnotify.data.local.WeatherDataBase
import com.example.cloudnotify.data.repo.WeatherRepository
import com.example.cloudnotify.databinding.FragmentHomeBinding
import com.example.cloudnotify.ui.adapters.HourWeatherItemAdapter
import com.example.cloudnotify.Utility.NetworkUtils
import com.example.cloudnotify.viewmodel.HomeViewModel.HomeVeiwModel
import com.example.cloudnotify.viewmodel.HomeViewModel.HomeViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var weatherRepo: WeatherRepository
    private lateinit var networkUtils: NetworkUtils
    private lateinit var weatherDao: WeatherDao
    private lateinit var hourWeatherAdapter: HourWeatherItemAdapter
    private lateinit var homeViewModel: HomeVeiwModel
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
            networkUtils
        )

        // You may initialize ViewModel here or in onViewCreated
        homeViewModelFactory = HomeViewModelFactory(weatherRepo)
        homeViewModel = homeViewModelFactory.create(HomeVeiwModel::class.java)
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
        binding.dayRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.dayRecyclerView.adapter = hourWeatherAdapter

        // Fetch and observe weather data in an IO thread
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                weatherRepo.getWeatherData().collect { weatherData ->
                    withContext(Dispatchers.Main) {
                        hourWeatherAdapter.setList(weatherData.hourlyWeather)
                    }
                }
            }
        }
    }
}