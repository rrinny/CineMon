package com.example.gcinemon

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView

class ReportActivity : AppCompatActivity() {

    private var isInvoiceOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // 하단 네비 -> Report 선택 상태 + 탭 이동 연결
        BottomNavHelper.bind(this, BottomNavHelper.Tab.REPORT)

        val svReport = findViewById<NestedScrollView>(R.id.svReport)
        val rowInvoiceToggle = findViewById<ConstraintLayout>(R.id.rowInvoiceToggle)
        val cardInvoice = findViewById<LinearLayout>(R.id.cardInvoice)
        val ivInvoiceArrow = findViewById<ImageView>(R.id.ivInvoiceArrow)

        // 초기 상태
        cardInvoice.visibility = View.GONE
        ivInvoiceArrow.setImageResource(R.drawable.icon_down)
        isInvoiceOpen = false

        rowInvoiceToggle.setOnClickListener {
            isInvoiceOpen = !isInvoiceOpen

            if (isInvoiceOpen) {
                cardInvoice.visibility = View.VISIBLE
                ivInvoiceArrow.setImageResource(R.drawable.icon_up)

                // 펼쳤을 때 카드가 화면에 들어오게 스크롤
                cardInvoice.post {
                    svReport.smoothScrollTo(0, cardInvoice.bottom)
                }
            } else {
                cardInvoice.visibility = View.GONE
                ivInvoiceArrow.setImageResource(R.drawable.icon_down)
            }
        }
    }
}
