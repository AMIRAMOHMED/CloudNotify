package com.example.cloudnotify.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudnotify.R
import com.example.cloudnotify.Utility.Converter
import com.example.cloudnotify.data.model.local.DailyWeather
import com.example.cloudnotify.databinding.ViewholderDailyBinding

class DailyWeatherItemAdapter: RecyclerView.Adapter<DailyWeatherItemAdapter.DailyWeatherItemViewHolder>() {
    private var weather = listOf<DailyWeather>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherItemViewHolder {
        val binding: ViewholderDailyBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.viewholder_daily, parent, false
        )
        return DailyWeatherItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyWeatherItemViewHolder, position: Int) {
        val dailyWeather = weather[position]
        holder.bind(dailyWeather)
    }

    override fun getItemCount(): Int {
        return weather.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(dailyWeather: List<DailyWeather>) {
        this.weather = dailyWeather
        notifyDataSetChanged()
    }

    class DailyWeatherItemViewHolder(private val binding: ViewholderDailyBinding) : RecyclerView.ViewHolder(binding.root) {
        private val converter = Converter()

        fun bind(dailyWeather: DailyWeather) {
            binding.dailyWeather = dailyWeather
            val weatherIconRes = converter.getWeatherIconResource(dailyWeather.icon)

            binding.imgWeather.setImageResource(weatherIconRes)
            binding.executePendingBindings()
        }
    }
}