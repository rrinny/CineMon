package com.example.gcinemon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gcinemon.data.entity.UserEntity

@Dao
interface UserDao {
    // 사용자 정보 저장
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // 저장된 사용자 정보 조회
    @Query("SELECT * FROM users WHERE id = 0")
    suspend fun getUser(): UserEntity?

    // 사용자 삭제
    @Query("DELETE FROM users")
    suspend fun deleteUser()
}