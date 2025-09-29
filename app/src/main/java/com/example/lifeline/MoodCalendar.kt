package com.example.lifeline

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lifeline.databinding.FragmentMoodCalendarBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.*

class MoodCalendar : Fragment() {

    private var _binding: FragmentMoodCalendarBinding? = null
    private val binding get() = _binding!!

    // Map moods (gif IDs used in app) → PNG icons for calendar
    private val moodToCalendarIcon = mapOf(
        R.drawable.happy to R.drawable.happy_png,
        R.drawable.sad to R.drawable.sad_png,
        R.drawable.love to R.drawable.love_png,
        R.drawable.angry to R.drawable.angry_png
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodCalendarBinding.inflate(inflater, container, false)

        binding.historyBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, MoodHistory())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendarView: MaterialCalendarView = binding.calendarView
        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE

        val moods = MoodStorage.load(requireContext())
        Log.d("MoodCalendar", "Loaded moods = ${moods.size}")

        val moodsByEmoji = moods.groupBy { it.emojiResId }

        moodsByEmoji.forEach { (emojiResId, moodList) ->
            val dates = moodList.map {
                val cal = Calendar.getInstance().apply { timeInMillis = it.timeStamp }
                CalendarDay.from(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
            }.toHashSet()

            val pngResId = moodToCalendarIcon[emojiResId] ?: R.drawable.business
            val drawable = ContextCompat.getDrawable(requireContext(), pngResId) ?: return@forEach

            calendarView.addDecorator(CalendarDecorator(dates, drawable))
        }

        calendarView.invalidateDecorators()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
