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
public class Visit { //??

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //????
    private Long visit_id; //???? ??

    @ManyToOne(fetch = FetchType.LAZY) //LAZY: ?? ?? -> ?????
    @JoinColumn(name = "patient_id", nullable = false) //patient_id :???? ??? ?? ???
    private Patient patient; //???? ??

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = true) //reservation_id: ???? ??? ?? ???
    private Reservation reservation; //???? ??

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false) // visit 테이블의 담당 의사 FK 컬럼명
    private User_account user_account; // 담당 의사/사용자 FK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_code", nullable = false) //department_code: ???? ??? ?? ???
    private Department department; //????? ??

    @Column(name = "visit_datetime")
    private LocalDateTime visit_datetime; //접수시간 필드
    
    @Column(name = "visit_route")
    private String visit_route; //???? ??

    @Column(name = "visit_type")
    private String visit_type; //???? ??

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_code", nullable = true) //insurance_code: ???? ??? ?? ???
    private Insurance_code insurance_code; //???? ??

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code", nullable = false) //status_code: ???? ??? ?? ???
    private Status_code status_code; //???? ??

    @Column(name = "note")
    private String note; //?? ??

    @Column(name = "created_at")
    private LocalDateTime created_at; //???? ??
    
    @OneToMany(mappedBy = "visit")
    private List<Claim> claim = new ArrayList<>(); //?? ???
    
    @OneToMany(mappedBy = "visit")
    private List<Chart> chart = new ArrayList<>(); //?? ???
    
    @OneToMany(mappedBy = "visit")
    private List<Payment> payment = new ArrayList<>(); //?? ???
}


