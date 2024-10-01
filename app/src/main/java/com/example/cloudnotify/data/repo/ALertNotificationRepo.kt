package com.example.cloudnotify.data.repo

import com.example.cloudnotify.data.local.db.AlertNotificationDao
import com.example.cloudnotify.data.model.local.AlertNotification
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ALertNotificationRepo  @Inject constructor(
    private val alertNotificationDao: AlertNotificationDao
) {
    fun insertAlertNotification(alertNotification: AlertNotification) {
        alertNotificationDao.insertAlertNotification(alertNotification)
    }
    fun getAllAlertNotifications(): Flow<List<AlertNotification>> {
        return alertNotificationDao.getAllAlertNotifications()
    }
    fun deleteAlertNotificationById(alertId: Int) {
        alertNotificationDao.deleteAlertNotificationById(alertId)
    }



}