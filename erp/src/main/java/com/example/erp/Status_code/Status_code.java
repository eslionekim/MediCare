package com.example.erp.Status_code;

import java.util.ArrayList;
import java.util.List;

import com.example.erp.Payment.Payment;
import com.example.erp.Reservation.Reservation;
import com.example.erp.Vacation.Vacation;
import com.example.erp.Visit.Visit;
import com.example.erp.Work_schedule.Work_schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "status_code")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Status_code { //상태코드 
	@Id
    private String status_code;  // 상태코드 필드

    private String category; // 구분 필드

    private String name; // 명칭 필드

    private boolean is_active = true; //사용 여부 필드
    
    @OneToMany(mappedBy = "status_code") //status_code: 다른 엔터티에서 나를 참조할 자바 필드명
    private List<Work_schedule> work_schedule = new ArrayList<>(); //근무스케줄 리스트
    
    @OneToMany(mappedBy = "status_code")
    private List<Vacation> vacation = new ArrayList<>(); //휴가 리스트
    
    @OneToMany(mappedBy = "status_code")
    private List<Reservation> reservation = new ArrayList<>(); //예약 리스트
    
    @OneToMany(mappedBy = "status_code")
    private List<Visit> visit = new ArrayList<>(); //방문 리스트
    
    @OneToMany(mappedBy = "status_code")
    private List<Payment> payment = new ArrayList<>(); //결제 리스트
}
