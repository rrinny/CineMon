package com.example.gcinemon.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 공휴일 API 호출을 위한 Retrofit 클라이언트 설정 객체
object RetrofitClient {
    // 공공데이터포털 공휴일 API 기본 URL
    private const val BASE_URL = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/"

    // Retrofit 인스턴스를 lazy 초기화하여 불필요한 생성 비용 최소화
    val instance: HolidayApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HolidayApi::class.java)
    }
}