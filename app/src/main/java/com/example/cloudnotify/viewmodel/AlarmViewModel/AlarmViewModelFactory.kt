package com.example.cloudnotify.viewmodel.AlarmViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cloudnotify.data.repo.ALertNotificationRepo

class AlarmViewModelFactory(private val alertNotificationRepo: ALertNotificationRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(alertNotificationRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}