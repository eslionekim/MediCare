package com.example.erp.Prescription;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {
	private Long prescriptionId;
    private String patientName;
    private LocalDateTime prescribedAt;
    private String prescriptionSummary; // "name 외 N개"
    private String dispenseStatus; // "조제 대기" or 상태명
    private String dispenser; // "-" or 담당자
    private String statusCode; //상태코드
}
