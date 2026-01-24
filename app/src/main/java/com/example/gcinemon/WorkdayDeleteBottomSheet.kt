package com.example.gcinemon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.gcinemon.data.AppDatabase
import com.google.android.material.R as MtrlR
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkdayDeleteBottomSheet(
    private val dateKey: String,
    private val onDeleted: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_workday_delete, container, false)
    }

    override fun onStart() {
        super.onStart()
        val sheet = dialog?.findViewById<View>(MtrlR.id.design_bottom_sheet) ?: return
        val behavior = BottomSheetBehavior.from(sheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sdfSource = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.KOREA)
        val date = sdfSource.parse(dateKey)

        val sdfDisplay = java.text.SimpleDateFormat("M월 d일 EEEE", java.util.Locale.KOREA)
        val displayDate = sdfDisplay.format(date) // "1월 23일 금요일"

        view.findViewById<TextView>(R.id.tvDeleteTitle).text = "$displayDate\n근무 일정을 삭제할까요?"

        view.findViewById<View>(R.id.btnDelete).setOnClickListener {
            val dbDate = "${dateKey.substring(0,4)}-${dateKey.substring(4,6)}-${dateKey.substring(6,8)}"

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(requireContext())
                db.scheduleDao().deleteScheduleByDate(dbDate) //

                withContext(Dispatchers.Main) {
                    onDeleted()
                    dismiss()
                }
            }
        }

        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }
    }
}