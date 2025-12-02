package com.example.erp.Visit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.erp.Chart.Chart;
import com.example.erp.Claim.Claim;
import com.example.erp.Department.Department;
import com.example.erp.Insurance_code.Insurance_code;
import com.example.erp.Patient.Patient;
import com.example.erp.Payment.Payment;
import com.example.erp.Reservation.Reservation;
import com.example.erp.Status_code.Status_code;
import com.example.erp.User_account.User_account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "visit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Visit { //방문

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동생성
    private Long visit_id; //방문번호 필드

    @ManyToOne(fetch = FetchType.LAZY) //LAZY: 지연 로딩 -> 필요할때만
    @JoinColumn(name = "patient_id", nullable = false) //patient_id :외래키로 받아올 자바 필드명
    private Patient patient_id; //환자번호 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = true) //reservation_id: 외래키로 받아올 자바 필드명
    private Reservation reservation_id; //예약번호 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) //user_id: 외래키로 받아올 자바 필드명
    private User_account user_id; //사용자번호 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_code", nullable = false) //department_code: 외래키로 받아올 자바 필드명
    private Department department_code; //진료과코드 필드

    @Column(name = "visit_datetime")
    private LocalDateTime visit_datetime; //방문일자 필드

    @Column(name = "visit_route")
    private String visit_route; //내원경로 필드

    @Column(name = "visit_type")
    private String visit_type; //진료유형 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_code", nullable = true) //insurance_code: 외래키로 받아올 자바 필드명
    private Insurance_code insurance_code; //보험코드 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code", nullable = false) //status_code: 외래키로 받아올 자바 필드명
    private Status_code status_code; //상태코드 필드

    @Column(name = "note")
    private String note; //메모 필드

    @Column(name = "created_at")
    private LocalDateTime created_at; //생성일시 필드
    
    @OneToMany(mappedBy = "visit_id")
    private List<Claim> claim = new ArrayList<>(); //청구 리스트
    
    @OneToMany(mappedBy = "visit_id")
    private List<Chart> chart = new ArrayList<>(); //차트 리스트
    
    @OneToMany(mappedBy = "visit_id")
    private List<Payment> payment = new ArrayList<>(); //결제 리스트
}
