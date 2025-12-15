// src/main/java/com/example/erp/Reservation/ReservationRequestDto.java
package com.example.erp.Reservation;

import lombok.Data;

@Data
public class ReservationRequestDto {

    private Long reservationId; // 수정 시 사용, 신규면 null
    private Long patientId;
    private String departmentCode;
    private String userId; // 의사 user_id

    private String date; // yyyy-MM-dd
    private String startTime; // HH:mm
    private String endTime; // HH:mm (선택)

    private String statusCode; // 기본: RESERVED 같은 코드
    private String note;
}
