package com.example.gcinemon.data.mapper

import com.example.gcinemon.TagStyle
import com.example.gcinemon.WorkTag
import com.example.gcinemon.data.entity.ScheduleEntity

object ScheduleMapper {

    fun toWorkTagMap(list: List<ScheduleEntity>): Map<String, List<WorkTag>> {
        return list
            .groupBy { it.date.replace("-", "") } // yyyyMMdd
            .mapValues { (_, schedules) ->
                schedules.flatMap { s ->
                    listOf(
                        // 근무 포지션
                        when (s.position) {
                            "매점" -> WorkTag("매점", TagStyle.ORANGE)
                            "검표" -> WorkTag("검표", TagStyle.BLUE)
                            else -> null
                        },

                        // 근무 타입
                        when (s.workType) {
                            "오픈" -> WorkTag("오픈", TagStyle.ORANGE)
                            "미들" -> WorkTag("미들", TagStyle.GREEN)
                            "마감" -> WorkTag("마감", TagStyle.BLUE)
                            else -> null
                        }
                    )
                }.filterNotNull().take(2)
            }
    }
}