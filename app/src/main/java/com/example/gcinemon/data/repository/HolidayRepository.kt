package com.example.gcinemon.data.repository

import com.example.gcinemon.data.remote.RetrofitClient
import com.example.gcinemon.util.HolidayManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HolidayRepository(private val holidayManager: HolidayManager) {
    private val SERVICE_KEY = "65e4efc883ba541e5e2eb2db7dfa2b6517d3f62515d3539676a0e95881e86d91"

    suspend fun refreshHolidays(year: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.getHolidays(
                serviceKey = SERVICE_KEY,
                year = year
            )

            val items = response.response.body.items.item
            if (items != null) {
                val holidayMap = items.associate { it.locdate.toString() to it.dateName }
                holidayManager.saveHolidays(holidayMap)
                Result.success(Unit)
            } else {
                Result.failure(Exception("데이터가 존재하지 않습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}