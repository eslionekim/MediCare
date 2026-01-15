package com.example.erp.Staff_profile;

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
public class MyPageDTO {
	private String password;       // User_account.password
    private String license;        // Staff_profile.license_number
    private String bank;           // Staff_profile.bank_name
    private String account;        // Staff_profile.bank_account
}
