package com.example.gcinemon.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 0,
    val nickname: String,
    val pin: String
)