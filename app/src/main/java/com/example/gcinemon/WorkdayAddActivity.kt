package com.example.gcinemon

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WorkdayAddActivity : AppCompatActivity() {

    private lateinit var vpWeek: ViewPager2
    private lateinit var tvDateTitle: TextView

    private lateinit var chipSelectedPos: Chip
    private lateinit var chipSelectedType: Chip

    private lateinit var chipPosStore: Chip
    private lateinit var chipPosTicket: Chip
    private lateinit var chipTypeOpen: Chip
    private lateinit var chipTypeMiddle: Chip
    private lateinit var chipTypeClose: Chip

    private lateinit var edtMemo: EditText
    private lateinit var btnSave: MaterialButton

    private val centerPosition = 1000
    private var currentPagerPosition = centerPosition

    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedDow: Int = selectedDate.get(Calendar.DAY_OF_WEEK)

    private lateinit var weekPagerAdapter: WeekPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workday_add)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        vpWeek = findViewById(R.id.vpWeek)
        tvDateTitle = findViewById(R.id.tvDateTitle)

        chipSelectedPos = findViewById(R.id.chipSelectedPos)
        chipSelectedType = findViewById(R.id.chipSelectedType)

        chipPosStore = findViewById(R.id.chipPosStore)
        chipPosTicket = findViewById(R.id.chipPosTicket)
        chipTypeOpen = findViewById(R.id.chipTypeOpen)
        chipTypeMiddle = findViewById(R.id.chipTypeMiddle)
        chipTypeClose = findViewById(R.id.chipTypeClose)

        edtMemo = findViewById(R.id.edtMemo)
        btnSave = findViewById(R.id.btnSave)

        chipSelectedPos.visibility = View.GONE
        chipSelectedType.visibility = View.GONE

        chipSelectedPos.setOnCloseIconClickListener {
            chipPosStore.isChecked = false
            chipPosTicket.isChecked = false
            chipSelectedPos.visibility = View.GONE
        }

        chipSelectedType.setOnCloseIconClickListener {
            chipTypeOpen.isChecked = false
            chipTypeMiddle.isChecked = false
            chipTypeClose.isChecked = false
            chipSelectedType.visibility = View.GONE
        }

        bindChipSelections()
        setupWeekPager()
        updateDateTitle()

        btnSave.setOnClickListener {
            val position = when {
                chipPosStore.isChecked -> "매점"
                chipPosTicket.isChecked -> "검표"
                else -> ""
            }

            val workType = when {
                chipTypeOpen.isChecked -> "오픈 07:00~11:30"
                chipTypeMiddle.isChecked -> "미들 11:30~18:30"
                chipTypeClose.isChecked -> "마감 18:30~00:00"
                else -> ""
            }

            val memo = edtMemo.text?.toString().orEmpty()

            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun bindChipSelections() {
        chipPosStore.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showSelectedChip(chipSelectedPos, "매점", "#FE6301", "#33FE6301")
            } else if (!chipPosTicket.isChecked) {
                chipSelectedPos.visibility = View.GONE
            }
        }

        chipPosTicket.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showSelectedChip(chipSelectedPos, "검표", "#0073FF", "#330073FF")
            } else if (!chipPosStore.isChecked) {
                chipSelectedPos.visibility = View.GONE
            }
        }

        chipTypeOpen.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showSelectedChip(chipSelectedType, "오픈 07:00~11:30", "#FE6301", "#33FE6301")
            } else if (!chipTypeMiddle.isChecked && !chipTypeClose.isChecked) {
                chipSelectedType.visibility = View.GONE
            }
        }

        chipTypeMiddle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showSelectedChip(chipSelectedType, "미들 11:30~18:30", "#06AF00", "#3306AF00")
            } else if (!chipTypeOpen.isChecked && !chipTypeClose.isChecked) {
                chipSelectedType.visibility = View.GONE
            }
        }

        chipTypeClose.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showSelectedChip(chipSelectedType, "마감 18:30~00:00", "#0073FF", "#330073FF")
            } else if (!chipTypeOpen.isChecked && !chipTypeMiddle.isChecked) {
                chipSelectedType.visibility = View.GONE
            }
        }
    }

    private fun setupWeekPager() {
        weekPagerAdapter = WeekPagerAdapter(
            baseDate = selectedDate,
            getSelectedDate = { selectedDate },
            onDayClick = { clicked ->
                selectedDate = clicked
                selectedDow = clicked.get(Calendar.DAY_OF_WEEK)
                updateDateTitle()
                weekPagerAdapter.notifyItemChanged(currentPagerPosition)
            }
        )

        vpWeek.adapter = weekPagerAdapter
        vpWeek.setCurrentItem(centerPosition, false)

        vpWeek.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val prev = currentPagerPosition
                currentPagerPosition = position

                val weekStart = weekPagerAdapter.weekStartFor(position)
                val newSelected = (weekStart.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, selectedDow - Calendar.SUNDAY)
                }
                selectedDate = newSelected

                updateDateTitle()

                weekPagerAdapter.notifyItemChanged(prev)
                weekPagerAdapter.notifyItemChanged(position)
            }
        })
    }

    private fun updateDateTitle() {
        val fmt = SimpleDateFormat("M월 d일 EEEE", Locale.KOREA)
        tvDateTitle.text = fmt.format(selectedDate.time)
    }

    private fun showSelectedChip(chip: Chip, text: String, stroke: String, bg: String) {
        chip.text = text
        chip.chipStrokeColor = ColorStateList.valueOf(Color.parseColor(stroke))
        chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(bg))
        chip.visibility = View.VISIBLE
    }
}

private class WeekPagerAdapter(
    private val baseDate: Calendar,
    private val getSelectedDate: () -> Calendar,
    private val onDayClick: (Calendar) -> Unit
) : RecyclerView.Adapter<WeekPagerAdapter.WeekViewHolder>() {

    private val center = 1000
    private val pageCount = 2001

    private val dowLabels = arrayOf("일", "월", "화", "수", "목", "금", "토")

    private val baseWeekStart: Calendar = startOfWeek(baseDate)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_week_card, parent, false)
        return WeekViewHolder(v, dowLabels, getSelectedDate, onDayClick)
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        holder.bind(weekStartFor(position))
    }

    override fun getItemCount(): Int = pageCount

    fun weekStartFor(position: Int): Calendar {
        val weekStart = (baseWeekStart.clone() as Calendar)
        val diffWeeks = position - center
        weekStart.add(Calendar.DAY_OF_MONTH, diffWeeks * 7)
        return weekStart
    }

    class WeekViewHolder(
        itemView: View,
        private val dowLabels: Array<String>,
        private val getSelectedDate: () -> Calendar,
        private val onDayClick: (Calendar) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvWeekMonth: TextView = itemView.findViewById(R.id.tvWeekMonth)
        private val rvWeekDays: RecyclerView = itemView.findViewById(R.id.rvWeekDays)

        fun bind(weekStart: Calendar) {
            val mid = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 3) }
            val monthFmt = SimpleDateFormat("yyyy년 M월", Locale.KOREA)
            tvWeekMonth.text = monthFmt.format(mid.time)

            val days = ArrayList<Calendar>(7)
            for (i in 0..6) {
                val d = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i) }
                days.add(d)
            }

            rvWeekDays.layoutManager = GridLayoutManager(itemView.context, 7)
            rvWeekDays.adapter = WeekDayAdapter(dowLabels, days, getSelectedDate(), onDayClick)
            rvWeekDays.itemAnimator = null
        }
    }
}

private class WeekDayAdapter(
    private val dowLabels: Array<String>,
    private val days: List<Calendar>,
    private val selectedDate: Calendar,
    private val onDayClick: (Calendar) -> Unit
) : RecyclerView.Adapter<WeekDayAdapter.DayVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_week_day, parent, false)
        return DayVH(v)
    }

    override fun onBindViewHolder(holder: DayVH, position: Int) {
        val day = days[position]
        holder.bind(day, dowLabels, isSameDay(day, selectedDate), onDayClick)
    }

    override fun getItemCount(): Int = days.size

    class DayVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardDay)
        private val tvDow: TextView = itemView.findViewById(R.id.tvDow)
        private val tvDom: TextView = itemView.findViewById(R.id.tvDom)

        fun bind(day: Calendar, dowLabels: Array<String>, selected: Boolean, onDayClick: (Calendar) -> Unit) {
            val dow = day.get(Calendar.DAY_OF_WEEK) // 1..7 (일..토)
            tvDow.text = dowLabels[dow - 1]
            tvDom.text = day.get(Calendar.DAY_OF_MONTH).toString()

            if (selected) {
                card.strokeWidth = dp(card, 1)
                card.strokeColor = Color.parseColor("#FE6301")
                card.setCardBackgroundColor(Color.WHITE)
                card.cardElevation = dp(card, 4).toFloat()
            } else {
                card.strokeWidth = 0
                card.setCardBackgroundColor(Color.TRANSPARENT)
                card.cardElevation = 0f
            }

            card.setOnClickListener {
                onDayClick(day.clone() as Calendar)
            }
        }

        private fun dp(view: View, dp: Int): Int {
            val density = view.resources.displayMetrics.density
            return (dp * density).toInt()
        }
    }
}

private fun startOfWeek(date: Calendar): Calendar {
    val c = (date.clone() as Calendar)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)

    val dow = c.get(Calendar.DAY_OF_WEEK) // 1..7 (일..토)
    c.add(Calendar.DAY_OF_MONTH, -(dow - Calendar.SUNDAY))
    return c
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean {
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
            a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH)
}
