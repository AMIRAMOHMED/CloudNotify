package com.example.cloudnotify.viewmodel.AlarmViewModel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudnotify.data.model.local.AlertNotification
import com.example.cloudnotify.data.repo.ALertNotificationRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class AlarmViewModel(private val alertNotificationRepo: ALertNotificationRepo) : ViewModel() {

    // MutableStateFlow to hold the list of notifications
    private val _alertNotifications = MutableStateFlow<List<AlertNotification>>(emptyList())
    val alertNotifications: StateFlow<List<AlertNotification>> = _alertNotifications.asStateFlow()

    init {
        getAllAlertNotifications()
    }

    fun getAllAlertNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            alertNotificationRepo.getAllAlertNotifications().collect { notifications ->
                // Current time in milliseconds
                val currentTime = System.currentTimeMillis()

                // Separate lists for filtering and deleting
                val filteredNotifications = mutableListOf<AlertNotification>()

                notifications.forEach { notification ->
                    if (notification.calendar > currentTime) {
                        // If the notification is in the future, add it to the filtered list
                        filteredNotifications.add(notification)
                    } else {
                        // If the notification is in the past, delete it from the database
                        deleteAlertNotificationById(notification.id)
                    }
                }

                // Update the StateFlow with filtered notifications
                _alertNotifications.value = filteredNotifications
            }
        }
    }

    fun insertAlertNotification(alertNotification: AlertNotification) {
        viewModelScope.launch(Dispatchers.IO) {
            alertNotificationRepo.insertAlertNotification(alertNotification)
        }
    }

    fun deleteAlertNotificationById(alertId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            alertNotificationRepo.deleteAlertNotificationById(alertId)
        }
    }


}