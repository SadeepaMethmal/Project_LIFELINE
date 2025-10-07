package com.example.lifeline

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onDelete: (Reminder, Int) -> Unit // callback to Hydration
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.textView32)
        val daysText: TextView = itemView.findViewById(R.id.textView33)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.deleteReminder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        val formattedTime = formatTime(reminder.hour, reminder.minute)

        holder.timeText.text = formattedTime
        holder.daysText.text = reminder.days

        holder.deleteBtn.setOnClickListener {
            val context = holder.itemView.context
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.custom_confirm_box3, null)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            val cancelButton = dialogView.findViewById<Button>(R.id.btnResetNo)
            val confirmButton = dialogView.findViewById<Button>(R.id.btnResetYes)

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            confirmButton.setOnClickListener {
                onDelete(reminder, position)
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()

            val window = dialog.window
            window?.setLayout(
                (context.resources.displayMetrics.widthPixels * 0.8).toInt(),  // 80% of screen width
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun getItemCount(): Int = reminders.size

    fun addReminder(reminder: Reminder) {
        reminders.add(reminder)
        notifyItemInserted(reminders.size - 1)
    }

    fun removeReminder(position: Int) {
        if (position >= 0 && position < reminders.size) {
            reminders.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "a.m" else "p.m"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val displayMinute = String.format("%02d", minute)
        return "$displayHour:$displayMinute $amPm"
    }
}
