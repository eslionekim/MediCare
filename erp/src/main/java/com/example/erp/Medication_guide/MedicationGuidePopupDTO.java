package com.example.erp.Medication_guide;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicationGuidePopupDTO {
	// 기본 정보
    private Long prescriptionId;

    // 환자 정보
    private String patientName; //환자명
    private LocalDate birth; //환자 생년월일
    private String gender; //환자 성별

    // 조제 정보
    private LocalDateTime dispensedAt; //조제일
    private String pharmacistName; //조제약사

    // 복약지도 상세 품목 리스트
    private List<MedicationItemDTO> items; //아이템 리스트

    // 금액 정보
    private BigDecimal totalAmount;      // 총 금액
    private BigDecimal taxAmount;        // 과세
    private BigDecimal nonTaxAmount;     // 비과세
    
    // 보험 할인
    private BigDecimal insurerAmount;    // 보험자 부담
    private BigDecimal patientAmount;    // 환자 부담
    private String insuranceName; //보험명
    private double discountRate; //보험률
}
