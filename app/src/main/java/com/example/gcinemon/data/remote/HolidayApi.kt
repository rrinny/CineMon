package com.example.gcinemon.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface HolidayApi {
    @GET("getRestDeInfo")
    suspend fun getHolidays(
        @Query("serviceKey", encoded = false) serviceKey: String,
        @Query("solYear") year: Int,
        @Query("_type") type: String = "json",
        @Query("numOfRows") rows: Int = 100
    ): HolidayResponse
}