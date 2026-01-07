package com.example.erp.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.erp.Reservation.Reservation;
import com.example.erp.Visit.Visit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient { // 환자

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성
    private Long patient_id; // 환자번호 필드

    private String rrn; // 주민등록번호 필드
    private String name; // 성명 필드
    private LocalDate birth_date; // 생년월일 필드
    private String gender; // 성별 필드
    private String phone; // 휴대전화 필드
    private String email; // 이메일 필드
    private String address1; // 주소1 필드
    private String address2; // 주소2 필드
    private String note; // 메모 필드

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime created_at; // 생성일시 필드


    @OneToMany(mappedBy = "patient") // patient_id : 다른 엔터티에서 나를 참조할 자바 필드명
    private List<Visit> visit = new ArrayList<>(); // 방문 리스트

    @OneToMany(mappedBy = "patient")
    private List<Reservation> reservation = new ArrayList<>(); // 예약 리스트
}
