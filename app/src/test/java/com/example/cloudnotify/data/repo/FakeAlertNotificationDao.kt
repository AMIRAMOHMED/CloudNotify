package com.example.cloudnotify.data.repo

import com.example.cloudnotify.data.local.db.AlertNotificationDao
import com.example.cloudnotify.data.model.local.AlertNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAlertNotificationDao : AlertNotificationDao {
    private val alertList = mutableListOf<AlertNotification>()
    private val alertFlow = MutableStateFlow<List<AlertNotification>>(emptyList())

    override fun insertAlertNotification(alertNotification: AlertNotification) {
        alertList.add(alertNotification)
        alertFlow.value = alertList
    }

    override fun getAllAlertNotifications(): Flow<List<AlertNotification>> {
        return alertFlow.asStateFlow()
    }

    override fun deleteAlertNotificationById(alertId: Int) {
        alertList.removeIf { it.id == alertId }
        alertFlow.value = alertList
    }

    fun clear() {
        alertList.clear() // Reset the state
    }
}