package com.example.gcinemon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gcinemon.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    @Query("""
        SELECT * FROM schedules 
        WHERE date >= :today 
        ORDER BY date ASC
    """)
    fun getFutureSchedules(today: String): Flow<List<ScheduleEntity>>

    @Query("""
        SELECT * FROM schedules
        WHERE date LIKE :yearMonth || '%'
        ORDER BY date ASC
    """)
    suspend fun getLatestSchedulesOfMonth(yearMonth: String): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE date = :date LIMIT 1")
    suspend fun getLatestScheduleByDate(date: String): ScheduleEntity?

    @Query("SELECT DISTINCT strftime('%Y-%m', date) FROM schedules ORDER BY date DESC")
    suspend fun getAllWorkedMonths(): List<String>

    @Query("DELETE FROM schedules WHERE date = :date")
    suspend fun deleteScheduleByDate(date: String)

    @Query("SELECT * FROM schedules WHERE date = :date LIMIT 1")
    suspend fun getScheduleByDate(date: String): ScheduleEntity?
}