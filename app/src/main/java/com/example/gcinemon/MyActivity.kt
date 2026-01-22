package com.example.gcinemon

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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class MyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        // 하단 네비 -> MY 선택 상태 + 탭 이동
        BottomNavHelper.bind(this, BottomNavHelper.Tab.MY)

        // 프리셋 관리 페이지 이동
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rowPreset).setOnClickListener {
            startActivity(Intent(this, PresetSettingActivity::class.java))
        }

        // color.xml 리소스 사용
        val orange = ContextCompat.getColor(this, R.color.gc_orange_main)
        val grayStroke = ContextCompat.getColor(this, R.color.gc_black_40)
        val iconGray = ContextCompat.getColor(this, R.color.gc_black_70)
        val textGray = ContextCompat.getColor(this, R.color.gc_black_40)
        val textBlack = ContextCompat.getColor(this, R.color.gc_black_90)

        //  기본 시급 -> 텍스트 직접 입력, 초기값 없음
        bindEditableTextField(
            card = findViewById(R.id.cardWage),
            edit = findViewById(R.id.etWage),
            icon = findViewById(R.id.ivWageAction),
            strokeOrange = orange,
            strokeGray = grayStroke,
            iconGray = iconGray,
            textGray = textGray,
            textBlack = textBlack,
            inputTypeNumber = true,
            initialTextEmpty = true
        )

        // 기본 휴게 시간 -> 편집 토글(키보드 입력)
        bindEditableTextField(
            card = findViewById(R.id.cardRest),
            edit = findViewById(R.id.etRest),
            icon = findViewById(R.id.ivRestAction),
            strokeOrange = orange,
            strokeGray = grayStroke,
            iconGray = iconGray,
            textGray = textGray,
            textBlack = textBlack,
            inputTypeNumber = false,
            initialTextEmpty = false
        )

        // 주간 시작 요일 -> 다이얼
        bindDayOfWeekPickerField(
            card = findViewById(R.id.cardWeekStart),
            edit = findViewById(R.id.etWeekStart),
            icon = findViewById(R.id.ivWeekStartAction),
            strokeOrange = orange,
            strokeGray = grayStroke,
            iconGray = iconGray,
            textGray = textGray,
            textBlack = textBlack
        )

        // 월급 시작일 -> 다이얼
        bindDayOfMonthPickerField(
            card = findViewById(R.id.cardSalaryStart),
            edit = findViewById(R.id.etSalaryStart),
            icon = findViewById(R.id.ivSalaryStartAction),
            strokeOrange = orange,
            strokeGray = grayStroke,
            iconGray = iconGray,
            textGray = textGray,
            textBlack = textBlack
        )

        // 스위치 색
        applySwitchColors(findViewById(R.id.swTax), onColor = orange)
        applySwitchColors(findViewById(R.id.swWeekly), onColor = orange)
    }

    private fun bindEditableTextField(
        card: MaterialCardView,
        edit: EditText,
        icon: ImageView,
        strokeOrange: Int,
        strokeGray: Int,
        iconGray: Int,
        textGray: Int,
        textBlack: Int,
        inputTypeNumber: Boolean,
        initialTextEmpty: Boolean
    ) {
        var editing = false
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (initialTextEmpty) {
            edit.setText("")
            edit.setTextColor(textGray)
        }

        if (inputTypeNumber) {
            edit.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        fun setState(on: Boolean) {
            editing = on

            if (on) {
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
                card.setStrokeColor(strokeGray)
                icon.setImageResource(R.drawable.icon_edit)
                icon.setColorFilter(iconGray)

                edit.isEnabled = false
                edit.isFocusable = false
                edit.isFocusableInTouchMode = false
                edit.isCursorVisible = false

                if (edit.text.isNullOrBlank()) {
                    edit.setTextColor(textGray)
                } else {
                    edit.setTextColor(textBlack)
                }

                imm.hideSoftInputFromWindow(edit.windowToken, 0)
                edit.clearFocus()
            }
        }


        card.setStrokeColor(strokeGray)
        icon.setImageResource(R.drawable.icon_edit)
        icon.setColorFilter(iconGray)

        edit.isEnabled = false
        edit.isFocusable = false
        edit.isFocusableInTouchMode = false
        edit.isCursorVisible = false

        icon.setOnClickListener { setState(!editing) }
        card.setOnClickListener { if (!editing) setState(true) }

        edit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                setState(false)
                true
            } else false
        }
    }


    private fun bindDayOfWeekPickerField(
        card: MaterialCardView,
        edit: EditText,
        icon: ImageView,
        strokeOrange: Int,
        strokeGray: Int,
        iconGray: Int,
        textGray: Int,
        textBlack: Int
    ) {
        var editing = false
        val daysShort = arrayOf("일", "월", "화", "수", "목", "금", "토")
        val daysFull = arrayOf("일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일")

        fun openDialog() {
            val picker = NumberPicker(this).apply {
                minValue = 0
                maxValue = daysShort.size - 1
                displayedValues = daysShort
                wrapSelectorWheel = true

                val current = edit.text?.toString()?.trim()
                val idx = daysFull.indexOf(current)
                value = if (idx >= 0) idx else 0
            }

            AlertDialog.Builder(this)
                .setTitle("주간 시작 요일")
                .setView(picker)
                .setPositiveButton("선택") { _, _ ->
                    edit.setText(daysFull[picker.value])
                    edit.setTextColor(textBlack)
                }
                .setNegativeButton("취소", null)
                .show()
        }

        fun setState(on: Boolean) {
            editing = on
            if (on) {
                card.setStrokeColor(strokeOrange)
                icon.setImageResource(R.drawable.icon_check_on)
                icon.setColorFilter(strokeOrange)
            } else {
                card.setStrokeColor(strokeGray)
                icon.setImageResource(R.drawable.icon_edit)
                icon.setColorFilter(iconGray)

                if (edit.text.isNullOrBlank()) edit.setTextColor(textGray)
            }
        }


        setState(false)
        edit.isEnabled = false
        edit.isFocusable = false
        edit.isFocusableInTouchMode = false
        edit.isCursorVisible = false

        icon.setOnClickListener {
            if (!editing) {
                setState(true)
                openDialog()
            } else {
                setState(false)
            }
        }

        card.setOnClickListener {
            if (!editing) setState(true)
            openDialog()
        }
    }


    private fun bindDayOfMonthPickerField(
        card: MaterialCardView,
        edit: EditText,
        icon: ImageView,
        strokeOrange: Int,
        strokeGray: Int,
        iconGray: Int,
        textGray: Int,
        textBlack: Int
    ) {
        var editing = false

        fun openDialog() {
            val picker = NumberPicker(this).apply {
                minValue = 1
                maxValue = 31
                wrapSelectorWheel = true

                val current = edit.text?.toString()?.replace("일", "")?.trim()
                val currentInt = current?.toIntOrNull()
                value = currentInt ?: 1
            }

            AlertDialog.Builder(this)
                .setTitle("월급 시작일")
                .setView(picker)
                .setPositiveButton("선택") { _, _ ->
                    edit.setText("${picker.value}일")
                    edit.setTextColor(textBlack)
                }
                .setNegativeButton("취소", null)
                .show()
        }

        fun setState(on: Boolean) {
            editing = on
            if (on) {
                card.setStrokeColor(strokeOrange)
                icon.setImageResource(R.drawable.icon_check_on)
                icon.setColorFilter(strokeOrange)
            } else {
                card.setStrokeColor(strokeGray)
                icon.setImageResource(R.drawable.icon_edit)
                icon.setColorFilter(iconGray)

                if (edit.text.isNullOrBlank()) edit.setTextColor(textGray)
            }
        }


        setState(false)
        edit.isEnabled = false
        edit.isFocusable = false
        edit.isFocusableInTouchMode = false
        edit.isCursorVisible = false

        icon.setOnClickListener {
            if (!editing) {
                setState(true)
                openDialog()
            } else {
                setState(false)
            }
        }

        card.setOnClickListener {
            if (!editing) setState(true)
            openDialog()
        }
    }

    private fun applySwitchColors(sw: SwitchMaterial, onColor: Int) {
        val offTrack = ContextCompat.getColor(this, R.color.gc_black_20)
        val thumb = android.graphics.Color.WHITE

        sw.trackTintList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(onColor, offTrack)
        )

        sw.thumbTintList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(thumb, thumb)
        )
    }
}
