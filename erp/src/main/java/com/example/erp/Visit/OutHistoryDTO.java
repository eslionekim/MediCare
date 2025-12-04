package com.example.erp.Visit;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutHistoryDTO {
    private LocalDate visitDate;
    private String doctorName;
    private String firstOrReturn;
    private String symptom;
}
