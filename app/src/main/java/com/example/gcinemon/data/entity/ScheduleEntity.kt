package com.example.gcinemon.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

// 일정 정보를 저장하기 위한 Room 엔티티 클래스
@Entity(tableName = "schedules")
data class ScheduleEntity(
    // 날짜를 기본 키로 사용하여 일정의 고유성을 보장
    @PrimaryKey
    val date: String,

    // 근무 포지션 정보를 저장
    val position: String,

    // 근무 타입 정보를 저장
    val workType: String,

    // 메모는 선택 사항으로 null을 허용하여 안정성 확보
    val memo: String?
)