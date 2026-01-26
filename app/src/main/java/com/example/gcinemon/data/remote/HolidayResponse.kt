package com.example.gcinemon.data.remote

import com.google.gson.annotations.SerializedName

data class HolidayResponse(
    // 최상위 response 객체를 매핑
    @SerializedName("response") val response: ResponseWrapper
)

data class ResponseWrapper(
    // 실제 응답 본문(body)을 감싸는 객체
    @SerializedName("body") val body: BodyWrapper
)

data class BodyWrapper(
    // 공휴일 목록과 전체 개수 정보를 포함
    @SerializedName("items") val items: ItemWrapper,
    @SerializedName("totalCount") val totalCount: Int
)

data class ItemWrapper(
    // 공휴일 데이터가 없을 수 있으므로 null 허용
    @SerializedName("item") val item: List<HolidayItem>?
)

data class HolidayItem(
    // 공휴일 이름과 날짜 정보를 저장
    @SerializedName("dateName") val dateName: String,
    @SerializedName("locdate") val locdate: Int
)