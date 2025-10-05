package com.example.lifeline

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission

class ReminderReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtils.ensureChannel(context)

        val title = intent.getStringExtra("title") ?: "Hydration"
        val message = intent.getStringExtra("message") ?: "💧 Time to drink water!"
        val id = intent.getIntExtra("id", System.currentTimeMillis().toInt())

        NotificationUtils.show(context, title, message, id)
    }
}
