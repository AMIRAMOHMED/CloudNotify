package com.example.cloudnotify.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudnotify.Utility.Converter
import com.example.cloudnotify.Utility.NetworkUtils
import com.example.cloudnotify.data.local.db.WeatherDao
import com.example.cloudnotify.data.local.db.WeatherDataBase
import com.example.cloudnotify.data.repo.WeatherRepository
import com.example.cloudnotify.databinding.FragmentDetailscreenBinding
import com.example.cloudnotify.ui.adapters.DailyWeatherItemAdapter
import com.example.cloudnotify.ui.adapters.HourWeatherItemAdapter
import com.example.cloudnotify.viewmodel.HomeViewModel.HomeViewModel
import com.example.cloudnotify.viewmodel.LocationViewModel
import com.example.cloudnotify.wrapper.WeatherDataState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class Detailscreen : Fragment() {
    private lateinit var binding: FragmentDetailscreenBinding

    private lateinit var hourWeatherAdapter: HourWeatherItemAdapter
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var  dailyWeatherItemAdapter: DailyWeatherItemAdapter
    private val converter = Converter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailscreenBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Initialize RecyclerView and adapter with binding
        hourWeatherAdapter = HourWeatherItemAdapter()
        dailyWeatherItemAdapter= DailyWeatherItemAdapter()

        binding.hourRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.hourRecyclerView.adapter = hourWeatherAdapter
        binding.dayRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.dayRecyclerView.adapter = dailyWeatherItemAdapter
        observeWeatherData()
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


}