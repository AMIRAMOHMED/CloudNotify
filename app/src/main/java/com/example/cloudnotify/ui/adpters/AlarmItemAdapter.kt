package com.example.cloudnotify.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudnotify.Utility.Converter
import com.example.cloudnotify.data.model.local.AlertNotification
import com.example.cloudnotify.data.model.local.BookmarkLocation
import com.example.cloudnotify.databinding.ViewholderAlarmBinding

class AlarmItemAdapter(    private val deleteAlarmListener :DeleteAlarmListener
) :
    RecyclerView.Adapter<AlarmItemAdapter.AlarmItemViewHolder>() {


    private  var alarms= listOf<AlertNotification>()


    // Inflate the layout and create a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ViewholderAlarmBinding.inflate(layoutInflater, parent, false)
        return AlarmItemViewHolder(binding)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: AlarmItemViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.bind(alarm) // Call bind method to bind the data
    }

    // Return the size of the dataset
    override fun getItemCount(): Int = alarms.size

   // Function to update data
        fun updateData(newList: List<AlertNotification>) {
            alarms = newList
            notifyDataSetChanged()
        }

    // Function to convert time (this can be customized as per your logic)
    private fun convertTime(time: Long): String {
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(time))
    }


    // ViewHolder class
    inner class AlarmItemViewHolder(private val binding: ViewholderAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: AlertNotification) {
            // Bind alarm data to views
            binding.alarm = alarm
            binding.txtTime.text = convertTime(alarm.calendar)
binding.icoRemove.setOnClickListener {
    deleteAlarmListener.deleteAlarm(alarm)
}
            // Execute pending bindings for live updates
            binding.executePendingBindings()
        }
    }
}
interface DeleteAlarmListener {
    fun deleteAlarm(alarm: AlertNotification)
}