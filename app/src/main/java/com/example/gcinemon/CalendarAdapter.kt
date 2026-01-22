package com.example.gcinemon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.Locale

class CalendarAdapter(
    private val onClickDay: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.VH>() {

    private val items = mutableListOf<CalendarDay>()
    private var selectedKey: String? = null

    fun submit(list: List<CalendarDay>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun setSelected(key: String) {
        selectedKey = key
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return VH(v, onClickDay)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], selectedKey)
    }

    override fun getItemCount(): Int = items.size

    class VH(
        itemView: View,
        private val onClickDay: (CalendarDay) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val tvHoliday: TextView = itemView.findViewById(R.id.tvHoliday)
        private val tvTag1: TextView = itemView.findViewById(R.id.tvTag1)
        private val tvTag2: TextView = itemView.findViewById(R.id.tvTag2)

        fun bind(day: CalendarDay, selectedKey: String?) {
            tvDay.text = day.day.toString()

            // 기본 색(이번달/이전달/다음달)
            if (day.isInMonth) {
                tvDay.setTextColor(itemView.context.getColor(R.color.gc_black_70))
            } else {
                tvDay.setTextColor(itemView.context.getColor(R.color.gc_black_20))
            }

            // 선택일
            if (day.key == selectedKey) {
                tvDay.setTextColor(itemView.context.getColor(R.color.gc_orange_main))
            }

            // holiday 라벨 (예: 새해)
            val holidayName = day.holidayName
            if (!holidayName.isNullOrBlank()) {
                tvHoliday.visibility = View.VISIBLE
                tvHoliday.text = holidayName
                tvDay.setTextColor(itemView.context.getColor(R.color.gc_blue_public))
            } else {
                tvHoliday.visibility = View.GONE
            }

            // 태그
            tvTag1.visibility = View.GONE
            tvTag2.visibility = View.GONE

            val tags = day.tags
            if (tags.isNotEmpty()) {
                bindTag(tvTag1, tags[0])
                tvTag1.visibility = View.VISIBLE
            }
            if (tags.size >= 2) {
                bindTag(tvTag2, tags[1])
                tvTag2.visibility = View.VISIBLE
            }

            itemView.setOnClickListener { onClickDay(day) }
        }

        private fun bindTag(tv: TextView, tag: WorkTag) {
            tv.text = tag.label

            val bgRes = when (tag.style) {
                TagStyle.ORANGE -> R.drawable.bg_tag_orange_50
                TagStyle.BLUE -> R.drawable.bg_tag_blue_50
                TagStyle.GREEN -> R.drawable.bg_tag_green_50
                TagStyle.HOLIDAY -> 0 // holiday는 tvHoliday로 표현
            }

            if (bgRes != 0) tv.setBackgroundResource(bgRes)
        }
    }
}

// 달력 한 칸 데이터
data class CalendarDay(
    val year: Int,
    val month: Int,   // 1~12
    val day: Int,
    val isInMonth: Boolean,
    val key: String,  // yyyyMMdd
    val holidayName: String? = null,
    val tags: List<WorkTag> = emptyList()
)

data class WorkTag(
    val label: String,
    val style: TagStyle
)

enum class TagStyle { ORANGE, BLUE, GREEN, HOLIDAY }

// 월 셀(42칸) 생성
object CalendarDataBuilder {

    fun buildMonthCells(
        year: Int,
        month: Int, // 1~12
        workTagMap: Map<String, List<WorkTag>>
    ): List<CalendarDay> {

        val result = mutableListOf<CalendarDay>()

        val cal = Calendar.getInstance(Locale.KOREA).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=일 ... 7=토
        val startOffset = firstDayOfWeek - Calendar.SUNDAY // 0~6

        // 이전달 마지막 날짜
        val prev = Calendar.getInstance(Locale.KOREA).apply {
            timeInMillis = cal.timeInMillis
            add(Calendar.MONTH, -1)
        }
        val prevLastDay = prev.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 이전달 채우기
        for (i in startOffset downTo 1) {
            val d = prevLastDay - i + 1
            val y = prev.get(Calendar.YEAR)
            val m = prev.get(Calendar.MONTH) + 1
            val key = keyOf(y, m, d)

            val (holiday, tags) = splitHoliday(workTagMap[key])
            result.add(
                CalendarDay(y, m, d, false, key, holidayName = holiday, tags = tags)
            )
        }

        // 이번달 채우기
        val thisMonthLastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (d in 1..thisMonthLastDay) {
            val key = keyOf(year, month, d)
            val (holiday, tags) = splitHoliday(workTagMap[key])
            result.add(
                CalendarDay(year, month, d, true, key, holidayName = holiday, tags = tags)
            )
        }

        // 다음달 채우기 (총 42칸 맞춤)
        val next = Calendar.getInstance(Locale.KOREA).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, 1)
        }

        var d = 1
        while (result.size < 42) {
            val y = next.get(Calendar.YEAR)
            val m = next.get(Calendar.MONTH) + 1
            val key = keyOf(y, m, d)

            val (holiday, tags) = splitHoliday(workTagMap[key])
            result.add(
                CalendarDay(y, m, d, false, key, holidayName = holiday, tags = tags)
            )
            d++
        }

        return result
    }

    private fun keyOf(y: Int, m: Int, d: Int): String {
        return String.format(Locale.US, "%04d%02d%02d", y, m, d)
    }

    // HOLIDAY는 tvHoliday로, 나머지는 tags로
    private fun splitHoliday(list: List<WorkTag>?): Pair<String?, List<WorkTag>> {
        if (list.isNullOrEmpty()) return null to emptyList()

        val holiday = list.firstOrNull { it.style == TagStyle.HOLIDAY }?.label
        val tags = list.filter { it.style != TagStyle.HOLIDAY }.take(2)
        return holiday to tags
    }
}
