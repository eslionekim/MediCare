package com.example.erp.Vacation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VacationDTO {
    private Long vacationId;
    private String typeName;
    private String startDate;
    private String endDate;
    private String statusName;
    private String statusCode;
}
