package com.example.gcinemon.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gcinemon.data.dao.ScheduleDao
import com.example.gcinemon.data.dao.UserDao
import com.example.gcinemon.data.entity.ScheduleEntity
import com.example.gcinemon.data.entity.UserEntity

// 앱 전역에서 사용하는 Room 데이터베이스 설정 클래스
@Database(entities = [ScheduleEntity::class, UserEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    // 일정 데이터 접근을 위한 DAO 제공
    abstract fun scheduleDao(): ScheduleDao

    // 사용자 데이터 접근을 위한 DAO 제공
    abstract fun userDao(): UserDao

    companion object {
        // 데이터베이스 인스턴스를 싱글톤으로 관리
        private var instance: AppDatabase? = null

        // 멀티 스레드 환경에서도 안전하게 DB 인스턴스를 생성
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gcinemon_db"
                )
                    .createFromAsset("gcinemon_db.db")
                    .fallbackToDestructiveMigration()
                    .build()

                instance = newInstance
                newInstance
            }
        }
    }
}