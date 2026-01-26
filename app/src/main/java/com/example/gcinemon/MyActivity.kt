package com.example.gcinemon

import com.example.gcinemon.util.PreferenceManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.gcinemon.data.AppDatabase
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import android.widget.TextView

class MyActivity : AppCompatActivity() {

    // 사용자 설정 값을 관리하는 PreferenceManager
    private lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        // PreferenceManager 초기화
        prefs = PreferenceManager(this)

        BottomNavHelper.bind(this, BottomNavHelper.Tab.MY)

        val orange = ContextCompat.getColor(this, R.color.gc_orange_main)
        val grayStroke = ContextCompat.getColor(this, R.color.gc_black_40)
        val iconGray = ContextCompat.getColor(this, R.color.gc_black_70)
        val textGray = ContextCompat.getColor(this, R.color.gc_black_40)
        val textBlack = ContextCompat.getColor(this, R.color.gc_black_90)

        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rowPreset).setOnClickListener {
            startActivity(Intent(this, PresetSettingActivity::class.java))
        }

        // 사용자 닉네임 로드
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@MyActivity)
            val user = db.userDao().getUser()

            user?.let {
                findViewById<TextView>(R.id.tvMyName).text = it.nickname
            }
        }

        // 기본 시급 설정 입력 필드 바인딩 (단위: 원)
        bindEditableTextField(
            card = findViewById(R.id.cardWage),
            edit = findViewById(R.id.etWage),
            icon = findViewById(R.id.ivWageAction),
            strokeOrange = orange, strokeGray = grayStroke, iconGray = iconGray,
            textGray = textGray, textBlack = textBlack,
            inputTypeNumber = true,
            initialValue = prefs.baseWage.toString(),
            unit = "원",
            onSave = { newValue -> prefs.baseWage = newValue.toIntOrNull() ?: 10320 }
        )

        // 근무 시간 설정 입력 필드 바인딩 (단위: 시간)
        bindEditableTextField(
            card = findViewById(R.id.cardWork),
            edit = findViewById(R.id.etWork),
            icon = findViewById(R.id.ivWorkAction),
            strokeOrange = orange, strokeGray = grayStroke, iconGray = iconGray,
            textGray = textGray, textBlack = textBlack,
            inputTypeNumber = true,
            initialValue = prefs.workTime.toString(),
            unit = "시간",
            onSave = { newValue -> prefs.workTime = newValue.toFloatOrNull() ?: 7.5f }
        )

        // 휴게 시간 설정 입력 필드 바인딩 (단위: 분)
        bindEditableTextField(
            card = findViewById(R.id.cardRest),
            edit = findViewById(R.id.etRest),
            icon = findViewById(R.id.ivRestAction),
            strokeOrange = orange, strokeGray = grayStroke, iconGray = iconGray,
            textGray = textGray, textBlack = textBlack,
            inputTypeNumber = true,
            initialValue = prefs.restTime.toString(),
            unit = "분",
            onSave = { newValue -> prefs.restTime = newValue.toIntOrNull() ?: 30 }
        )

        // 주간 시작 요일 선택 필드 바인딩
        bindDayOfWeekPickerField(
            card = findViewById(R.id.cardWeekStart),
            edit = findViewById(R.id.etWeekStart),
            icon = findViewById(R.id.ivWeekStartAction),
            strokeOrange = orange, strokeGray = grayStroke, iconGray = iconGray,
            textGray = textGray, textBlack = textBlack,
            initialValue = prefs.startDayOfWeek,
            onSave = { newValue -> prefs.startDayOfWeek = newValue }
        )

        // 월급날 선택 필드 바인딩
        bindDayOfMonthPickerField(
            card = findViewById(R.id.cardSalaryStart),
            edit = findViewById(R.id.etSalaryStart),
            icon = findViewById(R.id.ivSalaryStartAction),
            strokeOrange = orange, strokeGray = grayStroke, iconGray = iconGray,
            textGray = textGray, textBlack = textBlack,
            initialValue = "${prefs.payday}일",
            onSave = { newValue -> prefs.payday = newValue.replace("일", "").toIntOrNull() ?: 5 }
        )

        // 세금/주휴수당 스위치 초기화
        val swTax = findViewById<SwitchMaterial>(R.id.swTax)
        val swWeekly = findViewById<SwitchMaterial>(R.id.swWeekly)

        // 스위치 상태 로드
        swTax.isChecked = prefs.isTaxEnabled
        swWeekly.isChecked = prefs.isAllowanceEnabled

        // 스위치 색상 적용
        applySwitchColors(swTax, onColor = orange)
        applySwitchColors(swWeekly, onColor = orange)

        // 세금 계산 여부 저장
        swTax.setOnCheckedChangeListener { _, isChecked -> prefs.isTaxEnabled = isChecked }

        // 주휴 수당 계산 여부 저장
        swWeekly.setOnCheckedChangeListener { _, isChecked -> prefs.isAllowanceEnabled = isChecked }
    }

    private fun bindEditableTextField(
        card: MaterialCardView, edit: EditText, icon: ImageView,
        strokeOrange: Int, strokeGray: Int, iconGray: Int, textGray: Int, textBlack: Int,
        inputTypeNumber: Boolean, initialValue: String,
        unit: String,
        onSave: (String) -> Unit
    ) {
        var editing = false
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val decimalFormat = DecimalFormat("#,###")

        // 숫자에 콤마를 적용하는 포맷 함수
        fun formatValue(value: String): String {
            return if (value.isNotEmpty()) {
                try { decimalFormat.format(value.toLong()) } catch (e: Exception) { value }
            } else ""
        }

        // 초기 값에 콤마와 단위 적용
        val initialFormatted = formatValue(initialValue)
        if (initialFormatted.isNotEmpty()) {
            edit.setText("${initialFormatted}${unit}")
            edit.setTextColor(textBlack)
        } else {
            edit.setText("")
            edit.setTextColor(textGray)
        }

        // 숫자 입력 타입 설정
        if (inputTypeNumber) edit.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        // 편집 상태 전환 처리
        fun setState(on: Boolean) {
            editing = on
            if (on) {
                // 편집 시작 시 콤마와 단위 제거
                val currentRaw = edit.text.toString().replace(",", "").replace(unit, "").trim()
                edit.setText(currentRaw)

                card.setStrokeColor(strokeOrange)
                icon.setImageResource(R.drawable.icon_check_on)
                icon.setColorFilter(strokeOrange)
                edit.isEnabled = true
                edit.isFocusable = true
                edit.isFocusableInTouchMode = true
                edit.isCursorVisible = true
                edit.setTextColor(textBlack)
                edit.requestFocus()
                edit.setSelection(edit.text?.length ?: 0)
                imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT)
            } else {
                // 편집 종료 시 값 저장 및 포맷 적용
                val rawValue = edit.text.toString().replace(",", "").replace(unit, "").trim()
                onSave(rawValue)

                val formatted = formatValue(rawValue)
                if (formatted.isNotEmpty()) {
                    edit.setText("${formatted}${unit}")
                    edit.setTextColor(textBlack)
                } else {
                    edit.setTextColor(textGray)
                }

                card.setStrokeColor(strokeGray)
                icon.setImageResource(R.drawable.icon_edit)
                icon.setColorFilter(iconGray)
                edit.isEnabled = false
                edit.isFocusable = false
                edit.isFocusableInTouchMode = false
                edit.isCursorVisible = false
                imm.hideSoftInputFromWindow(edit.windowToken, 0)
                edit.clearFocus()
            }
        }

        icon.setOnClickListener { setState(!editing) }
        card.setOnClickListener { if (!editing) setState(true) }
        edit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { setState(false); true } else false
        }
    }

    // 요일 선택용 NumberPicker 바인딩
    private fun bindDayOfWeekPickerField(
        card: MaterialCardView, edit: EditText, icon: ImageView,
        strokeOrange: Int, strokeGray: Int, iconGray: Int, textGray: Int, textBlack: Int,
        initialValue: String, onSave: (String) -> Unit
    ) {
        val daysShort = arrayOf("일", "월", "화", "수", "목", "금", "토")
        val daysFull = arrayOf("일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일")

        // 초기 요일 값 세팅
        edit.setText(initialValue)
        edit.setTextColor(textBlack)

        // 요일 선택 다이얼로그 표시
        fun openDialog() {
            val picker = NumberPicker(this).apply {
                minValue = 0; maxValue = 6; displayedValues = daysShort; wrapSelectorWheel = true
                value = daysFull.indexOf(edit.text.toString()).let { if (it >= 0) it else 0 }
            }
            AlertDialog.Builder(this).setTitle("주간 시작 요일").setView(picker)
                .setPositiveButton("선택") { _, _ ->
                    val selected = daysFull[picker.value]
                    edit.setText(selected)
                    onSave(selected)
                }.setNegativeButton("취소", null).show()
        }

        icon.setOnClickListener { openDialog() }
        card.setOnClickListener { openDialog() }
    }

    // 월급날 선택용 NumberPicker 바인딩
    private fun bindDayOfMonthPickerField(
        card: MaterialCardView, edit: EditText, icon: ImageView,
        strokeOrange: Int, strokeGray: Int, iconGray: Int, textGray: Int, textBlack: Int,
        initialValue: String, onSave: (String) -> Unit
    ) {

        // 초기 월급날 표시
        edit.setText(initialValue)
        edit.setTextColor(textBlack)

        fun openDialog() {
            val picker = NumberPicker(this).apply {
                minValue = 1; maxValue = 31; wrapSelectorWheel = true
                value = edit.text.toString().replace("일", "").toIntOrNull() ?: 1
            }
            AlertDialog.Builder(this).setTitle("월급날").setView(picker)
                .setPositiveButton("선택") { _, _ ->
                    val selected = "${picker.value}일"
                    edit.setText(selected)
                    onSave(selected)
                }.setNegativeButton("취소", null).show()
        }

        icon.setOnClickListener { openDialog() }
        card.setOnClickListener { openDialog() }
    }

    private fun applySwitchColors(sw: SwitchMaterial, onColor: Int) {
        val offTrack = ContextCompat.getColor(this, R.color.gc_black_20)
        val thumb = android.graphics.Color.WHITE
        sw.trackTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)), intArrayOf(onColor, offTrack))
        sw.thumbTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)), intArrayOf(thumb, thumb))
    }
}