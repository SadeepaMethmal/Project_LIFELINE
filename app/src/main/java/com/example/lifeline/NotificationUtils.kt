package com.example.lifeline

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationUtils {

    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------
    const val CHANNEL_ID = "hydration_reminders"
    private const val CHANNEL_NAME = "Hydration Reminders"
    private const val CHANNEL_DESC = "Reminds you to drink water regularly"

    // -------------------------------------------------------------------------
    // CHANNEL MANAGEMENT
    // -------------------------------------------------------------------------

    /*
     * Ensures that a notification channel exists.
     */
    fun ensureChannel(context: Context) {
        // Only required on Android 8.0+ (Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // -------------------------------------------------------------------------
    // SHOW NOTIFICATION
    // -------------------------------------------------------------------------

    /*
     * Displays a notification to the user.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun show(context: Context, title: String, message: String, notificationId: Int) {
        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_lifeline)   // Small app logo icon
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)                      // Dismiss when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Display the notification using NotificationManagerCompat
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
