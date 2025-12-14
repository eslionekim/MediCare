package com.example.erp.Work_schedule;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.erp.Department.Department;
import com.example.erp.Status_code.Status_code;
import com.example.erp.User_account.User_account;
import com.example.erp.Work_type.Work_type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Work_schedule { //근무스케줄

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 생성
    private Long schedule_id; //근무스케줄번호 필드

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) //user_id: 외래키로 받아올 자바 필드명
    private User_account user_account; //사용자번호 필드

    @ManyToOne
    @JoinColumn(name = "department_code") //department_code: 외래키로 받아올 자바 필드명
    private Department department; //진료과코드 필드

    @Column(nullable = false)
    private LocalDate work_date; //근무일자 필드

    @Column
    private LocalTime start_time; //출근버튼 필드

    @Column
    private LocalTime end_time; //퇴근버튼 필드

    @ManyToOne
    @JoinColumn(name = "work_type_code") //work_type_code: 외래키로 받아올 자바 필드명
    private Work_type work_type;  //근무종류 필드

    @ManyToOne
    @JoinColumn(name = "status_code") //status_code: 외래키로 받아올 자바 필드명
    private Status_code status_code; //상태코드 필드

    private String note; //메모 필드
}