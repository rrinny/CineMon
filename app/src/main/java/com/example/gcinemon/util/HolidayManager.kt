package com.example.gcinemon.util

import android.content.Context
import android.content.SharedPreferences

// 공휴일 정보를 SharedPreferences로 관리하는 유틸 클래스
class HolidayManager(context: Context) {
    // 공휴일 데이터를 저장하기 위한 SharedPreferences 객체
    private val prefs: SharedPreferences =
        context.getSharedPreferences("HolidayPrefs", Context.MODE_PRIVATE)

    // 공휴일 날짜와 이름을 로컬 저장소에 저장
    fun saveHolidays(holidayMap: Map<String, String>) {
        val editor = prefs.edit()
        holidayMap.forEach { (date, name) ->
            editor.putString(date, name)
        }
        editor.apply()
    }

    // 전달받은 날짜가 공휴일인지 여부를 확인
    fun isHoliday(date: String): Boolean {
        return prefs.contains(date)
    }

    // 해당 날짜의 공휴일 이름을 반환
    fun getHolidayName(date: String): String? {
        return prefs.getString(date, null)
    }
}