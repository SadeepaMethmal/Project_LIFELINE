package com.example.lifeline

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtils.ensureChannel(context)

        val title = intent.getStringExtra("title") ?: "Hydration"
        val message = intent.getStringExtra("message") ?: "💧 Time to drink water!"
        val id = intent.getIntExtra("id", System.currentTimeMillis().toInt())

        NotificationUtils.show(context, title, message, id)
    }
}
