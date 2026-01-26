package com.example.gcinemon

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import com.example.gcinemon.data.AppDatabase
import com.example.gcinemon.data.mapper.ScheduleMapper
import kotlinx.coroutines.launch

class ScheduleActivity : AppCompatActivity() {

    private lateinit var tvMonthTitle: TextView
    private lateinit var btnPrevMonth: ImageView
    private lateinit var btnNextMonth: ImageView
    private lateinit var rvCalendar: RecyclerView
    private lateinit var btnAddWork: ImageButton

    private val cal: Calendar = Calendar.getInstance()
    private var selectedKey: String? = null
    private var workTagMap: Map<String, List<WorkTag>> = emptyMap()

    private lateinit var adapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        BottomNavHelper.bind(this, BottomNavHelper.Tab.SCHEDULE)

        bindViews()
        setupCalendar()
        bindActions()

        renderMonth()
    }

    private fun bindViews() {
        tvMonthTitle = findViewById(R.id.tvMonthTitle)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        rvCalendar = findViewById(R.id.rvCalendar)
        btnAddWork = findViewById(R.id.btnAddWork)
    }

    private fun setupCalendar() {
        adapter = CalendarAdapter(
            onClickDay = { day ->
                // 다른 달 날짜 클릭 시 해당 달로 이동
                if (!day.isInMonth) {
                    cal.set(Calendar.YEAR, day.year)
                    cal.set(Calendar.MONTH, day.month - 1)
                    renderMonth()

                    selectedKey = day.key
                    adapter.setSelected(day.key)
                }

                // 클릭한 날짜를 기준으로 추가(수정) 화면으로 이동
                selectedKey = day.key
                adapter.setSelected(day.key)

                // 근무 추가/수정 화면으로 이동
                val intent = Intent(this, WorkdayAddActivity::class.java).apply {
                    putExtra("SELECTED_DATE", day.key) // yyyyMMdd 형태
                }
                startActivity(intent)
                overridePendingTransition(0, 0)
            },
            onLongClickDay = { day ->
                // 해당 날짜에 근무 태그가 있을 때만 삭제 시트 표시
                if (day.tags.isNotEmpty()) {
                    showDeleteSheet(day.key)
                }
            }
        )

        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        rvCalendar.adapter = adapter
        rvCalendar.itemAnimator = null
    }

    private fun showDeleteSheet(dateKey: String) {
        val bottomSheet = WorkdayDeleteBottomSheet(
            dateKey = dateKey,
            onDeleted = {
                renderMonth()
            }
        )
        bottomSheet.show(supportFragmentManager, "DeleteSheet")
    }

    private fun bindActions() {
        btnPrevMonth.setOnClickListener {
            cal.add(Calendar.MONTH, -1)
            renderMonth()
        }

        btnNextMonth.setOnClickListener {
            cal.add(Calendar.MONTH, 1)
            renderMonth()
        }

        btnAddWork.setOnClickListener {
            startActivity(Intent(this, WorkdayAddActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }

    private fun renderMonth() {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        tvMonthTitle.text = "${year}년 ${month}월"

        val yearMonth = String.format("%04d-%02d", year, month)

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@ScheduleActivity)

            // 해당 월의 근무 스케줄 조회
            val schedules = db.scheduleDao().getLatestSchedulesOfMonth(yearMonth)

            val finalWorkTagMap = ScheduleMapper.toWorkTagMap(schedules).toMutableMap()

            val holidayManager = com.example.gcinemon.util.HolidayManager(this@ScheduleActivity)

            // 해당 월의 마지막 날짜 계산
            val tempCal = cal.clone() as Calendar
            tempCal.set(Calendar.DAY_OF_MONTH, 1)
            val lastDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            // 공휴일 태그 추가
            for (day in 1..lastDay) {
                val dateKey = String.format(Locale.US, "%04d%02d%02d", year, month, day)
                val holidayName = holidayManager.getHolidayName(dateKey)

                if (holidayName != null) {
                    val tags = finalWorkTagMap[dateKey]?.toMutableList() ?: mutableListOf()

                    // 이미 공휴일 태그가 없다면 추가
                    if (tags.none { it.style == TagStyle.HOLIDAY }) {
                        tags.add(WorkTag(holidayName, TagStyle.HOLIDAY))
                    }
                    finalWorkTagMap[dateKey] = tags
                }
            }

            // 달력 셀 데이터 생성
            val days = CalendarDataBuilder.buildMonthCells(year, month, finalWorkTagMap)
            adapter.submit(days)

            // 이전에 선택된 날짜가 있으면 다시 선택 처리
            selectedKey?.let { adapter.setSelected(it) }
        }
    }

    // 다른 화면에서 돌아왔을 때 달력 갱신
    override fun onResume() {
        super.onResume()
        renderMonth()
    }
}