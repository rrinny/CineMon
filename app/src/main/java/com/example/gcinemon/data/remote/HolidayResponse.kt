package com.example.gcinemon.data.remote

import com.google.gson.annotations.SerializedName

data class HolidayResponse(
    @SerializedName("response") val response: ResponseWrapper
)

data class ResponseWrapper(
    @SerializedName("body") val body: BodyWrapper
)

data class BodyWrapper(
    @SerializedName("items") val items: ItemWrapper,
    @SerializedName("totalCount") val totalCount: Int
)

data class ItemWrapper(
    @SerializedName("item") val item: List<HolidayItem>?
)

data class HolidayItem(
    @SerializedName("dateName") val dateName: String,
    @SerializedName("locdate") val locdate: Int
)