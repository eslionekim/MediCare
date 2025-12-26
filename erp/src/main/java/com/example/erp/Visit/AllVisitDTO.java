package com.example.erp.Visit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllVisitDTO {
	private Long chart_id;
    private Long patient_id;
    private String department_name;
    private String patient_name;
    private String gender;
    private LocalDate birth_date;
    private String visit_type;
    private LocalDateTime created_at;
    private String note;
    private String insurance_name;
    private String doctor_name;
    private Long visit_id;  // 차트 버튼용
    
    
}
