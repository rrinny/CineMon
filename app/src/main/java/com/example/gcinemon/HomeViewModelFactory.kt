package com.example.gcinemon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gcinemon.data.dao.ScheduleDao

// HomeViewModel에 ScheduleDao를 주입하기 위한 ViewModelFactory
class HomeViewModelFactory(
    private val scheduleDao: ScheduleDao
) : ViewModelProvider.Factory {

    // 요청된 ViewModel 타입에 맞는 인스턴스 생성
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // HomeViewModel 요청 시 ScheduleDao를 전달해 생성
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(scheduleDao) as T
        }
        // 지원하지 않는 ViewModel 타입일 경우 예외 발생
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}