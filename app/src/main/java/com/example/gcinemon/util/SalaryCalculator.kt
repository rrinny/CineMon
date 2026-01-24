package com.example.gcinemon.util

import com.example.gcinemon.data.entity.ScheduleEntity
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

class SalaryCalculator(private val prefs: PreferenceManager) {

    data class SalaryResult(
        val basePay: Int,
        val weeklyBonus: Int,
        val holidayBonus: Int,
        val nightBonus: Int,
        val taxAmount: Int,
        val totalPay: Int
    )

    fun calculate(schedules: List<ScheduleEntity>, holidayManager: HolidayManager): SalaryResult {
        var totalBase = 0.0
        var totalHolidayBonus = 0.0
        var totalNightBonus = 0.0

        // 주차별 근무 시간 합산을 위한 맵 (Key: 연도-주차)
        val weeklyHoursMap = mutableMapOf<String, Float>()
        val actualHoursPerDay = prefs.workTime - (prefs.restTime / 60f)

        schedules.forEach { schedule ->
            // 기본급 계산
            val dayBasePay = actualHoursPerDay * prefs.baseWage
            totalBase += dayBasePay

            // 공휴일 수당 가산 (시급의 0.5배 추가 지급)
            val dateKey = schedule.date.replace("-", "")
            if (holidayManager.isHoliday(dateKey)) {
                totalHolidayBonus += (dayBasePay * 0.5)
            }

            // 야간 수당 가산 구조 (22:00 ~ 06:00 사이 근무)
            // 현재는 22시 정각 종료라 0원이며, 확장 시 actualEndTime 수정
            if (schedule.workType == "마감") {
                val actualEndTime = 22.0f
                if (actualEndTime > 22.0f) {
                    val nightHours = actualEndTime - 22.0f
                    // 야간 수당 가산 (시급의 0.5배 추가 지급)
                    totalNightBonus += (nightHours * prefs.baseWage * 0.5)
                }
            }

            // 주차별 시간 합산 (주휴 수당용)
            val weekKey = getWeekOfYear(schedule.date)
            weeklyHoursMap[weekKey] = weeklyHoursMap.getOrDefault(weekKey, 0f) + actualHoursPerDay
        }

        // 주휴 수당 계산 (주 15시간 이상인 주만 합산)
        var totalWeeklyBonus = 0.0
        if (prefs.isAllowanceEnabled) {
            weeklyHoursMap.values.forEach { weeklyHours ->
                if (weeklyHours >= 15f) { // 주 15시간 기준 적용
                    // 주휴 수당 공식: (주 근로시간 / 40시간) * 8 * 시급 (최대 40시간 인정)
                    val cappedHours = if (weeklyHours > 40f) 40f else weeklyHours
                    totalWeeklyBonus += (cappedHours / 40f) * 8 * prefs.baseWage
                }
            }
        }

        // 세금 및 합계 계산 (3.3% 적용)
        val subTotal = totalBase + totalHolidayBonus + totalNightBonus + totalWeeklyBonus
        val tax = if (prefs.isTaxEnabled) (subTotal * 0.033) else 0.0

        return SalaryResult(
            basePay = totalBase.toInt(),
            weeklyBonus = totalWeeklyBonus.toInt(),
            holidayBonus = totalHolidayBonus.toInt(),
            nightBonus = totalNightBonus.toInt(),
            taxAmount = tax.toInt(),
            totalPay = (subTotal - tax).toInt()
        )
    }

    // 날짜 문자열로 주차(Week of Year)를 구하는 헬퍼 함수
    private fun getWeekOfYear(dateString: String): String {
        val date = LocalDate.parse(dateString)
        val weekFields = WeekFields.of(Locale.KOREA)
        return "${date.year}-${date.get(weekFields.weekOfYear())}"
    }
}