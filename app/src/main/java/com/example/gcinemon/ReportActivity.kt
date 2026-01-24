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

    private var isInvoiceOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        BottomNavHelper.bind(this, BottomNavHelper.Tab.REPORT)

        setupToggleUI()
        loadReportData()
        loadPastSalaryList()

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

    private fun loadReportData() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val yearMonth = String.format("%04d-%02d", year, month)

        findViewById<TextView>(R.id.tvHeaderYm).text = "$year. $month"

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@ReportActivity)
            val schedules = db.scheduleDao().getLatestSchedulesOfMonth(yearMonth)

            val prefs = PreferenceManager(this@ReportActivity)
            val holidayManager = HolidayManager(this@ReportActivity)
            val calculator = SalaryCalculator(prefs)

            val result = calculator.calculate(schedules, holidayManager)

            findViewById<TextView>(R.id.tvHeaderMoney).text = String.format("%,d", result.totalPay)
            findViewById<TextView>(R.id.tvBasePay).text = String.format("%,d원", result.basePay)
            findViewById<TextView>(R.id.tvWeeklyPay).text = String.format("%,d원", result.weeklyBonus)
            findViewById<TextView>(R.id.tvHolidayPay).text = String.format("%,d원", result.holidayBonus)
            findViewById<TextView>(R.id.tvNightPay).text = String.format("%,d원", result.nightBonus)
        }
    }

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

            val allMonths = db.scheduleDao().getAllWorkedMonths()
            val currentMonth = String.format("%04d-%02d", Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1)

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

                if (currentYear != lastDisplayedYear) {
                    yearLabels[index].text = currentYear
                    yearLabels[index].visibility = View.VISIBLE
                    lastDisplayedYear = currentYear
                }

                monthTexts[index].text = monthLabel
                moneyTexts[index].text = String.format("%,d원", result.totalPay)
                cards[index].visibility = View.VISIBLE
            }
        }
    }
}