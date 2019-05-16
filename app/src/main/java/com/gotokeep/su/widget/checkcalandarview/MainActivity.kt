package com.gotokeep.su.widget.checkcalandarview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.viewSummary
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val formater = SimpleDateFormat("yyyyMMdd")
        val calender = Calendar.getInstance(Locale.CHINA)
        calender.set(2018, 11, 31)
        viewSummary.adapter = DayflowSummaryAdapter(DateTime(2018, 12, 30, 0, 0, 0), mapOf(formater.format(Date(1557849600000)) to 2))
    }
}
