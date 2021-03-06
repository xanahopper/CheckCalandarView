package com.gotokeep.su.widget.checkcalandarview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.EdgeEffect
import android.widget.OverScroller
import androidx.core.graphics.withTranslation
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Duration
import kotlin.math.abs
import kotlin.math.max

/**
 * 日记本总结 View
 *
 * @author Xana/cuixianming
 */
class DayflowSummaryView : View {

    private var radius = 0
    private val inactiveColor = Color.argb(26, 255, 255, 255)
    private val activeColor = Color.WHITE
    private var pointSpacing = 0
    private var pointSize = 10
    private var weekTextSize = 9
    private var monthTextSize = 11
    private var yearTextSize = 16
    private var weekLabels = listOf("日", "", "二", "", "四", "", "六")
    private var clipToPadding = true

    private val dataCache = SparseArray<WeekData>()

    private val scroller by lazy { OverScroller(context) }
    private val velocityTracker = VelocityTracker.obtain()
    private val edgeEffect = EdgeEffect(context)
    private var lastX = 0
    private var currentX = 0
    private var distanceX = 0

    private val weekTextPaint by lazy {
        TextPaint().apply {
            textSize = weekTextSize.toFloat()
            color = activeColor
            isAntiAlias = true
        }
    }
    private val monthTextPaint by lazy {
        TextPaint().apply {
            textSize = monthTextSize.toFloat()
            color = activeColor
            isAntiAlias = true
        }
    }
    private val yearTextPaint by lazy {
        TextPaint().apply {
            textSize = yearTextSize.toFloat()
            color = activeColor
            isAntiAlias = true
        }
    }
    private val pointPaint by lazy {
        Paint().apply {
            isAntiAlias = true
        }
    }

    private val weekLabelSize by lazy {
        weekLabels.map {
            val rect = Rect()
            weekTextPaint.getTextBounds(it, 0, it.length, rect)
            rect
        }
    }
    val maxWeekSize by lazy { weekLabelSize.maxBy { it.width() }!!.width() }
    private val topSpacing by lazy {
        yearTextSize + ViewUtils.dpToPx(context, DIM_YEAR_MONTH) +
                monthTextSize + ViewUtils.dpToPx(context, DIM_MONTH_POINT)
    }

    var adapter: Adapter? = null
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }

    var showWeekends = true
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }
    val weekdays
        get() = if (showWeekends) WEEKDAY else WORKDAY

    var startDate = DateTime.now()
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }
    private var currentDate = DateTime.now()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup(context, attrs, defStyleAttr)
    }

    private fun setup(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.DayflowSummaryView,
            defStyleAttr,
            R.style.DayflowDefaultStyle
        )
        radius = ta.getDimensionPixelSize(R.styleable.DayflowSummaryView_radius, 0)
        pointSpacing = ta.getDimensionPixelSize(R.styleable.DayflowSummaryView_pointSpacing, 0)
        pointSize = ta.getDimensionPixelSize(R.styleable.DayflowSummaryView_pointSize, 10)
        weekTextSize = ta.getDimensionPixelSize(R.styleable.DayflowSummaryView_weekTextSize, 9)
        monthTextSize = ta.getDimensionPixelSize(R.styleable.DayflowSummaryView_monthTextSize, 11)
        yearTextSize = ta.getDimensionPixelSize(R.styleable.DayflowSummaryView_yearTextSize, 16)
        clipToPadding = ta.getBoolean(R.styleable.DayflowSummaryView_android_clipToPadding, clipToPadding)
        ta.recycle()
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, 0)
            postInvalidate()
        }
        super.computeScroll()
    }

    private fun smoothScrollBy(dx: Int) {
        val offsetX = when {
            scroller.finalX + dx < 0 -> -scroller.finalX
            scroller.finalX + dx > getScrollRange() -> getScrollRange() - scroller.finalX
            else -> dx
        }
        scroller.startScroll(scroller.finalX, scroller.finalY, offsetX, 0)
        postInvalidateOnAnimation()
    }

    private fun fling(velocityX: Int) {
        scroller.fling(scroller.finalX, scroller.finalY, velocityX, 0, 0, getScrollRange(), 0, 0)
        postInvalidateOnAnimation()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        velocityTracker.addMovement(event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished) {
                    scroller.abortAnimation()
                }
                lastX = event.x.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                currentX = event.x.toInt()
                distanceX = lastX - currentX
                lastX = currentX
                smoothScrollBy(distanceX)
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker.computeCurrentVelocity(1000)
                val velocityX = velocityTracker.xVelocity
                currentX = event.x.toInt()
                distanceX = lastX - currentX
                lastX = currentX
                if (abs(velocityX) > SNAP_VELOCITY) {
                    if (!scroller.isFinished) {
                        scroller.abortAnimation()
                    }
                    fling(-velocityX.toInt())
                } else if (scroller.springBack(scrollX, scrollY, 0, getScrollRange(), 0, 0)) {
                    postInvalidateOnAnimation()
                }
                edgeEffect.onRelease()
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val width = when (widthMode) {
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST -> getContentWidth()
            MeasureSpec.EXACTLY -> widthSize - paddingLeft - paddingRight
            else -> widthSize - paddingLeft - paddingRight
        }
        val height = pointSize * weekdays + pointSpacing * (weekdays - 1) + yearTextSize + monthTextSize +
                ViewUtils.dpToPx(context, DIM_YEAR_MONTH) + ViewUtils.dpToPx(context, DIM_MONTH_POINT)
        setMeasuredDimension(
            width + paddingLeft + paddingRight,
            height + paddingTop + paddingBottom
        )
    }

    private fun getContentWidth() =
            (adapter?.let {
                val weeks = Duration(
                    it.earliestDay.withDayOfWeek(DateTimeConstants.SATURDAY),
                    it.startDay.withDayOfWeek(DateTimeConstants.SATURDAY)
                ).standardDays / WEEKDAY
                (weeks * pointSize + (weeks - 1) * pointSpacing).toInt()
            } ?: WEEK_COUNT_PER_YEAR * pointSize + (WEEK_COUNT_PER_YEAR - 1) * pointSpacing)

    private fun getScrollRange() = max(0, measuredWidth - getContentWidth())

    override fun onDraw(canvas: Canvas?) {
        val scrollOffset = computeHorizontalScrollOffset()
        if (clipToPadding) {
            canvas?.clipRect(
                scrollOffset + paddingLeft, paddingTop,
                scrollOffset + measuredWidth - paddingRight, measuredHeight - paddingBottom
            )
        }
        canvas?.withTranslation((paddingLeft).toFloat(), paddingTop.toFloat()) {
            resetDate()
            var currentColumnOffsetX = drawWeekLabel(this)
            var currentYear = currentDate.year
            var currentMonth = currentDate.monthOfYear

            val width = measuredWidth - paddingLeft - paddingRight
            drawYearLabel(canvas, currentColumnOffsetX, width + scrollOffset, currentYear)
            currentColumnOffsetX = skipDate(currentColumnOffsetX)
            while (currentColumnOffsetX < width + scrollOffset && inRange()) {
                val weekDay = currentDate.dayOfWeek
                val year = currentDate.year
                val month = currentDate.monthOfYear
                val day = currentDate.dayOfMonth

                if (year != currentYear) {
                    drawYearLabel(canvas, currentColumnOffsetX, width + scrollOffset, year)
                    currentYear = year
                }

                if (month != currentMonth && day == currentDate.dayOfMonth().maximumValue) {
                    drawMonthLabel(canvas, currentColumnOffsetX, width + scrollOffset, year, month)
                    currentMonth = month
                }
                drawCalendarPoint(canvas, currentColumnOffsetX, weekDay)

                currentColumnOffsetX = decreaseCalendar(currentColumnOffsetX)
            }
        }
    }

    private fun inRange(): Boolean = adapter?.let { currentDate.isAfter(it.earliestDay) } ?: true

    private fun decreaseCalendar(currentColumnOffsetX: Int): Int {
        currentDate = currentDate.dayOfYear().addToCopy(-1)
        return if (currentDate.dayOfWeek == DateTimeConstants.SATURDAY) {
            currentColumnOffsetX + pointSize + pointSpacing
        } else currentColumnOffsetX
    }

    private fun drawCalendarPoint(canvas: Canvas, currentColumnOffsetX: Int, weekDay: Int) {
        pointPaint.color = getPointColor(currentDate)
        canvas.drawCircle(
            currentColumnOffsetX + pointSize * 0.5f,
            topSpacing + weekDay.rem(WEEKDAY) * (pointSize + pointSpacing) + pointSize * 0.5f,
            pointSize * 0.5f, pointPaint
        )
    }

    private fun prepareCache() {

    }

    private fun resetDate() {
        currentDate = startDate.withTime(0, 0, 0, 0)
            .dayOfMonth().withMaximumValue()
        if (currentDate.dayOfWeek != DateTimeConstants.SATURDAY) {
            currentDate = currentDate.withDayOfWeek(DateTimeConstants.SATURDAY)
        }
    }

    private fun skipDate(currentColumnOffsetX: Int): Int {
        val offsetSize = scrollX - currentColumnOffsetX
        return if (offsetSize > 0) {
            val skipWeek = offsetSize / (pointSize + pointSpacing)
            currentDate = currentDate.weekOfWeekyear().addToCopy(-skipWeek)
            currentColumnOffsetX + (pointSize + pointSpacing) * skipWeek
        } else currentColumnOffsetX
    }

    private fun getYearColor(year: Int): Int = adapter?.getYearColor(year) ?: inactiveColor

    private fun getMonthColor(year: Int, month: Int): Int {
        return adapter?.getMonthColor(year, month) ?: inactiveColor
    }

    private fun getPointColor(time: DateTime): Int {
        return adapter?.let { adapter ->
            if (adapter.getDayActive(time)) {
                adapter.getDayColor(time)
            } else inactiveColor
        } ?: inactiveColor
    }

    private fun drawWeekLabel(canvas: Canvas): Int {
        val leftSpacing = ViewUtils.dpToPx(context, DIM_WEEK_START_PADDING)
        weekTextPaint.color = activeColor
        val scrollOffset = computeHorizontalScrollOffset()
        if (-scrollOffset + leftSpacing + maxWeekSize > 0) {
            canvas.withTranslation((leftSpacing).toFloat(), (topSpacing).toFloat()) {
                val fmi = weekTextPaint.fontMetricsInt
                weekLabels.forEachIndexed { index, s ->
                    if (s.isNotEmpty()) {
                        val offsetY = index * (pointSpacing + pointSize)
                        val baseline = offsetY + (pointSize - fmi.bottom - fmi.top) * 0.5f
                        canvas.drawText(s, 0f, baseline, weekTextPaint)
                    }
                }
            }
        }
        return leftSpacing + maxWeekSize + ViewUtils.dpToPx(context, DIM_WEEK_POINT)
    }

    private fun drawMonthLabel(canvas: Canvas, currentColumnOffsetX: Int, width: Int, year: Int, month: Int) {
        val text = month.toString() + "月"
        val right = currentColumnOffsetX + monthTextPaint.measureText(text)
        if (right > 0 && currentColumnOffsetX < width) {
            monthTextPaint.color = getMonthColor(year, month)
            canvas.drawText(
                text, currentColumnOffsetX.toFloat(),
                (yearTextSize + ViewUtils.dpToPx(context, DIM_YEAR_MONTH) + monthTextSize).toFloat(), monthTextPaint
            )
        }
    }

    private fun drawYearLabel(canvas: Canvas, currentColumnOffsetX: Int, width: Int, year: Int) {
        val right = currentColumnOffsetX + yearTextPaint.measureText(year.toString())
        if (right > 0 && currentColumnOffsetX < width) {
            yearTextPaint.color = getYearColor(year)
            canvas.drawText(year.toString(), currentColumnOffsetX.toFloat(), yearTextSize.toFloat(), yearTextPaint)
        }
    }

    abstract class Adapter {

        abstract fun getDayActive(day: DateTime): Boolean

        abstract fun getDayColor(day: DateTime): Int

        abstract fun getYearColor(year: Int): Int

        abstract fun getMonthColor(year: Int, month: Int): Int

        abstract val earliestDay: DateTime

        abstract val startDay: DateTime
    }

    private class WeekData(
        val year: Int? = null,
        val month: Int? = null,
        val colors: IntArray = IntArray(WEEKDAY)
    )

    companion object {
        private const val WEEK_COUNT_PER_YEAR = 54
        private const val WEEKDAY = 7
        private const val WORKDAY = 5

        private const val DIM_YEAR_MONTH = 13.5f
        private const val DIM_MONTH_POINT = 8f
        private const val DIM_WEEK_POINT = 14.5f
        private const val DIM_WEEK_START_PADDING = 20f

        private const val SNAP_VELOCITY = 200
    }
}
