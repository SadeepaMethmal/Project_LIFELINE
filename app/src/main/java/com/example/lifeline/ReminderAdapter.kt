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
    private val onDelete: (Reminder, Int) -> Unit // Callback function to Hydration Fragment
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    // -------------------------------------------------------------------------
    // VIEW HOLDER
    // -------------------------------------------------------------------------

    /*
     * ViewHolder class representing a single reminder card.
     */
    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.textView32)
        val daysText: TextView = itemView.findViewById(R.id.textView33)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.deleteReminder)
    }

    // -------------------------------------------------------------------------
    // ADAPTER METHODS
    // -------------------------------------------------------------------------

    /*
     * Inflates the layout for each reminder item (item_reminder.xml)
     * and creates a corresponding ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    /*
     * Binds data from a Reminder object to its corresponding ViewHolder.
     * Also handles delete button confirmation dialog logic.
     */
    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        val formattedTime = formatTime(reminder.hour, reminder.minute)

        // Display time and days
        holder.timeText.text = formattedTime
        holder.daysText.text = reminder.days

        // Handle delete button click
        holder.deleteBtn.setOnClickListener {
            val context = holder.itemView.context
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.custom_confirm_box3, null)

            // Create a custom confirmation dialog
            val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            // Set up dialog buttons
            val cancelButton = dialogView.findViewById<Button>(R.id.btnResetNo)
            val confirmButton = dialogView.findViewById<Button>(R.id.btnResetYes)

            cancelButton.setOnClickListener { dialog.dismiss() }

            confirmButton.setOnClickListener {
                // Invoke callback to delete reminder in Hydration fragment
                onDelete(reminder, position)
                dialog.dismiss()
            }

            // Customize and show dialog
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()

            dialog.window?.setLayout(
                (context.resources.displayMetrics.widthPixels * 0.8).toInt(), // 80% screen width
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    /*
     * Returns the total number of reminders in the list.
     */
    override fun getItemCount(): Int = reminders.size

    // -------------------------------------------------------------------------
    // DATA MANIPULATION METHODS
    // -------------------------------------------------------------------------

    /*
     * Adds a new reminder to the list and updates the RecyclerView.
     */
    fun addReminder(reminder: Reminder) {
        reminders.add(reminder)
        notifyItemInserted(reminders.size - 1)
    }

    /*
     * Removes a reminder at a specific position and updates the RecyclerView.
     */
    fun removeReminder(position: Int) {
        if (position in reminders.indices) {
            reminders.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // -------------------------------------------------------------------------
    // HELPER FUNCTIONS
    // -------------------------------------------------------------------------

    /*
     * Formats hour and minute into a 12-hour time string with a.m./p.m.
     * Example: 14:05 → "2:05 p.m"
     */
    @SuppressLint("DefaultLocale")
    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "a.m" else "p.m"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val displayMinute = String.format("%02d", minute)
        return "$displayHour:$displayMinute $amPm"
    }
}
