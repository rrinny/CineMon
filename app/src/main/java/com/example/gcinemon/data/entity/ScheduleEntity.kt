package com.example.gcinemon.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey
    val date: String,
    val position: String,
    val workType: String,
    val memo: String?
)