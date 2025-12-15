// src/main/java/com/example/erp/Reservation/CalendarDateDto.java
package com.example.erp.Reservation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarDateDto {

    private int day; // 달력에 보이는 숫자
    private String fullDate; // yyyy-MM-dd
    private boolean selected; // 선택된 날짜 여부
    private boolean otherMonth;// 이전/다음 달 표시 여부
}
