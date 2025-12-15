// src/main/java/com/example/erp/Reservation/DailySlotDto.java
package com.example.erp.Reservation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySlotDto {

    private String time; // "09:00"
    private Long reservationId; // 예약 PK (없으면 null)
    private String patientName; // 환자명
    private String departmentName; // 진료과 이름
    private String doctorName; // 의사명
    private String statusName; // 상태명 (예: 예약완료, 진료완료)
    private boolean reservable; // true면 "예약하기" 버튼, false면 "변경하기"
}
