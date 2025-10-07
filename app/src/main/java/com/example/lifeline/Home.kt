package com.example.lifeline

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pl.droidsonroids.gif.GifImageButton
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.components.XAxis
import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class Home : Fragment() {

    // ------------------------------------------------------------
    // LIFECYCLE
    // ------------------------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ------------------------------------------------------------
        // USER GREETING
        // ------------------------------------------------------------
        val userNameText = view.findViewById<TextView>(R.id.textView7)
        userNameText.text = getWelcomeMessage(requireContext())

        // ------------------------------------------------------------
        // TODAY'S MOOD DISPLAY
        // ------------------------------------------------------------
        val moodImage = view.findViewById<GifImageButton>(R.id.gif1)
        val moodText = view.findViewById<TextView>(R.id.textView44)
        showTodayMood(requireContext(), moodImage, moodText)

        // ------------------------------------------------------------
        // BAR CHART LOGIC
        // ------------------------------------------------------------
        val chart = view.findViewById<BarChart>(R.id.moodBarChart)
        val moods = MoodStorage.load(requireContext())

        if (moods.isEmpty()) {
            chart.setNoDataText("No mood data yet")
        } else {
            // Count how many times each mood appears
            val moodCounts = moods.groupingBy { it.name }.eachCount()

            // Convert map entries into BarEntries
            val entries = moodCounts.toList().mapIndexed { index, (moodName, count) ->
                BarEntry(index.toFloat(), count.toFloat())
            }

            // Create X-axis labels (these will appear under each bar)
            val labels = moodCounts.keys.toList()

            val dataSet = BarDataSet(entries, "").apply { // empty label hides legend title
                color = "#64B5F6".toColorInt()
                valueTextSize = 12f
            }

            val data = BarData(dataSet)

            chart.apply {
                this.data = data
                description.isEnabled = false
                description.textSize = 14f
                legend.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    valueFormatter = IndexAxisValueFormatter(labels)
                    setDrawAxisLine(true)
                    axisLineColor = Color.BLACK
                    axisLineWidth = 1f
                }

                axisLeft.apply {
                    granularity = 1f
                    setDrawGridLines(true)
                    setDrawAxisLine(true)
                    axisLineColor = Color.BLACK
                    axisLineWidth = 1f
                    textColor = Color.BLACK
                }

                animateY(1000)
                invalidate()
            }
        }


    }

    // ------------------------------------------------------------
    // UPDATE HOME DATA ON RESUME
    // ------------------------------------------------------------
    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onResume() {
        super.onResume()

        // HABIT COMPLETION
        val prefs = requireContext().getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
        val completionText = prefs.getString("task_completion", "0/0")
        view?.findViewById<TextView>(R.id.textView47)?.text = completionText

        // HYDRATION REMINDER
        val reminders = loadReminders(requireContext())
        val reminderText = view?.findViewById<TextView>(R.id.textView32)

        if (!reminders.isNullOrEmpty()) {
            val latest = reminders.last()
            val formattedTime = formatTime(latest.hour, latest.minute)
            reminderText?.text = formattedTime
        } else {
            reminderText?.text = "No reminders"
        }
    }


    // ------------------------------------------------------------
    // HELPER FUNCTIONS
    // ------------------------------------------------------------

    /* Returns a greeting based on the last registered user */
    private fun getWelcomeMessage(context: Context): String {
        val prefs = context.getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("users_list", null)
        if (json.isNullOrEmpty()) return "Welcome, \nUser"

        val type = object : TypeToken<MutableList<UserDetails>>() {}.type
        val users: MutableList<UserDetails> = Gson().fromJson(json, type)

        return if (users.isNotEmpty()) {
            val currentUser = users.last()
            "Welcome, \n${currentUser.name}"
        } else {
            "Welcome, \nUser"
        }
    }

    /* Displays today's mood (emoji + text) if available */
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun showTodayMood(context: Context, moodImage: GifImageButton, moodText: TextView) {
        val moods = MoodStorage.load(context)
        if (moods.isEmpty()) {
            moodImage.setImageResource(R.drawable.wink)
            moodText.text = "No mood logged today"
            return
        }

        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayMoods = moods.filter {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timeStamp)) == todayKey
        }

        val latestMood = todayMoods.maxByOrNull { it.timeStamp }
        if (latestMood != null) {
            moodImage.setImageResource(latestMood.emojiResId)
            moodText.text = "Today's Mood"
        } else {
            moodImage.setImageResource(R.drawable.wink)
            moodText.text = "No mood logged today"
        }
    }

    /* Loads saved hydration reminders */
    private fun loadReminders(context: Context): List<Reminder>? {
        val sharedPref = context.getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("reminders", null)
        return if (json != null) Gson().fromJson(json, Array<Reminder>::class.java).toList() else null
    }

    /* Formats hour/minute to a readable 12-hour string */
    @SuppressLint("DefaultLocale")
    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "a.m" else "p.m"
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        return String.format("%d:%02d %s", hour12, minute, amPm)
    }
}
