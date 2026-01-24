package com.example.gcinemon

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gcinemon.data.AppDatabase
import com.example.gcinemon.data.entity.ScheduleEntity
import com.example.gcinemon.data.repository.HolidayRepository
import com.example.gcinemon.util.HolidayManager
import com.example.gcinemon.util.PreferenceManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            AppDatabase.getInstance(this).scheduleDao()
        )
    }

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var holidayManager: HolidayManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // PreferenceManager 초기화
        preferenceManager = PreferenceManager(this)

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@HomeActivity)
            val user = db.userDao().getUser()

            user?.let {
                findViewById<TextView>(R.id.tvHello).text = "${it.nickname} 님 안녕하세요!"

                viewModel.refreshAll(getLocalStartDay())
            }
        }

        // 유저 정보 로드 및 초기 UI 새로고침
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@HomeActivity)
            val user = db.userDao().getUser()

            user?.let {
                findViewById<TextView>(R.id.tvHello).text = "${it.nickname} 님 안녕하세요!"

                // 앱 실행 시 로컬에 저장된 요일에 맞춰 달력 로드
                viewModel.refreshAll(getLocalStartDay())
            }
        }

        holidayManager = HolidayManager(this)
        BottomNavHelper.bind(this, BottomNavHelper.Tab.HOME)

        findViewById<ImageView>(R.id.btnEditUpcoming).setOnClickListener {
            viewModel.nextSchedule.value?.let { schedule ->
                val tag = "WorkdayEditBottomSheet"
                val bottomSheet = WorkdayEditBottomSheet.newInstance(schedule)
                bottomSheet.show(supportFragmentManager, tag)
            }
        }

        observeViewModel()
        setupHolidayRepository()

        supportFragmentManager.setFragmentResultListener(
            WorkdayEditBottomSheet.RESULT_KEY,
            this
        ) { _, bundle ->
            val date = bundle.getString(WorkdayEditBottomSheet.KEY_DATE)
            val pos = bundle.getString(WorkdayEditBottomSheet.KEY_POSITION)
            val type = bundle.getString(WorkdayEditBottomSheet.KEY_WORKTYPE)
            val memo = bundle.getString(WorkdayEditBottomSheet.KEY_MEMO)

            if (date != null) {
                lifecycleScope.launch {
                    val db = AppDatabase.getInstance(this@HomeActivity)
                    val updatedSchedule = ScheduleEntity(
                        date = date,
                        position = pos ?: "",
                        workType = type ?: "",
                        memo = memo ?: ""
                    )
                    db.scheduleDao().insertSchedule(updatedSchedule)

                    viewModel.refreshAll(getLocalStartDay())
                }
            }
        }
    }

    private fun getLocalStartDay(): java.time.DayOfWeek {
        val savedDay = preferenceManager.startDayOfWeek

        return when (savedDay) {
            "월요일" -> java.time.DayOfWeek.MONDAY
            "화요일" -> java.time.DayOfWeek.TUESDAY
            "수요일" -> java.time.DayOfWeek.WEDNESDAY
            "목요일" -> java.time.DayOfWeek.THURSDAY
            "금요일" -> java.time.DayOfWeek.FRIDAY
            "토요일" -> java.time.DayOfWeek.SATURDAY
            "일요일" -> java.time.DayOfWeek.SUNDAY
            else -> java.time.DayOfWeek.MONDAY
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.remainingTime.collect { timeText ->
                        val tvOrange = findViewById<TextView>(R.id.tvRemainOrange)
                        val tvGray = findViewById<TextView>(R.id.tvRemainGray)
                        val tvNextWorkTitle = findViewById<TextView>(R.id.tvNextWorkTitle)

                        when (timeText) {
                            "오늘" -> {
                                tvNextWorkTitle.visibility = View.GONE
                                tvOrange.text = "오늘"
                                tvGray.text = " 근무가 있어요!"
                                tvGray.visibility = View.VISIBLE
                            }
                            "예정된 근무가 없어요!" -> {
                                tvNextWorkTitle.visibility = View.GONE
                                tvOrange.text = timeText
                                tvGray.visibility = View.GONE
                            }
                            else -> {
                                if (timeText.isNotEmpty()) {
                                    tvNextWorkTitle.visibility = View.VISIBLE
                                    tvOrange.text = timeText
                                    tvGray.text = " 남았어요!"
                                    tvGray.visibility = View.VISIBLE
                                } else {
                                    tvNextWorkTitle.visibility = View.GONE
                                    tvOrange.text = ""
                                    tvGray.text = ""
                                }
                            }
                        }
                    }
                }
                launch {
                    viewModel.weekStatus.collect { statusList ->
                        if (statusList.isEmpty()) return@collect

                        val tvMonth = findViewById<TextView>(R.id.tvMonth)
                        val today = LocalDate.now()
                        tvMonth.text = today.format(DateTimeFormatter.ofPattern("yyyy. MM"))

                        // 뷰 리스트 매핑
                        val bgViews = listOf(R.id.bgDay17, R.id.bgDay18, R.id.bgDay19, R.id.bgDay20, R.id.bgDay21, R.id.bgDay22, R.id.bgDay23).map { findViewById<View>(it) }
                        val dateViews = listOf(R.id.tvDate17, R.id.tvDate18, R.id.tvDate19, R.id.tvDate20, R.id.tvDate21, R.id.tvDate22, R.id.tvDate23).map { findViewById<TextView>(it) }
                        val dowViews = listOf(R.id.tvDow17, R.id.tvDow18, R.id.tvDow19, R.id.tvDow20, R.id.tvDow21, R.id.tvDow22, R.id.tvDow23).map { findViewById<TextView>(it) }

                        statusList.forEachIndexed { index, status ->
                            val bgView = bgViews[index]
                            val tvDate = dateViews[index]
                            val tvDow = dowViews[index]

                            val realDate = LocalDate.parse(status.date)
                            val dayOfWeek = realDate.dayOfWeek

                            // 텍스트 업데이트
                            tvDate?.text = status.dayNum
                            tvDow?.text = when(dayOfWeek) {
                                java.time.DayOfWeek.MONDAY -> "월"
                                java.time.DayOfWeek.TUESDAY -> "화"
                                java.time.DayOfWeek.WEDNESDAY -> "수"
                                java.time.DayOfWeek.THURSDAY -> "목"
                                java.time.DayOfWeek.FRIDAY -> "금"
                                java.time.DayOfWeek.SATURDAY -> "토"
                                java.time.DayOfWeek.SUNDAY -> "일"
                                else -> ""
                            }

                            when {
                                status.isToday -> {
                                    bgView?.visibility = View.VISIBLE
                                    bgView?.setBackgroundResource(R.drawable.bg_calendar_today)
                                }
                                status.isPast && status.hasSchedule -> {
                                    bgView?.visibility = View.VISIBLE
                                    bgView?.setBackgroundResource(R.drawable.bg_calendar_selected)
                                }
                                else -> {
                                    bgView?.visibility = View.INVISIBLE
                                }
                            }

                            val isSunday = (dayOfWeek == java.time.DayOfWeek.SUNDAY)
                            val isHoliday = false

                            // 요일(tvDow) 색상: 일요일/공휴일 (파랑) > 근무 (주황) > 평일 (검정)
                            val dowColor = when {
                                isSunday || isHoliday -> ContextCompat.getColor(this@HomeActivity, R.color.gc_blue_public)
                                status.hasSchedule -> ContextCompat.getColor(this@HomeActivity, R.color.gc_orange_main)
                                else -> ContextCompat.getColor(this@HomeActivity, R.color.gc_black_70)
                            }
                            tvDow?.setTextColor(dowColor)

                            // 날짜(tvDate) 색상: 근무 (주황) > 평일 (검정)
                            val dateColor = if (status.hasSchedule) {
                                ContextCompat.getColor(this@HomeActivity, R.color.gc_orange_main)
                            } else {
                                ContextCompat.getColor(this@HomeActivity, R.color.gc_black_70)
                            }
                            tvDate?.setTextColor(dateColor)

                            if (status.isPast && status.hasSchedule) {
                                val black = ContextCompat.getColor(this@HomeActivity, R.color.gc_black_70)
                                tvDate?.setTextColor(black)
                                tvDow?.setTextColor(black)
                            }
                        }
                    }
                }
                // 다가오는 근무 날 정보 업데이트
                launch {
                    viewModel.nextSchedule.collect { schedule ->
                        val cardView = findViewById<View>(R.id.cardUpcoming)
                        val tvUpcomingTitle = findViewById<TextView>(R.id.tvUpcomingTitle) // "다가오는 근무 날" 텍스트 ID
                        val btnEditUpcoming = findViewById<ImageView>(R.id.btnEditUpcoming) // 연필 버튼 ID

                        if (schedule != null) {
                            cardView.visibility = View.VISIBLE
                            tvUpcomingTitle.visibility = View.VISIBLE
                            btnEditUpcoming.visibility = View.VISIBLE
                            bindUpcomingCard(schedule)
                        } else {
                            cardView.visibility = View.GONE
                            tvUpcomingTitle.visibility = View.GONE
                            btnEditUpcoming.visibility = View.GONE
                        }
                    }
                }

            }
        }
    }

    private fun bindUpcomingCard(schedule: ScheduleEntity) {
        // 실제 날짜 데이터(예: 2026-01-28)를 가져와서 텍스트뷰에 세팅
        val tvUpcomingDate = findViewById<TextView>(R.id.tvUpcomingDate)
        val chipPosition = findViewById<TextView>(R.id.chipPosition)
        val chipWorkType = findViewById<TextView>(R.id.chipWorkType)

        try {
            // DB에 저장된 "yyyy-MM-dd" 형태를 읽어서 "M월 d일 EEEE" 형태로 변환
            val date = java.time.LocalDate.parse(schedule.date)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("M월 d일 EEEE", java.util.Locale.KOREA)
            tvUpcomingDate.text = formatter.format(date)
        } catch (e: Exception) {
            // 데이터 형식이 다를 경우를 대비한 기본값
            tvUpcomingDate.text = schedule.date
        }

        // 근무 포지션 세팅 및 색상 업데이트
        chipPosition.text = schedule.position
        when (schedule.position) {
            "매점" -> chipPosition.setBackgroundResource(R.drawable.bg_chip_orange)
            "검표" -> chipPosition.setBackgroundResource(R.drawable.bg_chip_blue)
            else -> chipPosition.setBackgroundResource(R.drawable.bg_chip_orange)
        }

        // 근무 타입 세팅 및 색상 업데이트
        val workTypeWithTime = when (schedule.workType) {
            "오픈" -> {
                chipWorkType.setBackgroundResource(R.drawable.bg_chip_orange)
                "오픈 09:00~15:30"
            }
            "미들" -> {
                chipWorkType.setBackgroundResource(R.drawable.bg_chip_green)
                "미들 11:30~18:30"
            }
            "마감" -> {
                chipWorkType.setBackgroundResource(R.drawable.bg_chip_blue)
                "마감 15:30~22:00"
            }
            else -> {
                chipWorkType.setBackgroundResource(R.drawable.bg_chip_orange)
                schedule.workType
            }
        }
        chipWorkType.text = workTypeWithTime

        // 메모 세팅
        val tvMemo = findViewById<TextView>(R.id.tvMemo)
        tvMemo.text = if (schedule.memo.isNullOrBlank()) "아직 메모가 없어요!" else schedule.memo
    }

    private fun setupHolidayRepository() {
        val holidayRepository = HolidayRepository(holidayManager)
        lifecycleScope.launch {
            Log.d("CineMon", "공휴일 정보 조회 API를 호출합니다.")
            val result = holidayRepository.refreshHolidays(2026)

            result.onSuccess {
                Log.d("CineMon", "공휴일 정보 조회에 성공했습니다.")
            }.onFailure {
                Log.e("CineMon", "공휴일 정보 조회에 실패했습니다. ${it.message}")
                it.printStackTrace()
            }
        }
    }
}