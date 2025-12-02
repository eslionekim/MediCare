package com.example.erp.Reservation;

import java.time.LocalDateTime;

import com.example.erp.Department.Department;
import com.example.erp.Patient.Patient;
import com.example.erp.Status_code.Status_code;
import com.example.erp.User_account.User_account;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation { //예약

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 생성
    private Long reservation_id; //예약번호 필드

    @ManyToOne(fetch = FetchType.LAZY) //LAZY: 지연 로딩 -> 필요할때만
    @JoinColumn(name = "patient_id", nullable = false) //patient_id: 외래키로 받아올 자바 필드명
    private Patient patient_id; // 환자번호 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User_account user_id; //사용자번호 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_code", nullable = false)
    private Department department_code; //진료과코드 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code", nullable = false)
    private Status_code status_code; //상태코드 필드

    private LocalDateTime startTime; //시작시각 필드
    private LocalDateTime endTime; //종료시각 필드
    private LocalDateTime createdAt; //생성일시 필드

    private String note; //메모 필드
}
