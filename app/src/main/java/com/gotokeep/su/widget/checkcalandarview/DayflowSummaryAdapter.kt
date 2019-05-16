package com.gotokeep.su.widget.checkcalandarview

import android.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 *
 * @author Xana/cuixianming
 */
class DayflowSummaryAdapter(
    val startDate: Date,
    val data: Map<String, Int>
) : DayflowSummaryView.Adapter() {

    private val calender = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
    private val startYear: Int
    private val startMonth: Int
    private val today = Date()

    init {
        calender.time = startDate
        startYear = calender.get(Calendar.YEAR)
        startMonth = calender.get(Calendar.MONTH)
    }

    override fun getDayActive(day: Date): Boolean {
        return day > startDate && day <= today
    }

    private fun hasRecord(day: Date): Boolean {
        calender.time = day
        return data.containsKey(format.format(calender.time))
    }

    override fun getDayColor(day: Date): Int {
        return if (hasRecord(day)) ACTIVE_COLOR else NORMAL_COLOR
    }

    override fun getYearColor(year: Int): Int {
        return if (year >= startYear) ACTIVE_COLOR else NORMAL_COLOR
    }

    override fun getMonthColor(year: Int, month: Int): Int {
        return if (year > startYear || (year == startYear && month >= startMonth)) ACTIVE_COLOR else NORMAL_COLOR
    }

    companion object {
        private val NORMAL_COLOR = Color.argb(101, 255, 255, 255)
        private const val ACTIVE_COLOR = Color.WHITE
        private val format = SimpleDateFormat("yyyyMMdd")

        private fun isSameDay(dayA: Date, dayB: Date): Boolean {
            val calA = Calendar.getInstance().apply { time = dayA }
            val calB = Calendar.getInstance().apply { time = dayB }
            return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
                    calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
        }
    }
}
