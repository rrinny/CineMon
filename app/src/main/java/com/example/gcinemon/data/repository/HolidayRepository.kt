package com.example.gcinemon.data.repository

import com.example.gcinemon.data.remote.RetrofitClient
import com.example.gcinemon.util.HolidayManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 공휴일 데이터를 네트워크에서 조회하고 로컬에 저장하는 레포지토리 클래스
class HolidayRepository(private val holidayManager: HolidayManager) {
    // 공휴일 API 호출에 필요한 서비스 키
    private val SERVICE_KEY = "65e4efc883ba541e5e2eb2db7dfa2b6517d3f62515d3539676a0e95881e86d91"

    // 연도 기준 공휴일 데이터를 갱신하고 결과를 Result 타입으로 반환
    suspend fun refreshHolidays(year: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 공휴일 API 호출을 수행
            val response = RetrofitClient.instance.getHolidays(
                serviceKey = SERVICE_KEY,
                year = year
            )

            // 응답 데이터가 존재할 경우 날짜-이름 형태로 변환하여 저장
            val items = response.response.body.items.item
            if (items != null) {
                val holidayMap = items.associate { it.locdate.toString() to it.dateName }
                holidayManager.saveHolidays(holidayMap)
                Result.success(Unit)
            } else {
                // 데이터가 없는 경우 실패 결과를 반환
                Result.failure(Exception("데이터가 존재하지 않습니다."))
            }
        } catch (e: Exception) {
            // 예외 발생 시 앱 비정상 종료를 방지하고 실패 결과로 처리
            Result.failure(e)
        }
    }
}