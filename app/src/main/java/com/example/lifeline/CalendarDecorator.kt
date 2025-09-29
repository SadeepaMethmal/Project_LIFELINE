package com.example.lifeline

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class CalendarDecorator(
    private val dates: Set<CalendarDay>,
    private val emojiDrawable: Drawable
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)

    override fun decorate(view: DayViewFacade) {
        // Set PNG background
        view.setBackgroundDrawable(emojiDrawable)

        // Hide the day number by making text transparent
        view.addSpan(ForegroundColorSpan(Color.TRANSPARENT))
    }
}
