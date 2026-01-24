package com.example.gcinemon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import com.example.gcinemon.data.entity.ScheduleEntity
import com.google.android.material.R as MtrlR
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class WorkdayEditBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val RESULT_KEY = "workday_edit_result"
        const val KEY_POSITION = "position"
        const val KEY_WORKTYPE = "workType"
        const val KEY_MEMO = "memo"
        const val KEY_DATE = "date"

        fun newInstance(schedule: ScheduleEntity): WorkdayEditBottomSheet {
            val args = Bundle().apply {
                putString("date", schedule.date)
                putString("position", schedule.position)
                putString("workType", schedule.workType)
                putString("memo", schedule.memo)
            }
            return WorkdayEditBottomSheet().apply { arguments = args }
        }
    }

    private var selectedPosition: String? = null
    private var selectedWorkType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_workday_edit, container, false)
    }

    override fun onStart() {
        super.onStart()
        val sheet = dialog?.findViewById<View>(MtrlR.id.design_bottom_sheet) ?: return
        val behavior = BottomSheetBehavior.from(sheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 바인딩
        val tvBsDate = view.findViewById<TextView>(R.id.tvBsDate)
        val edtMemo = view.findViewById<EditText>(R.id.edtMemo)
        val chipSelectedPos = view.findViewById<Chip>(R.id.chipSelectedPos)
        val chipSelectedType = view.findViewById<Chip>(R.id.chipSelectedType)
        val chipGroupPosition = view.findViewById<ChipGroup>(R.id.chipGroupPosition)
        val chipGroupWorkType = view.findViewById<ChipGroup>(R.id.chipGroupWorkType)

        // 전달받은 데이터 세팅
        val date = arguments?.getString("date") ?: ""
        val savedPos = arguments?.getString("position")
        val savedType = arguments?.getString("workType")
        val savedMemo = arguments?.getString("memo")

        // 날짜 세팅 (2026-01-28 -> 1월 28일 수요일)
        if (date.isNotEmpty()) {
            try {
                // 날짜 데이터를 읽어 요일까지 포함된 포맷으로 변환
                val sdfSource = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA)
                val dateObj = sdfSource.parse(date)

                val sdfDisplay = java.text.SimpleDateFormat("M월 d일 EEEE", java.util.Locale.KOREA)
                view.findViewById<TextView>(R.id.tvBsDate).text = sdfDisplay.format(dateObj)
            } catch (e: Exception) {
                // 오류 발생 시 기본 포맷 반환
                view.findViewById<TextView>(R.id.tvBsDate).text = date
            }
        }

        // 메모 필드
        edtMemo.setText(savedMemo)

        // 기존 선택 상태 복구 로직
        ensureChildrenCheckable(chipGroupPosition)
        ensureChildrenCheckable(chipGroupWorkType)

        // 포지션 칩 복구
        for (i in 0 until chipGroupPosition.childCount) {
            val chip = chipGroupPosition.getChildAt(i) as? Chip
            if (chip?.text == savedPos) {
                chip?.isChecked = true
                selectedPosition = savedPos
                chipSelectedPos.text = savedPos
                chipSelectedPos.visibility = View.VISIBLE
                break
            }
        }

        // 근무 타입 칩 복구 (시간 포함 문자열 비교)
        for (i in 0 until chipGroupWorkType.childCount) {
            val chip = chipGroupWorkType.getChildAt(i) as? Chip
            if (chip?.text?.toString()?.startsWith(savedType ?: "") == true) {
                chip.isChecked = true
                selectedWorkType = savedType
                chipSelectedType.text = chip.text
                chipSelectedType.visibility = View.VISIBLE
                break
            }
        }

        // 리스너 설정
        chipGroupPosition.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                selectedPosition = null
                chipSelectedPos.visibility = View.GONE
            } else {
                val chip = group.findViewById<Chip>(checkedIds.first())
                selectedPosition = chip.text?.toString()
                chipSelectedPos.text = selectedPosition
                chipSelectedPos.visibility = View.VISIBLE
            }
        }

        chipGroupWorkType.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                selectedWorkType = null
                chipSelectedType.visibility = View.GONE
            } else {
                val chip = group.findViewById<Chip>(checkedIds.first())
                selectedWorkType = chip.text?.toString()?.split(" ")?.get(0)
                chipSelectedType.text = chip.text
                chipSelectedType.visibility = View.VISIBLE
            }
        }

        chipSelectedPos.setOnCloseIconClickListener {
            selectedPosition = null
            chipSelectedPos.visibility = View.GONE
            chipGroupPosition.clearCheck()
        }

        chipSelectedType.setOnCloseIconClickListener {
            selectedWorkType = null
            chipSelectedType.visibility = View.GONE
            chipGroupWorkType.clearCheck()
        }

        view.findViewById<View>(R.id.btnSave).setOnClickListener {
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                bundleOf(
                    KEY_DATE to date, // 원래 날짜를 넘겨줘야 DB에서 찾아서 업데이트함
                    KEY_POSITION to selectedPosition,
                    KEY_WORKTYPE to selectedWorkType,
                    KEY_MEMO to edtMemo.text.toString()
                )
            )
            dismiss()
        }
    }

    private fun ensureChildrenCheckable(group: ChipGroup) {
        for (i in 0 until group.childCount) {
            val v = group.getChildAt(i)
            if (v is Chip) {
                v.isCheckable = true
            }
        }
    }
}