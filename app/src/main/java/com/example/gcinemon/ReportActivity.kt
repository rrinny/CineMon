package com.example.gcinemon

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.example.gcinemon.data.AppDatabase
import com.example.gcinemon.util.HolidayManager
import com.example.gcinemon.util.PreferenceManager
import com.example.gcinemon.util.SalaryCalculator
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportActivity : AppCompatActivity() {

    // 급여 명세서(인보이스) 카드가 열려 있는지 여부
    private var isInvoiceOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        BottomNavHelper.bind(this, BottomNavHelper.Tab.REPORT)

        setupToggleUI()
        loadReportData()
        loadPastSalaryList()

        // 사용자 닉네임을 헤더에 표시
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@ReportActivity)
            val user = db.userDao().getUser()

            user?.let {
                findViewById<TextView>(R.id.tvHeaderLine1).text = "${it.nickname} 님의"
            }
        }
    }

    private fun setupToggleUI() {
        val svReport = findViewById<NestedScrollView>(R.id.svReport)
        val rowInvoiceToggle = findViewById<ConstraintLayout>(R.id.rowInvoiceToggle)
        val cardInvoice = findViewById<LinearLayout>(R.id.cardInvoice)
        val ivInvoiceArrow = findViewById<ImageView>(R.id.ivInvoiceArrow)

        cardInvoice.visibility = View.GONE
        ivInvoiceArrow.setImageResource(R.drawable.icon_down)

        rowInvoiceToggle.setOnClickListener {
            isInvoiceOpen = !isInvoiceOpen
            if (isInvoiceOpen) {
                cardInvoice.visibility = View.VISIBLE
                ivInvoiceArrow.setImageResource(R.drawable.icon_up)
                cardInvoice.post { svReport.smoothScrollTo(0, cardInvoice.bottom) }
            } else {
                cardInvoice.visibility = View.GONE
                ivInvoiceArrow.setImageResource(R.drawable.icon_down)
            }
        }
    }

    // 현재 월 급여 리포트 계산 및 표시
    private fun loadReportData() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val yearMonth = String.format("%04d-%02d", year, month)

        findViewById<TextView>(R.id.tvHeaderYm).text = "$year. $month"

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@ReportActivity)

            // 해당 월의 근무 스케줄 조회
            val schedules = db.scheduleDao().getLatestSchedulesOfMonth(yearMonth)

            val prefs = PreferenceManager(this@ReportActivity)
            val holidayManager = HolidayManager(this@ReportActivity)
            val calculator = SalaryCalculator(prefs)

            // 급여 계산
            val result = calculator.calculate(schedules, holidayManager)

            // 계산 결과 UI 반영
            findViewById<TextView>(R.id.tvHeaderMoney).text = String.format("%,d", result.totalPay)
            findViewById<TextView>(R.id.tvBasePay).text = String.format("%,d원", result.basePay)
            findViewById<TextView>(R.id.tvWeeklyPay).text = String.format("%,d원", result.weeklyBonus)
            findViewById<TextView>(R.id.tvHolidayPay).text = String.format("%,d원", result.holidayBonus)
            findViewById<TextView>(R.id.tvNightPay).text = String.format("%,d원", result.nightBonus)
        }
    }

    // 이전 월 (최대 3개월) 급여 요약 리포트 표시
    private fun loadPastSalaryList() {
        val cards = listOf(findViewById<View>(R.id.cardPast12), findViewById<View>(R.id.cardPast11), findViewById<View>(R.id.cardPast10))
        val yearLabels = listOf(findViewById<TextView>(R.id.tvYear1), findViewById<TextView>(R.id.tvYear2), findViewById<TextView>(R.id.tvYear3))
        val monthTexts = listOf(findViewById<TextView>(R.id.tvMonth12), findViewById<TextView>(R.id.tvMonth11), findViewById<TextView>(R.id.tvMonth10))
        val moneyTexts = listOf(findViewById<TextView>(R.id.tvMoney12), findViewById<TextView>(R.id.tvMoney11), findViewById<TextView>(R.id.tvMoney10))

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@ReportActivity)
            val holidayManager = HolidayManager(this@ReportActivity)
            val prefs = PreferenceManager(this@ReportActivity)
            val calculator = SalaryCalculator(prefs)

            // 근무 기록이 있는 모든 월 조회
            val allMonths = db.scheduleDao().getAllWorkedMonths()

            // 현재 월 조회
            val currentMonth = String.format("%04d-%02d", Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1)

            // 현재 월 제외, 최근 3개월만 사용
            val targetMonths = allMonths.filter { it != currentMonth }.take(3)

            cards.forEach { it.visibility = View.GONE }
            yearLabels.forEach { it.visibility = View.GONE }

            var lastDisplayedYear = ""

            targetMonths.forEachIndexed { index, yearMonth ->
                val schedules = db.scheduleDao().getLatestSchedulesOfMonth(yearMonth)
                val result = calculator.calculate(schedules, holidayManager)

                val split = yearMonth.split("-")
                val currentYear = split[0]
                val monthLabel = "${split[1].toInt()}월"

                // 연도가 바뀌는 경우에만 연도 표시
                if (currentYear != lastDisplayedYear) {
                    yearLabels[index].text = currentYear
                    yearLabels[index].visibility = View.VISIBLE
                    lastDisplayedYear = currentYear
                }

                // 월 / 금액 표시
                monthTexts[index].text = monthLabel
                moneyTexts[index].text = String.format("%,d원", result.totalPay)
                cards[index].visibility = View.VISIBLE
            }
        }
    }
}