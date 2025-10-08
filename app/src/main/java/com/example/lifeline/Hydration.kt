package com.example.lifeline

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import java.util.*

class Hydration : Fragment(R.layout.fragment_hydration) {

    // -------------------------------------------------------------------------
    // ANIMATIONS
    // -------------------------------------------------------------------------
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom_anim) }
    private var fabMenuOpen = false

    // -------------------------------------------------------------------------
    // VARIABLES
    // -------------------------------------------------------------------------
    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<Reminder>()

    // Request notification permission (Android 13+)
    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* No result handling needed */ }

    // -------------------------------------------------------------------------
    // LIFECYCLE
    // -------------------------------------------------------------------------
    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ensureNotificationPermission()

        // UI references
        val addBtn: FloatingActionButton = view.findViewById(R.id.addReminderBtn)
        val deleteAllBtn: FloatingActionButton = view.findViewById(R.id.deleteAllBtn)
        val mainFab: FloatingActionButton = view.findViewById(R.id.editReminderBtn)
        val recycler: RecyclerView = view.findViewById(R.id.reminderRecyclerView)
        val emptyState: View = view.findViewById(R.id.emptyStateLayout)

        // RecyclerView setup
        adapter = ReminderAdapter(reminders) { reminder, position ->
            val alarmId = reminder.hour * 100 + reminder.minute
            cancelReminder(requireContext(), alarmId)
            adapter.removeReminder(position)
            saveReminders(reminders)
            emptyState.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Load saved reminders
        loadReminders()?.let {
            reminders.addAll(it)
            adapter.notifyDataSetChanged()
        }

        emptyState.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE

        // FAB click listeners
        mainFab.setOnClickListener { toggleFabMenu(addBtn, deleteAllBtn, mainFab) }
        addBtn.setOnClickListener { showTimePicker(emptyState) }
        deleteAllBtn.setOnClickListener { clearAllReminders(emptyState) }
    }

    // -------------------------------------------------------------------------
    // FLOATING ACTION BUTTON MENU BEHAVIOR
    // -------------------------------------------------------------------------
    /* Expands or collapses the FAB menu with animation. */
    private fun toggleFabMenu(addBtn: View, deleteBtn: View, mainBtn: View) {
        setFabVisibility(fabMenuOpen, addBtn, deleteBtn)
        setFabAnimation(fabMenuOpen, addBtn, deleteBtn, mainBtn)
        setFabClickable(fabMenuOpen, addBtn, deleteBtn)
        fabMenuOpen = !fabMenuOpen
    }

    private fun setFabVisibility(open: Boolean, addBtn: View, deleteBtn: View) {
        addBtn.visibility = if (!open) View.VISIBLE else View.INVISIBLE
        deleteBtn.visibility = if (!open) View.VISIBLE else View.INVISIBLE
    }

    private fun setFabAnimation(open: Boolean, addBtn: View, deleteBtn: View, mainBtn: View) {
        if (!open) {
            addBtn.startAnimation(fromBottom)
            deleteBtn.startAnimation(fromBottom)
            mainBtn.startAnimation(rotateOpen)
        } else {
            addBtn.startAnimation(toBottom)
            deleteBtn.startAnimation(toBottom)
            mainBtn.startAnimation(rotateClose)
        }
    }

    private fun setFabClickable(open: Boolean, addBtn: View, deleteBtn: View) {
        addBtn.isClickable = !open
        deleteBtn.isClickable = !open
    }

    // -------------------------------------------------------------------------
    // REMINDER CREATION / DELETION
    // -------------------------------------------------------------------------

    /* Displays a TimePicker dialog and schedules a reminder. */
    private fun showTimePicker(emptyState: View) {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val reminder = Reminder(hour = selectedHour, minute = selectedMinute)
                reminders.add(reminder)
                adapter.notifyItemInserted(reminders.size - 1)
                saveReminders(reminders)
                emptyState.visibility = View.GONE

                val alarmId = selectedHour * 100 + selectedMinute
                scheduleDailyReminder(requireContext(), selectedHour, selectedMinute, alarmId)
            },
            hour, minute, false
        ).show()
    }

    /* Clears all reminders after confirmation dialog. */
    private fun clearAllReminders(emptyState: View) {
        val context = emptyState.context
        val view = LayoutInflater.from(context).inflate(R.layout.custom_confirm_box3, null)
        val dialog = AlertDialog.Builder(context).setView(view).create()

        val confirm = view.findViewById<Button>(R.id.btnResetYes)
        val cancel = view.findViewById<Button>(R.id.btnResetNo)

        cancel.setOnClickListener { dialog.dismiss() }

        confirm.setOnClickListener {
            reminders.clear()
            adapter.notifyDataSetChanged()
            saveReminders(reminders)
            emptyState.visibility = View.VISIBLE
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    // -------------------------------------------------------------------------
    // SHARED PREFERENCES (DATA PERSISTENCE)
    // -------------------------------------------------------------------------
    /* Saves all reminders as a JSON array in SharedPreferences. */
    private fun saveReminders(reminders: List<Reminder>) {
        val prefs = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        val json = Gson().toJson(reminders)
        prefs.edit { putString("reminders", json) }
    }

    /* Loads saved reminders from SharedPreferences. */
    private fun loadReminders(): List<Reminder>? {
        val prefs = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("reminders", null)
        return json?.let { Gson().fromJson(it, Array<Reminder>::class.java).toList() }
    }

    // -------------------------------------------------------------------------
    // ALARM MANAGEMENT
    // -------------------------------------------------------------------------
    /*
     * Schedules an exact daily alarm for a specific time using AlarmManager.
     * Also schedules a next-day duplicate to keep it recurring.
     */
    private fun scheduleDailyReminder(context: Context, hour: Int, minute: Int, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12+ exact alarm permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            NotificationUtils.ensureChannel(context)
            NotificationUtils.show(
                context, "Alarm Permission Needed",
                "Enable 'Alarms & reminders' in Settings > Apps > Lifeline.",
                9998
            )
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Hydration Reminder")
            putExtra("message", "💧 Time to drink water!")
            putExtra("id", id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            }
            scheduleNextDayAlarm(context, hour, minute, id)
        } catch (e: SecurityException) {
            NotificationUtils.ensureChannel(context)
            NotificationUtils.show(context, "Alarm Permission Denied",
                "Cannot schedule alarms. Check app permissions.", 9997)
        }
    }

    /* Schedules a backup alarm for the next day to ensure recurrence. */
    private fun scheduleNextDayAlarm(context: Context, hour: Int, minute: Int, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Hydration Reminder")
            putExtra("message", "💧 Time to drink water!")
            putExtra("id", id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, id + 1000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            }
        } catch (_: SecurityException) { /* Silent fail for next-day alarm */ }
    }

    /* Cancels a scheduled reminder (current + next day). */
    private fun cancelReminder(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)

        val todayPI = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextDayPI = PendingIntent.getBroadcast(
            context, id + 1000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(todayPI)
        alarmManager.cancel(nextDayPI)
    }

    // -------------------------------------------------------------------------
    // PERMISSIONS
    // -------------------------------------------------------------------------
    /*Requests POST_NOTIFICATIONS permission on Android 13+. */
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
