package com.example.gcinemon.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gcinemon.data.dao.ScheduleDao
import com.example.gcinemon.data.dao.UserDao
import com.example.gcinemon.data.entity.ScheduleEntity
import com.example.gcinemon.data.entity.UserEntity

@Database(entities = [ScheduleEntity::class, UserEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao
    abstract fun userDao(): UserDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gcinemon_db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}