package com.example.gcinemon.data.mapper

import com.example.gcinemon.TagStyle
import com.example.gcinemon.WorkTag
import com.example.gcinemon.data.entity.ScheduleEntity

// 일정 엔티티를 UI에서 사용하는 태그 구조로 변환하는 매퍼 객체
object ScheduleMapper {
    // 일정 목록을 날짜별 WorkTag 맵 형태로 변환
    fun toWorkTagMap(list: List<ScheduleEntity>): Map<String, List<WorkTag>> {
        return list
            // 날짜를 yyyyMMdd 형식의 키로 그룹화
            .groupBy { it.date.replace("-", "") } // yyyyMMdd
            .mapValues { (_, schedules) ->
                schedules.flatMap { s ->
                    listOf(
                        // 근무 포지션에 따라 태그와 스타일을 매핑
                        when (s.position) {
                            "매점" -> WorkTag("매점", TagStyle.ORANGE)
                            "검표" -> WorkTag("검표", TagStyle.BLUE)
                            else -> null
                        },

                        // 근무 타입에 따라 태그와 스타일을 매핑
                        when (s.workType) {
                            "오픈" -> WorkTag("오픈", TagStyle.ORANGE)
                            "미들" -> WorkTag("미들", TagStyle.GREEN)
                            "마감" -> WorkTag("마감", TagStyle.BLUE)
                            else -> null
                        }
                    )
                // null 태그 제거 및 최대 2개까지만 사용
                }.filterNotNull().take(2)
            }
    }
}