package com.example.erp.User_account;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateDTO {
	private String userId;
    private String name;
    private String password;
    private LocalDate hireDate;
    private String departmentName;
}
