package com.example.gcinemon.util

import com.example.gcinemon.data.entity.ScheduleEntity
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

// 근무 일정과 설정 값을 기반으로 급여를 계산하는 유틸 클래스
class SalaryCalculator(private val prefs: PreferenceManager) {

    // 급여 계산 결과를 묶어 반환하기 위한 데이터 클래스
    data class SalaryResult(
        val basePay: Int,
        val weeklyBonus: Int,
        val holidayBonus: Int,
        val nightBonus: Int,
        val taxAmount: Int,
        val totalPay: Int
    )

    // 근무 일정과 공휴일 정보를 기반으로 최종 급여를 계산
    fun calculate(schedules: List<ScheduleEntity>, holidayManager: HolidayManager): SalaryResult {
        var totalBase = 0.0
        var totalHolidayBonus = 0.0
        var totalNightBonus = 0.0

        // 주차별 근무 시간을 누적하여 주휴 수당 계산에 사용
        val weeklyHoursMap = mutableMapOf<String, Float>()
        val actualHoursPerDay = prefs.workTime - (prefs.restTime / 60f)

        schedules.forEach { schedule ->
            // // 일일 기본급을 계산하여 누적
            val dayBasePay = actualHoursPerDay * prefs.baseWage
            totalBase += dayBasePay

            // 공휴일 근무 시 추가 수당을 가산 (시급의 0.5배 추가 지급)
            val dateKey = schedule.date.replace("-", "")
            if (holidayManager.isHoliday(dateKey)) {
                totalHolidayBonus += (dayBasePay * 0.5)
            }

            // 야간 수당 가산 구조를 고려한 계산 로직 (22:00 ~ 06:00 사이 근무)
            // 현재는 22시 정각 종료이므로 0원이며, 확장 시 actualEndTime 수정
            if (schedule.workType == "마감") {
                val actualEndTime = 22.0f
                if (actualEndTime > 22.0f) {
                    val nightHours = actualEndTime - 22.0f
                    // 야간 근무 시 추가 수당을 가산 (시급의 0.5배 추가 지급)
                    totalNightBonus += (nightHours * prefs.baseWage * 0.5)
                }
            }

            // 날짜 기준 주차 키로 근무 시간을 누적
            val weekKey = getWeekOfYear(schedule.date)
            weeklyHoursMap[weekKey] = weeklyHoursMap.getOrDefault(weekKey, 0f) + actualHoursPerDay
        }

        // 주휴 수당 계산 (주 15시간 이상인 주만 합산)
        var totalWeeklyBonus = 0.0
        if (prefs.isAllowanceEnabled) {
            weeklyHoursMap.values.forEach { weeklyHours ->
                if (weeklyHours >= 15f) { // 주 15시간 기준 적용
                    // 주휴 수당 공식: (주 근무 시간 / 40시간) * 8 * 시급 (최대 40시간 인정)
                    val cappedHours = if (weeklyHours > 40f) 40f else weeklyHours
                    totalWeeklyBonus += (cappedHours / 40f) * 8 * prefs.baseWage
                }
            }
        }

        // 세금 적용 여부에 따라 공제 금액을 계산 (3.3% 적용)
        val subTotal = totalBase + totalHolidayBonus + totalNightBonus + totalWeeklyBonus
        val tax = if (prefs.isTaxEnabled) (subTotal * 0.033) else 0.0

        // 최종 급여 계산 결과를 객체로 반환
        return SalaryResult(
            basePay = totalBase.toInt(),
            weeklyBonus = totalWeeklyBonus.toInt(),
            holidayBonus = totalHolidayBonus.toInt(),
            nightBonus = totalNightBonus.toInt(),
            taxAmount = tax.toInt(),
            totalPay = (subTotal - tax).toInt()
        )
    }

    // 날짜 문자열을 연도-주차 형식의 키로 변환
    private fun getWeekOfYear(dateString: String): String {
        val date = LocalDate.parse(dateString)
        val weekFields = WeekFields.of(Locale.KOREA)
        return "${date.year}-${date.get(weekFields.weekOfYear())}"
    }
}