package com.example.lifeline

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission

class ReminderReceiver : BroadcastReceiver() {

    /*
     * Called automatically by the Android system when the scheduled alarm fires.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        // Ensure notification channel exists (required for Android 8.0+)
        NotificationUtils.ensureChannel(context)

        // Retrieve reminder details from intent extras
        val title = intent.getStringExtra("title") ?: "Hydration Reminder"
        val message = intent.getStringExtra("message") ?: "💧 It's time to drink water!"
        val id = intent.getIntExtra("id", System.currentTimeMillis().toInt())

        // Display the reminder notification
        NotificationUtils.show(context, title, message, id)
    }
}
