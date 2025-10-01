package com.example.lifeline

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.util.*

class Hydration : Fragment(R.layout.fragment_hydration) {

    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<Reminder>()

    // Launcher for Android 13+ notification permission
    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ensureNotificationPermission()

        val addReminderBtn: ImageButton = view.findViewById(R.id.addReminder)
        val recyclerView: RecyclerView = view.findViewById(R.id.reminderRecyclerView)

        adapter = ReminderAdapter(reminders) { reminder, position ->
            // Cancel alarm when delete pressed
            val alarmId = reminder.hour * 100 + reminder.minute
            cancelReminder(requireContext(), alarmId)

            // Remove from RecyclerView + storage
            adapter.removeReminder(position)
            saveReminders(reminders)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Load saved reminders
        loadReminders()?.let {
            reminders.addAll(it)
            adapter.notifyDataSetChanged()
        }

        addReminderBtn.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val reminder = Reminder(
                    hour = selectedHour,
                    minute = selectedMinute
                )
                adapter.addReminder(reminder)
                saveReminders(reminders)

                // Schedule notification
                val alarmId = selectedHour * 100 + selectedMinute // simple unique ID
                scheduleDailyReminder(requireContext(), selectedHour, selectedMinute, alarmId)
            },
            hour,
            minute,
            false
        )
        timePicker.show()
    }

    // Save list of reminders
    private fun saveReminders(reminders: List<Reminder>) {
        val sharedPref = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        val json = Gson().toJson(reminders)
        sharedPref.edit { putString("reminders", json) }
    }

    // Load list of reminders
    private fun loadReminders(): List<Reminder>? {
        val sharedPref = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("reminders", null)
        return if (json != null) Gson().fromJson(json, Array<Reminder>::class.java).toList() else null
    }

    // Schedule exact daily reminder
    private fun scheduleDailyReminder(context: Context, hour: Int, minute: Int, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Hydration Reminder")
            putExtra("message", "💧 Time to drink water!")
            putExtra("id", id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
             AlarmManager.RTC_WAKEUP,
             cal.timeInMillis,
             AlarmManager.INTERVAL_DAY,
             pendingIntent
        )
    }

    private fun cancelReminder(context: Context, id: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    // Ask for POST_NOTIFICATIONS permission on Android 13+
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
