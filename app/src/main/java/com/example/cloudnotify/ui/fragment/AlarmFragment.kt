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
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudnotify.broadcastreceiver.BroadcastReceiver
import com.example.cloudnotify.R
import com.example.cloudnotify.data.local.db.AlertNotificationDao
import com.example.cloudnotify.data.local.db.WeatherDataBase
import com.example.cloudnotify.data.model.local.AlertNotification
import com.example.cloudnotify.data.repo.ALertNotificationRepo
import com.example.cloudnotify.databinding.FragmentAlarmBinding
import com.example.cloudnotify.ui.adapters.AlarmItemAdapter
import com.example.cloudnotify.ui.adapters.DeleteAlarmListener
import com.example.cloudnotify.viewmodel.AlarmViewModel.AlarmViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar


@AndroidEntryPoint
class AlarmFragment : Fragment() , DeleteAlarmListener {

    private val sharedPreferencesName = "alert_preferences"  // Name for shared preferences to store alert data
    private val requestCodeKey = "request_code"  // Key for saving and retrieving the request code
    var requestCode: Int = 0  // Request code to differentiate between alarms/notifications
    private val notificationChannelId = "channel_id"  // Notification channel ID for Android O+ notifications
    private lateinit var alertItemAdapter: AlarmItemAdapter
    private lateinit var binding: FragmentAlarmBinding
    private  val alarmViewModel: AlarmViewModel by viewModels()


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
        binding  = FragmentAlarmBinding.inflate(inflater, container, false)

        // Retrieve request code from shared preferences
        requestCode = getRequestCodeFromPreferences()

        // Create notification channel for Android O+
        createNotificationChannel()

        // Set up a button listener for adding alerts
        val fabAddAlert: ExtendedFloatingActionButton = binding.btnAddAlarm
        fabAddAlert.setOnClickListener {
            showAlertDialog()  // Show date and time picker dialog
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertItemAdapter= AlarmItemAdapter(this)
        binding.recyclerView.layoutManager=
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
        binding.recyclerView.adapter=alertItemAdapter
        observeBookmarkList()

    }
    private fun observeBookmarkList() {
        lifecycleScope.launch {
            alarmViewModel.alertNotifications.collect { alarms ->
                // Update the UI with the alarm
                alertItemAdapter.updateData(alarms)
            }
        }

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
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_alarm, null)

        val titleInput = dialogView.findViewById<EditText>(R.id.title_input)

        builder.setView(dialogView)
            .setTitle("Set Alarm or Notification")
            .setPositiveButton("Next") { _, _ ->
                val title = titleInput.text.toString()
                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    // Proceed to select date and time
                    showDateTimePicker(title)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDateTimePicker(title: String) {
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

                        // Proceed to set the alarm or notification with the provided title
                        showTypeDialog(calendar,title)
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
    private fun showTypeDialog(calendar: Calendar, title: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Type")

        val types = arrayOf("Alarm", "Notification")
        builder.setItems(types) { _, which ->
            updateRequestCode()
            when (which) {
                0 -> checkAlarmPermission(calendar, title)
                1 -> checkNotificationPermission(calendar, title)
            }
        }
        builder.show()
    }


    // Check if the app has permission to set exact alarms (required for Android S+)
    private fun checkAlarmPermission(calendar: Calendar, title: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestExactAlarmPermission(calendar, title)  // Request exact alarm permission if needed
        } else {
            setAlarm(calendar, title)  // Otherwise, set the alarm directly
        }
    }

    // Check if the app has permission to post notifications (required for Android T+)
    @SuppressLint("NewApi")
    private fun checkNotificationPermission(calendar: Calendar, title: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission(calendar, title)  // Request notification permission if needed
        } else {
            showNotification(calendar, title)  // Otherwise, show the notification directly
        }
    }

    // Set an alarm at the specified time
    private fun setAlarm(calendar: Calendar, title: String) {
        // Ensure the selected time is in the future
        if (calendar.before(Calendar.getInstance())) {
            Toast.makeText(requireContext(), "Cannot set alarm for past time!", Toast.LENGTH_SHORT).show()
            return
        }

        val alarmTimeInMillis = calendar.timeInMillis
        Log.d("AlarmTime", "Setting alarm for: $alarmTimeInMillis (${calendar.time}) with title $title")

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), BroadcastReceiver::class.java)

        // Pass the scheduled time and title
        intent.putExtra("SCHEDULED_TIME", alarmTimeInMillis)
        intent.putExtra("ALARM_TITLE", title)
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
            //save in database
            val alertNotification = AlertNotification(
                getRequestCodeFromPreferences(),
                title,
                alarmTimeInMillis,
                "Alarm"
            )

            alarmViewModel.insertAlertNotification(alertNotification)
            Log.i("Alarm Fragment", "setAlarm: "+alertNotification.id)

            Toast.makeText(requireContext(), "Alarm Set Successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to set alarm: ${e.message}", Toast.LENGTH_SHORT).show()
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
    private fun showNotification(calendar: Calendar, title: String) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), BroadcastReceiver::class.java)

        // Intent for triggering the notification via BroadcastReceiver
        intent.action = "Notification"
        intent.putExtra("ALARM_TITLE", title)
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
        //saved in database


        Toast.makeText(requireContext(), "Notification Scheduled!", Toast.LENGTH_SHORT).show()

    }
//delete from database and alarm manager



    // Request permission to set exact alarms (Android S+)
    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission(calendar: Calendar, title: String) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)  // Direct the user to the settings to allow exact alarms
        } else {
            setAlarm(calendar, title)  // Set the alarm if permission is already granted
        }
    }

    // Request permission to post notifications (Android T+)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission(calendar: Calendar,title: String) {
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
            showNotification(calendar,title)  // Show the notification if permission is already granted
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

    override fun deleteAlarm(alarm: AlertNotification) {

            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(requireContext(), BroadcastReceiver::class.java)

            intent.action = "Alarm"

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                alarm.id,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            // Check if the PendingIntent exists, meaning the alarm is set
            if (pendingIntent != null) {
                // Cancel the alarm
                alarmManager.cancel(pendingIntent)
                Toast.makeText(requireContext(), "Alarm deleted successfully", Toast.LENGTH_SHORT).show()

                // Optionally remove the alarm from the database as well
                alarmViewModel.deleteAlertNotificationById( alarm.id)  // Ensure this method is implemented in your ViewModel
            } else {
                Toast.makeText(requireContext(), "No alarm found with request code: ${alarm.id}", Toast.LENGTH_SHORT).show()
            }
        }
    }

