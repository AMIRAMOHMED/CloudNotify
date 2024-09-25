package com.example.cloudnotify.ui.fragment
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog

import android.util.Log
import com.example.cloudnotify.broadcastreceiver.BroadcastReceiver
import com.example.cloudnotify.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

import java.util.Calendar


class AlarmFragment : Fragment() {
    private val sharedPreferencesName = "alert_preferences"  // Name for shared preferences to store alert data
    private val requestCodeKey = "request_code"  // Key for saving and retrieving the request code
    var requestCode: Int = 0  // Request code to differentiate between alarms/notifications
    private val notificationChannelId = "channel_id"  // Notification channel ID for Android O+ notifications

    companion object {
        private const val REQUEST_CODE_OVERLAY_PERMISSION = 1001  // Request code for overlay permission
        private const val REQUEST_CODE_NOTIFICATION_PERMISSION = 1002  // Request code for notification permission
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment's layout
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)

        // Retrieve request code from shared preferences
        requestCode = getRequestCodeFromPreferences()

        // Create notification channel for Android O+
        createNotificationChannel()

        // Set up a button listener for adding alerts
        val fabAddAlert: ExtendedFloatingActionButton = view.findViewById(R.id.btn_add_alarm)
        fabAddAlert.setOnClickListener {
            showAlertDialog()  // Show date and time picker dialog
        }

        return view
    }

    // Create a notification channel (required for Android O+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Weather Alerts",  // Name for the notification channel
                NotificationManager.IMPORTANCE_HIGH  // High importance for weather alerts
            ).apply {
                description = "Channel for Weather Alert Notifications"
            }
            val notificationManager =
                requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)  // Register the channel
        }
    }

    // Show the date picker and then time picker when adding a new alert
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAlertDialog() {
        val calendar = Calendar.getInstance()  // Initialize calendar with the current date and time

        // Show date picker dialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Set the selected date in the calendar
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Show time picker dialog
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        // Set the selected time in the calendar
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        // Show dialog to select the type of alert (Alarm or Notification)
                        showTypeDialog(calendar)
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Show a dialog to choose between setting an Alarm or a Notification
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTypeDialog(calendar: Calendar) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Type")  // Title of the dialog

        val types = arrayOf("Alarm", "Notification")  // Options to choose from
        builder.setItems(types) { _, which ->
            // Update the request code to ensure uniqueness
            updateRequestCode()

            // Call the appropriate method based on user selection
            when (which) {
                0 -> checkAlarmPermission(calendar)  // Handle Alarm
                1 -> checkNotificationPermission(calendar)  // Handle Notification
            }
        }
        builder.show()
    }

    // Check if the app has permission to set exact alarms (required for Android S+)
    private fun checkAlarmPermission(calendar: Calendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestExactAlarmPermission(calendar)  // Request exact alarm permission if needed
        } else {
            setAlarm(calendar)  // Otherwise, set the alarm directly
        }
    }

    // Check if the app has permission to post notifications (required for Android T+)
    @SuppressLint("NewApi")
    private fun checkNotificationPermission(calendar: Calendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission(calendar)  // Request notification permission if needed
        } else {
            showNotification(calendar)  // Otherwise, show the notification directly
        }
    }

    // Set an alarm at the specified time
    private fun setAlarm(calendar: Calendar) {
        // Ensure the selected time is in the future
        if (calendar.before(Calendar.getInstance())) {
            Toast.makeText(requireContext(), "Cannot set alarm for past time!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val alarmTimeInMillis = calendar.timeInMillis  // Get the alarm time in milliseconds
        Log.d("AlarmTime", "Setting alarm for: $alarmTimeInMillis (${calendar.time})")

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), BroadcastReceiver::class.java)

        // Intent for triggering the alarm via BroadcastReceiver
        intent.action = "Alarm"
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Schedule the exact alarm
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTimeInMillis,
                pendingIntent
            )

            // Check if the app has permission to show overlays (for showing UI elements over other apps)
            checkOverlayPermission()

            Toast.makeText(requireContext(), "Alarm Set Successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Handle any errors while setting the alarm
            Toast.makeText(
                requireContext(),
                "Failed to set alarm: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("AlarmError", "Error setting alarm", e)
        }
    }

    // Check if the app has permission to draw overlays (display over other apps)
    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(requireContext())) {
            // Request overlay permission if not granted
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}")
            )
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
        }
    }

    // Show a notification at the specified time
    private fun showNotification(calendar: Calendar) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), BroadcastReceiver::class.java)

        // Intent for triggering the notification via BroadcastReceiver
        intent.action = "Notification"
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the exact time for the notification
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Toast.makeText(requireContext(), "Notification Scheduled!", Toast.LENGTH_SHORT).show()

    }

    // Request permission to set exact alarms (Android S+)
    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission(calendar: Calendar) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)  // Direct the user to the settings to allow exact alarms
        } else {
            setAlarm(calendar)  // Set the alarm if permission is already granted
        }
    }

    // Request permission to post notifications (Android T+)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission(calendar: Calendar) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_NOTIFICATION_PERMISSION
            )
        } else {
            showNotification(calendar)  // Show the notification if permission is already granted
        }
    }

    // Handle the result of permission requests (for notifications)
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_NOTIFICATION_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Notify the user if notification permission is granted
                    Toast.makeText(
                        requireContext(),
                        "Notification Permission Granted!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Notify the user if notification permission is denied
                    Toast.makeText(
                        requireContext(),
                        "Notification Permission Denied!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Get the last saved request code from shared preferences (used to ensure unique alarms/notifications)
    private fun getRequestCodeFromPreferences(): Int {
        val sharedPreferences = requireContext().getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(requestCodeKey, 0)  // Default value is 0
    }

    // Increment and save the request code in shared preferences
    private fun updateRequestCode() {
        requestCode++
        val sharedPreferences = requireContext().getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(requestCodeKey, requestCode)
        editor.apply()  // Save the new request code
    }
}
