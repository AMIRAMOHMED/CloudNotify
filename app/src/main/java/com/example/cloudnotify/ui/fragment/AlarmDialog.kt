package com.example.cloudnotify.ui.fragment
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.cloudnotify.R
import com.example.cloudnotify.ui.activity.MainActivity


class AlarmDialog : DialogFragment() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alarm_dialog, container, false)

        val stopButton: Button = view.findViewById(R.id.stop_button)
        val goToAppButton: Button = view.findViewById(R.id.snooze_button)

        // Stop button listener
        stopButton.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            dismiss()
        }

        // Go to App button listener
        goToAppButton.setOnClickListener {
            // Open the app, no need to pass any extras, HomeFragment is loaded by default
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            dismiss()
        }


        return view
    }

}