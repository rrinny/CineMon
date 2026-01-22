package com.example.gcinemon

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class JoinActivity : AppCompatActivity() {

    private lateinit var etNickname: EditText
    private lateinit var etPin: EditText
    private lateinit var etPinCheck: EditText
    private lateinit var btnJoin: TextView
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        etNickname = findViewById(R.id.etNickname)
        etPin = findViewById(R.id.etPin)
        etPinCheck = findViewById(R.id.etPinCheck)
        btnJoin = findViewById(R.id.btnJoin)
        btnBack = findViewById(R.id.btnBack)

        // 초기 상태(회색)
        updateJoinButtonState()

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateJoinButtonState()
            }
        }

        etNickname.addTextChangedListener(watcher)
        etPin.addTextChangedListener(watcher)
        etPinCheck.addTextChangedListener(watcher)

        btnJoin.setOnClickListener {
            // enabled=true일 때만 동작
            if (!btnJoin.isEnabled) return@setOnClickListener

            // 회원가입 성공 처리 후 로그인 화면으로 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Join 화면은 뒤로가기로 돌아오지 않게
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateJoinButtonState() {
        val nicknameOk = etNickname.text.toString().trim().isNotEmpty()
        val pin = etPin.text.toString().trim()
        val pinCheck = etPinCheck.text.toString().trim()

        val pinOk = pin.length == 6
        val pinCheckOk = pinCheck.length == 6
        val matchOk = pinOk && pinCheckOk && pin == pinCheck

        btnJoin.isEnabled = nicknameOk && matchOk
    }
}
