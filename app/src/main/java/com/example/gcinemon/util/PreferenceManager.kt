package com.example.gcinemon.util

import android.content.Context
import android.content.SharedPreferences

// 앱 설정 값을 SharedPreferences로 관리하는 매니저 클래스
class PreferenceManager(context: Context) {
    // 앱 설정 정보를 저장하기 위한 SharedPreferences 객체
    private val prefs: SharedPreferences =
        context.getSharedPreferences("CineMonPrefs", Context.MODE_PRIVATE)

    companion object {
        // 설정 항목별 키 값을 상수로 정의하여 일관성을 유지
        const val KEY_BASE_WAGE = "base_wage"
        const val KEY_PAYDAY = "payday"
        const val KEY_START_DAY_OF_WEEK = "start_day"
        const val KEY_WORK_TIME = "work_time"
        const val KEY_REST_TIME = "rest_time"
        const val KEY_TAX_ENABLED = "tax_enabled"
        const val KEY_ALLOWANCE_ENABLED = "allowance_enabled"
    }

    // 기본 시급 값을 저장 및 조회 (기본값: 10,320원)
    var baseWage: Int
        get() = prefs.getInt(KEY_BASE_WAGE, 10320)
        set(value) = prefs.edit().putInt(KEY_BASE_WAGE, value).apply()

    // 근무 시간을 저장 및 조회 (기본값: 7.5시간)
    var workTime: Float
        get() = prefs.getFloat(KEY_WORK_TIME, 7.5f)
        set(value) = prefs.edit().putFloat(KEY_WORK_TIME, value).apply()

    // 휴게 시간을 저장 및 조회 (기본값: 30분)
    var restTime: Int
        get() = prefs.getInt(KEY_REST_TIME, 30)
        set(value) = prefs.edit().putInt(KEY_REST_TIME, value).apply()

    // 주간 시작 요일 설정을 저장 및 조회 (기본값: 월요일)
    var startDayOfWeek: String
        get() = prefs.getString(KEY_START_DAY_OF_WEEK, "월요일") ?: "월요일"
        set(value) = prefs.edit().putString(KEY_START_DAY_OF_WEEK, value).apply()

    // 월급날 설정을 저장 및 조회 (기본값: 5일)
    var payday: Int
        get() = prefs.getInt(KEY_PAYDAY, 5)
        set(value) = prefs.edit().putInt(KEY_PAYDAY, value).apply()

    // 세금 공제 계산 사용 여부를 저장 및 조회 (기본값: true)
    var isTaxEnabled: Boolean
        get() = prefs.getBoolean(KEY_TAX_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_TAX_ENABLED, value).apply()

    // 주휴 수당 자동 계산 사용 여부를 저장 및 조회 (기본값: true)
    var isAllowanceEnabled: Boolean
        get() = prefs.getBoolean(KEY_ALLOWANCE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ALLOWANCE_ENABLED, value).apply()
}