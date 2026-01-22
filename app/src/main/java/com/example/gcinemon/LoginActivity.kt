package com.example.gcinemon

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

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

    private fun appendDigit(d: Char) {
        if (pin.length >= 6) return
        pin.append(d)
        updateDots()

        if (pin.length == 6) {
            // 6자리 입력 완료 -> 홈 이동
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
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
