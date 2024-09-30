package com.example.cloudnotify.data.model.local

import androidx.room.Entity
import java.util.*

@Entity(tableName = "alert_notification", primaryKeys = ["id", "type"])
data class AlertNotification(
    val id: Int,
    val title: String,
    val calendar: Long,
    val type: String // "Alarm" or "Notification"
)
