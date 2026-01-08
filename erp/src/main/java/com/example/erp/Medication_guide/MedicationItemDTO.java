package com.example.erp.Medication_guide;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicationItemDTO { //리스트 들어갈 내용
	// 약품 기본 정보
    private String name;  //약품명

    // 투약 정보
    private BigDecimal dose;      // 1회 투약량
    private Integer frequency;    // 1일 투약 횟수
    private Integer days;         // 투약 일수

    // 복약 지도 내용
    private String guidance;      // 복약 안내
    private String description;   // 주의사항 설명
}
