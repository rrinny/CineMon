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

class ScheduleActivity : AppCompatActivity() {

    private lateinit var tvMonthTitle: TextView
    private lateinit var btnPrevMonth: ImageView
    private lateinit var btnNextMonth: ImageView
    private lateinit var rvCalendar: RecyclerView
    private lateinit var btnAddWork: ImageButton

    private val cal: Calendar = Calendar.getInstance()
    private var selectedKey: String? = null

    // 임시 데이터
    // key: "yyyyMMdd"
    private val workTagMap: Map<String, List<WorkTag>> = mapOf(
        "20260118" to listOf(
            WorkTag("매점", TagStyle.ORANGE),
            WorkTag("마감", TagStyle.BLUE)
        ),
        "20260119" to listOf(
            WorkTag("검표", TagStyle.BLUE),
            WorkTag("미들", TagStyle.GREEN)
        ),
        "20260101" to listOf(
            WorkTag("새해", TagStyle.HOLIDAY) // holiday 라벨로 표시
        )
    )

    private lateinit var adapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // 하단 네비 -> 스케줄 선택 상태 + 탭 이동 통합
        BottomNavHelper.bind(this, BottomNavHelper.Tab.SCHEDULE)

        bindViews()
        setupCalendar()
        bindActions()

        renderMonth() // 최초 렌더
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
                // 다른 달 날짜 클릭하면 해당 달로 이동하는 UX
                if (!day.isInMonth) {
                    cal.set(Calendar.YEAR, day.year)
                    cal.set(Calendar.MONTH, day.month - 1)
                    renderMonth()

                    selectedKey = day.key
                    adapter.setSelected(day.key)
                    return@CalendarAdapter
                }

                selectedKey = day.key
                adapter.setSelected(day.key)


            }
        )

        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        rvCalendar.adapter = adapter
        rvCalendar.itemAnimator = null
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

        // 플로팅 버튼 -> 근무일정 추가 화면 이동
        btnAddWork.setOnClickListener {
            startActivity(Intent(this, WorkdayAddActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }

    private fun renderMonth() {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        tvMonthTitle.text = String.format(Locale.KOREA, "%d년 %d월", year, month)

        val days = CalendarDataBuilder.buildMonthCells(year, month, workTagMap)
        adapter.submit(days)

        // 선택 유지(있으면)
        selectedKey?.let { adapter.setSelected(it) }
    }
}
