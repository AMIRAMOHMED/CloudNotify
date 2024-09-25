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
import com.example.cloudnotify.BroadcastReceiver
import com.example.cloudnotify.R
import com.example.cloudnotify.receivers.OverlayService
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

import java.util.Calendar

class AlarmFragment : Fragment() {
    private val sharedPreferencesName = "alert_preferences"
    private val requestCodeKey = "request_code"
    var requestCode: Int = 0
    private val notificationChannelId = "channel_id"

    companion object {
        private const val REQUEST_CODE_OVERLAY_PERMISSION = 1001
        private const val REQUEST_CODE_NOTIFICATION_PERMISSION = 1002
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)
        requestCode = getRequestCodeFromPreferences()
        createNotificationChannel()

        val fabAddAlert: ExtendedFloatingActionButton = view.findViewById(R.id.btn_add_alarm)
        fabAddAlert.setOnClickListener {
            showAlertDialog()
        }

        return view
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Weather Alert Notifications"
            }
            val notificationManager =
                requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAlertDialog() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTypeDialog(calendar: Calendar) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Type")

        val types = arrayOf("Alarm", "Notification")
        builder.setItems(types) { _, which ->
            updateRequestCode()
            when (which) {
                0 -> checkAlarmPermission(calendar)
                1 -> checkNotificationPermission(calendar)
            }
        }
        builder.show()
    }

    private fun checkAlarmPermission(calendar: Calendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestExactAlarmPermission(calendar)
        } else {
            setAlarm(calendar)
        }
    }

    @SuppressLint("NewApi")
    private fun checkNotificationPermission(calendar: Calendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission(calendar)
        } else {
            showNotification(calendar)
        }
    }

    private fun setAlarm(calendar: Calendar) {
        // Ensure the selected time is in the future
        if (calendar.before(Calendar.getInstance())) {
            Toast.makeText(requireContext(), "Cannot set alarm for past time!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val alarmTimeInMillis = calendar.timeInMillis
        Log.d("AlarmTime", "Setting alarm for: $alarmTimeInMillis (${calendar.time})")

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), BroadcastReceiver::class.java)

        // Pass the scheduled time
        // intent.putExtra("SCHEDULED_TIME", alarmTimeInMillis)
        intent.action = "Alarm"
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            getRequestCodeFromPreferences(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Set the alarm
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTimeInMillis,
                pendingIntent
            )

            // Check Overlay Permission
            checkOverlayPermission()

            Toast.makeText(requireContext(), "Alarm Set Successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Failed to set alarm: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("AlarmError", "Error setting alarm", e)
        }
    }


    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(requireContext())) {
            // Request the Overlay permission
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}")
            )
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
        }
    }


    private fun showNotification(calendar: Calendar) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), OverlayService::class.java)


        intent.action = "Notification"
        // Create a PendingIntent for the AlarmReceiver
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            getRequestCodeFromPreferences(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm to trigger the notification at the selected time
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Toast.makeText(requireContext(), "Notification Scheduled!", Toast.LENGTH_SHORT).show()
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission(calendar: Calendar) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        } else {
            setAlarm(calendar)
        }
    }




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
            showNotification(calendar)
        }
    }

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
                    Toast.makeText(
                        requireContext(),
                        "Notification Permission Granted!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Notification Permission Denied!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getRequestCodeFromPreferences(): Int {
        val sharedPrefs =
            requireActivity().getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        return sharedPrefs.getInt(requestCodeKey, 0) // Default value is 0
    }


    private fun updateRequestCode() {

        requestCode++

        val sharedPrefs =
            requireActivity().getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putInt(requestCodeKey, requestCode)
            apply() // Commit changes asynchronously
        }
    }
}