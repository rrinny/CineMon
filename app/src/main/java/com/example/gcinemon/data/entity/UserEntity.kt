package com.example.gcinemon.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

// 사용자 정보를 저장하기 위한 Room 엔티티 클래스
@Entity(tableName = "users")
data class UserEntity(
    // 단일 사용자 구조를 가정하여 고정된 기본 키를 사용
    @PrimaryKey val id: Int = 0,

    // 사용자 닉네임 정보를 저장
    val nickname: String,

    // 간편 인증을 위한 PIN 값을 저장
    val pin: String
)