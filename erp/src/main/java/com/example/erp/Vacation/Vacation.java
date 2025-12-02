package com.example.erp.Vacation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.erp.Status_code.Status_code;
import com.example.erp.User_account.User_account;
import com.example.erp.Vacation_type.Vacation_type;

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
public class Vacation { //휴가

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 생성
    private Long vacation_id; //휴가번호 필드

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) //user_id : 외래키로 받아올 자바 필드명
    private User_account user_account; //사용자번호 필드

    @ManyToOne
    @JoinColumn(name = "vacation_type_code", nullable = false) //vacation_type_code:외래키로 받아올 자바 필드명
    private Vacation_type vacation_type; //휴가분류코드 필드

    @Column(nullable = false)
    private LocalDate start_date; //시작일자 필드

    @Column(nullable = false)
    private LocalDate end_date; //종료일자 필드

    @ManyToOne
    @JoinColumn(name = "status_code", nullable = false) //status_code: 외래키로 받아올 자바 필드명
    private Status_code status_code; //상태코드 필드

    private String reason; //사유 필드

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); //생성일시 필드
}
