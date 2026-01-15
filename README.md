# 🍿 CineMon

**프로젝트명:** CineMon (Cinema + Monitoring)  
**슬로건:** "영화관 근무, 한눈에 모니터링"  
**컨셉:** 직관적인 근무 스케줄 관리 + 정확한 급여 계산

---

## 프로젝트 개요

CineMon은 영화관 근무자를 위한 **스케줄 관리 및 급여 계산 앱**입니다.

- 근무 일정 등록/수정/삭제 (CRUD)
- 근무 유형별 시각화 (오픈/미들/마감)
- 휴게시간, 야간수당, 주휴수당, 공휴일 특근 고려 급여 계산
- 공휴일 API 연동 및 로컬 캐싱

---

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| 언어 | Kotlin (Android) |
| DB | SQLite |
| 외부 연동 | 공휴일 공공데이터 API |

---

## 팀원 소개
  <table width="100%">
    <tr>
      <td align="center">
        <a href="https://github.com/sonyewoen">
          <img src="https://github.com/sonyewoen.png" width="100px" alt="손예원"/><br />
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/rrinny">
          <img src="https://github.com/rrinny.png" width="100px" alt="박채린"/><br />
        </a>
      </td>
    </tr>
    <tr>
      <td align="center">손예원</td>
      <td align="center">박채린</td>
    </tr>
    <tr>
      <td align="center">
        <p>UI/UX 설계 및 화면 구현</p>
        <p>데이터 시각화</p>
      </td>
      <td align="center">
        <p>DB 설계 및 로직 구현</p>
        <p>공휴일 API 연동·캐싱</p>
      </td>
    </tr>
  </table>
  
---

## 핵심 기능

1. **스케줄 관리 시스템**
   - 근무 일정 생성, 조회, 수정, 삭제
   - 근무 유형, 휴게시간, 특이사항 메모 저장

2. **스마트 프리셋**
   - 오픈/미들/마감 시간 및 포지션 자동 입력
   - 총 근무 시간 - 휴게시간 → 실 근무 시간 계산

3. **실시간 근무 관제 대시보드**
   - 현재 시간과 근무 일정 비교 → 출근 전 / 근무 중 / 퇴근 완료 상태 표시
   - UI 색상, 텍스트 실시간 변경

4. **차등 급여 계산 엔진**
   - 휴게시간 차감 후 급여 계산
   - 야간수당 적용 (22:00 이후 1.5배)
   - 주휴수당 자동 계산 (주간 실 근무 15시간 이상)
   - 공휴일 또는 수동 지정 특근 시 1.5배 가산

5. **공휴일 정보 캐싱**
   - 공휴일 API 호출 후 로컬 DB 저장
   - 네트워크 없이도 달력 표시 및 급여 계산 가능

---

## 데이터베이스

| Entity | 주요 필드 | 설명 |
| --- | --- | --- |
| **Schedule** | id, date, startTime, endTime, breakTime, type, status, isManualHoliday | 근무 일정, 휴게 시간, 수동 특근 포함 |
| **WageRule** | id, baseWage, applyYear, nightRate, holidayRate | 연도별 급여 기준, 야간/공휴일 배율 |
| **Holiday** | date, name, isHoliday | 공휴일 API 캐싱용, 급여 계산 및 달력 표시 |
| **Preset** | id, name, defStartTime, defEndTime, colorCode | 근무 유형 프리셋, 캘린더 색상 포함 |
