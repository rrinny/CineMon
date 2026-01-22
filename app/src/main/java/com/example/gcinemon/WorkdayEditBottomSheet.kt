package com.example.gcinemon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
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

        val chipSelectedPos = view.findViewById<Chip>(R.id.chipSelectedPos)
        val chipSelectedType = view.findViewById<Chip>(R.id.chipSelectedType)

        val chipGroupPosition = view.findViewById<ChipGroup>(R.id.chipGroupPosition)
        val chipGroupWorkType = view.findViewById<ChipGroup>(R.id.chipGroupWorkType)

        chipGroupPosition.isSingleSelection = true
        chipGroupWorkType.isSingleSelection = true
        ensureChildrenCheckable(chipGroupPosition)
        ensureChildrenCheckable(chipGroupWorkType)

        chipSelectedPos.visibility = View.GONE
        chipSelectedType.visibility = View.GONE

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
                selectedWorkType = chip.text?.toString()
                chipSelectedType.text = selectedWorkType
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
                    KEY_POSITION to selectedPosition,
                    KEY_WORKTYPE to selectedWorkType
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
