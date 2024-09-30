package com.example.cloudnotify.Utility


import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieAnimationView
import com.example.cloudnotify.R

class DialogUtils {

    companion object {
        fun showNoInternetDialog(context: Context) {
            val builder = AlertDialog.Builder(context)
            val dialogView = View.inflate(context, R.layout.dialog_no_internet, null)

            // Find Lottie animation view in the dialog layout
            val lottieAnimationView: LottieAnimationView = dialogView.findViewById(R.id.lottie_no_internet)

            builder.setView(dialogView)
            builder.setCancelable(true)

            // Start Lottie animation
            lottieAnimationView.playAnimation()

            // Show dialog
            val dialog = builder.create()
            dialog.show()
        }
    }
}