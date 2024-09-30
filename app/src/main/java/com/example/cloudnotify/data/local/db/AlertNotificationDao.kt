package com.example.cloudnotify.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cloudnotify.data.model.local.AlertNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlertNotification(alertNotification: AlertNotification)

    @Query("SELECT * FROM alert_notification")
    fun getAllAlertNotifications(): Flow<List<AlertNotification>>


    @Query("DELETE FROM alert_notification WHERE id = :alertId")
    fun deleteAlertNotificationById(alertId: Int)
}