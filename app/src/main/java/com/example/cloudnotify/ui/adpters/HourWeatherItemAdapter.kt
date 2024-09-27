package com.example.cloudnotify.ui.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudnotify.R
import com.example.cloudnotify.Utility.Converter
import com.example.cloudnotify.data.model.local.HourlyWeather
import com.example.cloudnotify.databinding.ViewholderHourlyBinding

class HourWeatherItemAdapter : RecyclerView.Adapter<HourWeatherItemAdapter.HourWeatherItemViewHolder>() {

    private var weather = listOf<HourlyWeather>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourWeatherItemViewHolder {
        val binding: ViewholderHourlyBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.viewholder_hourly, parent, false
        )
        return HourWeatherItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return weather.size
    }

    override fun onBindViewHolder(holder: HourWeatherItemViewHolder, position: Int) {
        val hourlyWeather = weather[position]
        holder.bind(hourlyWeather)
    }

    fun setList(hourlyWeatherList: List<HourlyWeather>) {
        this.weather = hourlyWeatherList
        notifyDataSetChanged()
    }

    class HourWeatherItemViewHolder(private val binding: ViewholderHourlyBinding) : RecyclerView.ViewHolder(binding.root) {
        private val converter = Converter()
        fun bind(hourWeather: HourlyWeather) {
            binding.hourWeather = hourWeather
            val weatherIconRes = converter.getWeatherIconResource(hourWeather.icon)
            binding.imgWeather.setImageResource(weatherIconRes)
            binding.executePendingBindings()
        }
    }
}
