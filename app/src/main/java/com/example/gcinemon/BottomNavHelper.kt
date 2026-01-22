package com.example.gcinemon

import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

object BottomNavHelper {

    enum class Tab { HOME, SCHEDULE, REPORT, MY }

    fun bind(activity: AppCompatActivity, selected: Tab) {
        val orange = ContextCompat.getColor(activity, R.color.gc_orange_main)
        val gray = ContextCompat.getColor(activity, R.color.gc_black_20)

        val tabHome = activity.findViewById<LinearLayout>(R.id.tabHome)
        val tabSchedule = activity.findViewById<LinearLayout>(R.id.tabSchedule)
        val tabReport = activity.findViewById<LinearLayout>(R.id.tabReport)
        val tabMy = activity.findViewById<LinearLayout>(R.id.tabMy)

        val ivHome = activity.findViewById<ImageView>(R.id.ivTabHome)
        val tvHome = activity.findViewById<TextView>(R.id.tvTabHome)
        val ivSchedule = activity.findViewById<ImageView>(R.id.ivTabSchedule)
        val tvSchedule = activity.findViewById<TextView>(R.id.tvTabSchedule)
        val ivReport = activity.findViewById<ImageView>(R.id.ivTabReport)
        val tvReport = activity.findViewById<TextView>(R.id.tvTabReport)
        val ivMy = activity.findViewById<ImageView>(R.id.ivTabMy)
        val tvMy = activity.findViewById<TextView>(R.id.tvTabMy)

        // reset
        ivHome.setColorFilter(gray); tvHome.setTextColor(gray)
        ivSchedule.setColorFilter(gray); tvSchedule.setTextColor(gray)
        ivReport.setColorFilter(gray); tvReport.setTextColor(gray)
        ivMy.setColorFilter(gray); tvMy.setTextColor(gray)

        // selected
        when (selected) {
            Tab.HOME -> { ivHome.setColorFilter(orange); tvHome.setTextColor(orange) }
            Tab.SCHEDULE -> { ivSchedule.setColorFilter(orange); tvSchedule.setTextColor(orange) }
            Tab.REPORT -> { ivReport.setColorFilter(orange); tvReport.setTextColor(orange) }
            Tab.MY -> { ivMy.setColorFilter(orange); tvMy.setTextColor(orange) }
        }

        tabHome.setOnClickListener {
            if (selected != Tab.HOME) {
                activity.startActivity(Intent(activity, HomeActivity::class.java))
                activity.finish()
            }
        }
        tabSchedule.setOnClickListener {
            if (selected != Tab.SCHEDULE) {
                activity.startActivity(Intent(activity, ScheduleActivity::class.java))
                activity.finish()
            }
        }
        tabReport.setOnClickListener {
            if (selected != Tab.REPORT) {
                activity.startActivity(Intent(activity, ReportActivity::class.java))
                activity.finish()
            }
        }
        tabMy.setOnClickListener {
            if (selected != Tab.MY){
                activity.startActivity(Intent(activity, MyActivity::class.java))
                activity.finish()
            }
        }
    }
}
