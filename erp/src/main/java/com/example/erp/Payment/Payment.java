package com.example.erp.Payment;

import java.time.LocalDateTime;

import com.example.erp.Payment_method.Payment_method;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Visit.Visit;

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
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment { //결제
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 생성
    private Long payment_id; //결제번호 필드

    @ManyToOne(fetch = FetchType.LAZY) ////LAZY: 지연 로딩 -> 필요할때만
    @JoinColumn(name = "visit_id", nullable = false) //visit_id: 외래키로 받아올 자바 필드명
    private Visit visit_id; //방문번호 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_code", nullable = false) //payment_method_code: 외래키로 받아올 자바 필드명
    private Payment_method payment_method_code; //결제수단 필드

    private int amount; //금액 필드

    private LocalDateTime paid_at; //결제시각 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code", nullable = false) //status_code: 외래키로 받아올 자바 필드명
    private Status_code status_code; //상태코드 필드
}
