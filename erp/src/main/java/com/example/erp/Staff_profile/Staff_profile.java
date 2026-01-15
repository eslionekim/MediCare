package com.example.erp.Staff_profile;

import java.time.LocalDate;

import com.example.erp.Department.Department;
import com.example.erp.User_account.User_account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Staff_profile { //의료진 프로필
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 생성
    private Long staff_profile_id; //의료진 프로필 번호 필드

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) //user_id: 외래키로 받아올 자바 필드명
    private User_account user_account; //사용자 번호 필드

    @ManyToOne
    @JoinColumn(name = "department_code", nullable = false) //department_code: 외래키로 받아올 자바 필드명
    private Department department; // 진료과 코드 필드

    private String license_number; //면허번호 필드
    private String position; // 직함 필드
 
    private LocalDate hire_date;
    private String employment_type;

    private String bank_name;
    private String bank_account;
}
