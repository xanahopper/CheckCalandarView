package com.gotokeep.su.widget.checkcalandarview

import android.graphics.Color
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

/**
 *
 *
 * @author Xana/cuixianming
 */
class DayflowSummaryAdapter(
    override val startDay: DateTime,
    private val data: Map<String, Int>
) : DayflowSummaryView.Adapter() {

    private val startYear: Int = startDay.year
    private val startMonth: Int = startDay.monthOfYear
    private val today = DateTime.now()

    override fun getDayActive(day: DateTime): Boolean {
        return day > startDay && day <= today
    }

    private fun hasRecord(day: DateTime): Boolean {
        return data.containsKey(day.toString("yyyyMMdd"))
    }

    override fun getDayColor(day: DateTime): Int {
        return if (hasRecord(day)) ACTIVE_COLOR else NORMAL_COLOR
    }

    override fun getYearColor(year: Int): Int {
        return if (year >= startYear) ACTIVE_COLOR else NORMAL_COLOR
    }

    override fun getMonthColor(year: Int, month: Int): Int {
        return if (year > startYear || (year == startYear && month >= startMonth)) ACTIVE_COLOR else NORMAL_COLOR
    }

    override val earliestDay: DateTime = startDay.dayOfYear().addToCopy(-BACK_DAYS).withDayOfWeek(DateTimeConstants.SATURDAY)

    companion object {
        private val NORMAL_COLOR = Color.argb(101, 255, 255, 255)
        private const val ACTIVE_COLOR = Color.WHITE

        private const val BACK_DAYS = 150
    }
}
