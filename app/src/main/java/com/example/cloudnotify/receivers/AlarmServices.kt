package com.example.cloudnotify.receivers
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.cloudnotify.R
import com.example.cloudnotify.databinding.FragmentAlarmDialogBinding
import com.example.cloudnotify.ui.activity.MainActivity



class AlarmServices : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var alarmView: View
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding : FragmentAlarmDialogBinding

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        alarmView = LayoutInflater.from(this).inflate(R.layout.fragment_alarm_dialog, null)

        binding = FragmentAlarmDialogBinding.bind(alarmView)

        binding.stopButton.setOnClickListener {
            stopSelf()
        }

        binding.snoozeButton.setOnClickListener {
            stopSelf()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        params.let {
            it.gravity= Gravity.TOP
        }
        // insert the view into the window and then display it
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(alarmView, params)

        // open the media player and start looping
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer.create(this, notificationUri)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        alarmView.setOnClickListener {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::alarmView.isInitialized) {
            windowManager.removeView(alarmView)
        }

        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
}
