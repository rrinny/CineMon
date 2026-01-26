package com.example.gcinemon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gcinemon.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

// 일정(Schedule) 테이블에 대한 DB 접근을 담당하는 DAO 인터페이스
@Dao
interface ScheduleDao {

    // 일정 저장 시 중복 날짜는 교체하여 데이터 일관성을 유지
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    // 오늘 이후의 일정만 조회하여 화면에 실시간 반영
    @Query("""
        SELECT * FROM schedules 
        WHERE date >= :today 
        ORDER BY date ASC
    """)
    fun getFutureSchedules(today: String): Flow<List<ScheduleEntity>>

    // 특정 연-월에 해당하는 일정 목록을 조회
    @Query("""
        SELECT * FROM schedules
        WHERE date LIKE :yearMonth || '%'
        ORDER BY date ASC
    """)
    suspend fun getLatestSchedulesOfMonth(yearMonth: String): List<ScheduleEntity>

    // 특정 날짜의 일정 1건을 조회하며 없을 경우 null을 반환
    @Query("SELECT * FROM schedules WHERE date = :date LIMIT 1")
    suspend fun getLatestScheduleByDate(date: String): ScheduleEntity?

    // 근무 기록이 있는 모든 연-월 목록을 조회
    @Query("SELECT DISTINCT strftime('%Y-%m', date) FROM schedules ORDER BY date DESC")
    suspend fun getAllWorkedMonths(): List<String>

    // 특정 날짜의 일정을 삭제
    @Query("DELETE FROM schedules WHERE date = :date")
    suspend fun deleteScheduleByDate(date: String)

    // 특정 날짜의 일정을 조회
    @Query("SELECT * FROM schedules WHERE date = :date LIMIT 1")
    suspend fun getScheduleByDate(date: String): ScheduleEntity?
}