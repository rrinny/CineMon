package com.example.gcinemon.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// 공휴일 정보를 조회하기 위한 Retrofit API 인터페이스
interface HolidayApi {
    // 연도 기준 공휴일 데이터를 서버로부터 조회
    @GET("getRestDeInfo")
    suspend fun getHolidays(
        @Query("serviceKey", encoded = false) serviceKey: String,
        @Query("solYear") year: Int,
        @Query("_type") type: String = "json",
        @Query("numOfRows") rows: Int = 100
    ): HolidayResponse
}