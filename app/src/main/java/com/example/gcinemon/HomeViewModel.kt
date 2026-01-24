package com.example.gcinemon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gcinemon.data.dao.ScheduleDao
import com.example.gcinemon.data.dao.UserDao
import com.example.gcinemon.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeViewModel(private val scheduleDao: ScheduleDao) : ViewModel() {

    data class DayStatus(
        val date: String,
        val dayNum: String,
        val isPast: Boolean,
        val isToday: Boolean,
        val hasSchedule: Boolean
    )

    private val _remainingTime = MutableStateFlow("")
    val remainingTime: StateFlow<String> = _remainingTime

    private val _nextSchedule = MutableStateFlow<ScheduleEntity?>(null)
    val nextSchedule: StateFlow<ScheduleEntity?> = _nextSchedule

    private val _weekStatus = MutableStateFlow<List<DayStatus>>(emptyList())
    val weekStatus: StateFlow<List<DayStatus>> = _weekStatus

    init {
        loadIncomingSchedule()
        loadWeekStatus(java.time.DayOfWeek.MONDAY)
    }

    private fun loadIncomingSchedule() {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        viewModelScope.launch {
            scheduleDao.getFutureSchedules(todayStr).collectLatest { schedules ->
                val now = LocalTime.now()

                val next = schedules.firstOrNull { schedule ->
                    val scheduleDate = LocalDate.parse(schedule.date)

                    if (scheduleDate.isAfter(today)) {
                        true
                    } else if (scheduleDate.isEqual(today)) {
                        // 오늘 근무라면, 설정된 퇴근 시간과 비교
                        val endTime = when (schedule.workType) {
                            "오픈" -> LocalTime.of(15, 30)
                            "미들" -> LocalTime.of(18, 30)
                            "마감" -> LocalTime.of(22, 0)
                            else -> LocalTime.MAX
                        }
                        now.isBefore(endTime)
                    } else {
                        false
                    }
                }

                // 실시간 변수 업데이트
                _nextSchedule.value = next

                if (next != null) {
                    calculateDayDifference(next.date, today)
                } else {
                    _remainingTime.value = "예정된 근무가 없어요!"
                }
            }
        }
    }

    private fun calculateDayDifference(targetDateStr: String, today: LocalDate) {
        try {
            val targetDate = LocalDate.parse(targetDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val daysBetween = ChronoUnit.DAYS.between(today, targetDate)

            _remainingTime.value = when {
                daysBetween == 0L -> "오늘"
                daysBetween > 0L -> "${daysBetween}일"
                else -> ""
            }
        } catch (e: Exception) {
            _remainingTime.value = ""
        }
    }

    fun loadWeekStatus(startDayOfWeek: java.time.DayOfWeek) {
        val today = LocalDate.now()
        // 설정된 요일을 기준으로 가장 가까운 과거 날짜를 계산
        val startOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(startDayOfWeek))

        viewModelScope.launch {
            val statusList = mutableListOf<DayStatus>()
            for (i in 0..6) {
                val date = startOfWeek.plusDays(i.toLong())
                val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                val hasSchedule = scheduleDao.getLatestScheduleByDate(dateStr) != null
                val isToday = date.isEqual(today)
                val isPast = date.isBefore(today)

                statusList.add(DayStatus(dateStr, date.dayOfMonth.toString(), isPast, isToday, hasSchedule))
            }
            _weekStatus.value = statusList
        }
    }

    fun refreshAll(startDayOfWeek: java.time.DayOfWeek) {
        loadIncomingSchedule()
        loadWeekStatus(startDayOfWeek)
    }
}