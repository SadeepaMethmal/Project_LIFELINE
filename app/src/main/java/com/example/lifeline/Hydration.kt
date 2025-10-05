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
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import java.util.*

class Hydration : Fragment(R.layout.fragment_hydration) {

    // --------------------------------------------------
    // ANIMATIONS
    // --------------------------------------------------
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom_anim) }
    private var fabMenuOpen = false

    // --------------------------------------------------
    // VARIABLES
    // --------------------------------------------------
    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<Reminder>()

    // Notification permission launcher (Android 13+)
    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    // --------------------------------------------------
    // LIFECYCLE
    // --------------------------------------------------
    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ensureNotificationPermission()

        // UI Elements
        val addReminderBtn: FloatingActionButton = view.findViewById(R.id.addReminderBtn)
        val deleteAllBtn: FloatingActionButton = view.findViewById(R.id.deleteAllBtn)
        val editReminderBtn: FloatingActionButton = view.findViewById(R.id.editReminderBtn)
        val recyclerView: RecyclerView = view.findViewById(R.id.reminderRecyclerView)
        val emptyState: View = view.findViewById(R.id.emptyStateLayout)

        // Recycler setup
        adapter = ReminderAdapter(reminders) { reminder, position ->
            val alarmId = reminder.hour * 100 + reminder.minute
            cancelReminder(requireContext(), alarmId)
            adapter.removeReminder(position)
            saveReminders(reminders)
            emptyState.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Load saved reminders
        loadReminders()?.let {
            reminders.addAll(it)
            adapter.notifyDataSetChanged()
        }

        // Handle empty state
        emptyState.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE

        // FAB actions
        editReminderBtn.setOnClickListener {
            toggleFabMenu(addReminderBtn, deleteAllBtn, editReminderBtn)
        }

        addReminderBtn.setOnClickListener {
            showTimePicker(emptyState)
        }

        deleteAllBtn.setOnClickListener {
            clearAllReminders(emptyState)
        }
    }

    // --------------------------------------------------
    // FAB MENU BEHAVIOR
    // --------------------------------------------------
    private fun toggleFabMenu(addBtn: View, deleteBtn: View, mainBtn: View) {
        setFabVisibility(fabMenuOpen, addBtn, deleteBtn)
        setFabAnimation(fabMenuOpen, addBtn, deleteBtn, mainBtn)
        setFabClickable(fabMenuOpen, addBtn, deleteBtn)
        fabMenuOpen = !fabMenuOpen
    }

    private fun setFabVisibility(open: Boolean, addBtn: View, deleteBtn: View) {
        if (!open) {
            addBtn.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE
        } else {
            addBtn.visibility = View.INVISIBLE
            deleteBtn.visibility = View.INVISIBLE
        }
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

    // --------------------------------------------------
    // REMINDER HANDLING
    // --------------------------------------------------
    private fun showTimePicker(emptyState: View) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

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
            currentHour,
            currentMinute,
            false
        ).show()
    }

    private fun clearAllReminders(emptyState: View) {
        reminders.clear()
        adapter.notifyDataSetChanged()
        saveReminders(reminders)
        emptyState.visibility = View.VISIBLE
    }

    // --------------------------------------------------
    // SHARED PREFERENCES
    // --------------------------------------------------
    private fun saveReminders(reminders: List<Reminder>) {
        val prefs = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        val json = Gson().toJson(reminders)
        prefs.edit { putString("reminders", json) }
    }

    private fun loadReminders(): List<Reminder>? {
        val prefs = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("reminders", null)
        return json?.let {
            Gson().fromJson(it, Array<Reminder>::class.java).toList()
        }
    }

    // --------------------------------------------------
    // ALARM MANAGEMENT
    // --------------------------------------------------
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
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
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

    // --------------------------------------------------
    // PERMISSIONS
    // --------------------------------------------------
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
