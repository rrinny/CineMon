package com.example.gcinemon

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 하단 네비 -> HOME 선택 상태 + 탭 이동 로직 일괄 처리
        BottomNavHelper.bind(this, BottomNavHelper.Tab.HOME)

        // "다가오는 근무 날" 편집 버튼 -> 바텀시트
        findViewById<ImageView>(R.id.btnEditUpcoming).setOnClickListener {
            val tag = "WorkdayEditBottomSheet"
            if (supportFragmentManager.findFragmentByTag(tag) == null) {
                WorkdayEditBottomSheet().show(supportFragmentManager, tag)
            }
        }
    }
}
