package com.example.gcinemon

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.gcinemon.data.AppDatabase

class LoginActivity : AppCompatActivity() {

    // 사용자가 입력한 PIN 번호를 저장
    private val pin = StringBuilder()

    private lateinit var dotsLayout: LinearLayout
    private lateinit var btnDelete: ImageView
    private lateinit var tvSignup: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dotsLayout = findViewById(R.id.layoutPinDots)
        btnDelete = findViewById(R.id.btnDelete)
        tvSignup = findViewById(R.id.tvSignup)

        val keys = listOf(
            findViewById<View>(R.id.key1) to '1',
            findViewById<View>(R.id.key2) to '2',
            findViewById<View>(R.id.key3) to '3',
            findViewById<View>(R.id.key4) to '4',
            findViewById<View>(R.id.key5) to '5',
            findViewById<View>(R.id.key6) to '6',
            findViewById<View>(R.id.key7) to '7',
            findViewById<View>(R.id.key8) to '8',
            findViewById<View>(R.id.key9) to '9',
            findViewById<View>(R.id.key0) to '0',
        )

        keys.forEach { (view, digit) ->
            view.setOnClickListener { appendDigit(digit) }
        }

        btnDelete.setOnClickListener { deleteDigit() }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java))
        }

        updateDots()
    }

    // 숫자를 누를 때 실행되는 함수
    private fun appendDigit(d: Char) {
        if (pin.length >= 6) return
        pin.append(d)
        updateDots()

        if (pin.length == 6) {
            checkPinAndLogin()
        }
    }

    // 실제 비밀번호를 확인하는 함수
    private fun checkPinAndLogin() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@LoginActivity)
            val user = db.userDao().getUser() // 저장된 PIN 가져오기

            // 가입된 정보가 있고, 입력한 PIN이 일치할 때만 통과
            if (user != null && pin.toString() == user.pin) {
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
            } else {
                // 틀렸을 때 실행되는 애니메이션
                val shake = AnimationUtils.loadAnimation(this@LoginActivity, R.anim.shake)
                dotsLayout.startAnimation(shake)

                // 햅틱 진동
                dotsLayout.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)

                // 입력 값 초기화
                pin.setLength(0)
                updateDots()
            }
        }
    }

    private fun deleteDigit() {
        if (pin.isNotEmpty()) {
            pin.deleteCharAt(pin.length - 1)
            updateDots()
        }
    }

    private fun updateDots() {

        for (i in 0 until dotsLayout.childCount) {
            val dot = dotsLayout.getChildAt(i)
            if (i < pin.length) {
                dot.setBackgroundResource(R.drawable.pin_dot_active)
            } else {
                dot.setBackgroundResource(R.drawable.pin_dot_inactive)
            }
        }
    }
}
