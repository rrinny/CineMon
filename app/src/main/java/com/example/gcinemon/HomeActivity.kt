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

// 홈 화면을 담당하며 일정, 급여, 공휴일 정보를 종합적으로 관리하는 Activity
class HomeActivity : AppCompatActivity() {

    // Home 화면 전용 ViewModel을 생성하여 UI 로직을 분리
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            AppDatabase.getInstance(this).scheduleDao()
        )
    }

    // 사용자 설정 및 공휴일 정보를 관리하기 위한 매니저
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var holidayManager: HolidayManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 사용자 설정 값을 관리하기 위한 PreferenceManager 초기화
        preferenceManager = PreferenceManager(this)

        // 사용자 정보를 불러와 인사말 및 초기 데이터 로드를 수행
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@HomeActivity)
            val user = db.userDao().getUser()

            user?.let {
                findViewById<TextView>(R.id.tvHello).text = "${it.nickname} 님 안녕하세요!"

                viewModel.refreshAll(getLocalStartDay())
            }
        }

        // 앱 실행 시 사용자 정보와 달력을 다시 로드하여 UI를 최신화
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@HomeActivity)
            val user = db.userDao().getUser()

            user?.let {
                findViewById<TextView>(R.id.tvHello).text = "${it.nickname} 님 안녕하세요!"

                // 앱 실행 시 로컬에 저장된 요일에 맞춰 달력 로드
                viewModel.refreshAll(getLocalStartDay())
            }
        }

        // 공휴일 정보 관리 객체 초기화
        holidayManager = HolidayManager(this)

        // 하단 네비게이션 바를 홈 탭에 바인딩
        BottomNavHelper.bind(this, BottomNavHelper.Tab.HOME)

        // 다가오는 근무 일정 수정 버튼 클릭 이벤트 처리
        findViewById<ImageView>(R.id.btnEditUpcoming).setOnClickListener {
            viewModel.nextSchedule.value?.let { schedule ->
                val tag = "WorkdayEditBottomSheet"
                val bottomSheet = WorkdayEditBottomSheet.newInstance(schedule)
                bottomSheet.show(supportFragmentManager, tag)
            }
        }

        // ViewModel 상태 변경을 관찰하여 UI를 갱신
        observeViewModel()

        // 공휴일 정보를 서버에서 조회하여 로컬에 저장
        setupHolidayRepository()

        // 근무 일정 수정 결과를 수신하여 DB에 반영
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

                    // 일정 변경 후 화면 데이터를 다시 갱신
                    viewModel.refreshAll(getLocalStartDay())
                }
            }
        }
    }

    // 로컬 설정에 저장된 주간 시작 요일을 DayOfWeek로 변환
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

    // ViewModel의 상태 변화를 lifecycle에 맞춰 안전하게 관찰
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 다음 근무까지 남은 시간을 UI에 반영
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

                // 주간 캘린더 상태를 관찰하여 날짜 UI를 갱신
                launch {
                    viewModel.weekStatus.collect { statusList ->
                        if (statusList.isEmpty()) return@collect

                        val tvMonth = findViewById<TextView>(R.id.tvMonth)
                        val today = LocalDate.now()
                        tvMonth.text = today.format(DateTimeFormatter.ofPattern("yyyy. MM"))

                        // 날짜, 요일, 배경 뷰를 리스트로 매핑하여 반복 처리
                        val bgViews = listOf(R.id.bgDay17, R.id.bgDay18, R.id.bgDay19, R.id.bgDay20, R.id.bgDay21, R.id.bgDay22, R.id.bgDay23).map { findViewById<View>(it) }
                        val dateViews = listOf(R.id.tvDate17, R.id.tvDate18, R.id.tvDate19, R.id.tvDate20, R.id.tvDate21, R.id.tvDate22, R.id.tvDate23).map { findViewById<TextView>(it) }
                        val dowViews = listOf(R.id.tvDow17, R.id.tvDow18, R.id.tvDow19, R.id.tvDow20, R.id.tvDow21, R.id.tvDow22, R.id.tvDow23).map { findViewById<TextView>(it) }

                        statusList.forEachIndexed { index, status ->
                            val bgView = bgViews[index]
                            val tvDate = dateViews[index]
                            val tvDow = dowViews[index]

                            val realDate = LocalDate.parse(status.date)
                            val dayOfWeek = realDate.dayOfWeek

                            // 날짜와 요일 텍스트를 설정
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

                            // 날짜 상태에 따라 배경 스타일을 변경
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

                            // 요일과 날짜 색상을 근무 여부에 따라 구분
                            val isSunday = (dayOfWeek == java.time.DayOfWeek.SUNDAY)
                            val isHoliday = false

                            val dowColor = when {
                                isSunday || isHoliday -> ContextCompat.getColor(this@HomeActivity, R.color.gc_blue_public)
                                status.hasSchedule -> ContextCompat.getColor(this@HomeActivity, R.color.gc_orange_main)
                                else -> ContextCompat.getColor(this@HomeActivity, R.color.gc_black_70)
                            }
                            tvDow?.setTextColor(dowColor)

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

                // 다가오는 근무 일정 카드 UI를 갱신
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

    // 다가오는 근무 일정 카드에 데이터를 바인딩
    private fun bindUpcomingCard(schedule: ScheduleEntity) {
        val tvUpcomingDate = findViewById<TextView>(R.id.tvUpcomingDate)
        val chipPosition = findViewById<TextView>(R.id.chipPosition)
        val chipWorkType = findViewById<TextView>(R.id.chipWorkType)

        try {
            // 날짜 문자열을 사용자 친화적인 형식으로 변환
            val date = java.time.LocalDate.parse(schedule.date)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("M월 d일 EEEE", java.util.Locale.KOREA)
            tvUpcomingDate.text = formatter.format(date)
        } catch (e: Exception) {
            // 날짜 파싱 실패 시 앱이 중단되지 않도록 기본값을 사용
            tvUpcomingDate.text = schedule.date
        }

        // 근무 포지션에 따라 칩 스타일을 적용
        chipPosition.text = schedule.position
        when (schedule.position) {
            "매점" -> chipPosition.setBackgroundResource(R.drawable.bg_chip_orange)
            "검표" -> chipPosition.setBackgroundResource(R.drawable.bg_chip_blue)
            else -> chipPosition.setBackgroundResource(R.drawable.bg_chip_orange)
        }

        // 근무 타입에 따라 시간 정보와 색상을 함께 표시
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

        // 메모가 없을 경우 기본 안내 문구를 표시
        val tvMemo = findViewById<TextView>(R.id.tvMemo)
        tvMemo.text = if (schedule.memo.isNullOrBlank()) "아직 메모가 없어요!" else schedule.memo
    }

    // 공휴일 API를 호출하여 로컬에 공휴일 정보를 저장
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