package com.example.lifeline

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment() {

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

        val prefs = requireContext().getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("users_list", null)

        val userName = view.findViewById<TextView>(R.id.textView7)

        if (json != null) {
            val type = object : TypeToken<MutableList<UserDetails>>() {}.type
            val users: MutableList<UserDetails> = Gson().fromJson(json, type)

            if (users.isNotEmpty()) {
                val currentUser = users.last()  // get the last registered user
                userName.text = "Hi, ${currentUser.name}"
            } else {
                userName.text = "Hi, User"
            }
        } else {
            userName.text = "Hi, User"
        }


        // for the pie chart
        val chart = view.findViewById<PieChart>(R.id.moodPieChart)
        val moods = MoodStorage.load(requireContext())

        if (moods.isEmpty()) {
            chart.setNoDataText("No mood data yet")
            return
        }

        // Count occurrences of each mood by name
        val moodCounts = moods.groupingBy { it.name }.eachCount()

        // Convert to PieEntries
        val entries = moodCounts.map { (moodName, count) ->
            PieEntry(count.toFloat(), moodName)
        }

        // Create dataset
        val dataSet = PieDataSet(entries, "Mood Frequency").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
        }

        // Create PieData
        val data = PieData(dataSet)

        chart.apply {
            this.data = data
            description.isEnabled = false
            isDrawHoleEnabled = false
            dataSet.sliceSpace = 5f
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(android.graphics.Color.BLACK)
            setCenterTextSize(16f)
            legend.isEnabled = true
            legend.textSize = 15f
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.form = Legend.LegendForm.CIRCLE
            animateY(1000)
            invalidate()
        }

        //For today's mood
        val todayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayKey = todayFmt.format(Date())

        val todayMoods = moods.filter{
            todayFmt.format(Date(it.timeStamp)) == todayKey
        }

        val latestMood = todayMoods.maxByOrNull { it.timeStamp }

        val moodImage = view.findViewById<pl.droidsonroids.gif.GifImageButton>(R.id.gif1)
        val moodText = view.findViewById<TextView>(R.id.textView44)

        if (latestMood != null) {
            moodImage.setImageResource(latestMood.emojiResId)
            moodText.text = "Today's Mood"
        } else {
            moodImage.setImageResource(R.drawable.wink) // fallback emoji
            moodText.text = "No mood logged today"
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onResume() {
        super.onResume()

        //for current task completion
        val prefs = requireContext().getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
        val completionText = prefs.getString("task_completion", "0/0")

        val taskCompletionView = view?.findViewById<TextView>(R.id.textView47)
        taskCompletionView?.text = completionText


        //for hydration reminder section
        fun loadReminders(context: Context): List<Reminder>? {
            val sharedPref = context.getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
            val json = sharedPref.getString("reminders", null)
            return if (json != null) Gson().fromJson(json, Array<Reminder>::class.java).toList() else null
        }

        val reminders = loadReminders(requireContext())
        val reminderText = view?.findViewById<TextView>(R.id.textView32) // the big time label
        if (!reminders.isNullOrEmpty()) {
            // Show the last one in the list (newest added)
            val latest = reminders.last()
            val hour = latest.hour
            val minute = latest.minute

            val amPm = if (hour < 12) "a.m" else "p.m"
            val hour12 = if (hour % 12 == 0) 12 else hour % 12
            val formattedTime = String.format("%d:%02d %s", hour12, minute, amPm)

            reminderText?.text = formattedTime
        } else {
            reminderText?.text = "No reminders"
        }


    }

}
