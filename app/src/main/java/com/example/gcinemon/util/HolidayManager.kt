package com.example.gcinemon.util

import android.content.Context
import android.content.SharedPreferences

class HolidayManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("HolidayPrefs", Context.MODE_PRIVATE)

    // 공휴일 정보를 로컬에 저장
    fun saveHolidays(holidayMap: Map<String, String>) {
        val editor = prefs.edit()
        holidayMap.forEach { (date, name) ->
            editor.putString(date, name)
        }
        editor.apply()
    }

    // 해당 날짜가 공휴일인지 확인
    fun isHoliday(date: String): Boolean {
        return prefs.contains(date)
    }

    // 공휴일 이름 가져오기
    fun getHolidayName(date: String): String? {
        return prefs.getString(date, null)
    }
}