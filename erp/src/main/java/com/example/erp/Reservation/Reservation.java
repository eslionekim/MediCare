// src/main/java/com/example/erp/Reservation/Reservation.java
package com.example.erp.Reservation;

import java.time.LocalDateTime;

import com.example.erp.Patient.Patient;
import com.example.erp.User_account.User_account;
import com.example.erp.Department.Department;
import com.example.erp.Status_code.Status_code;

import jakarta.persistence.*;
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
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservation_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User_account user; // 의사(또는 담당자)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_code", nullable = false)
    private Department department;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime start_time;

    @Column(name = "end_time")
    private LocalDateTime end_time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code", nullable = false)
    private Status_code status_code;

    @Column(name = "created_at", updatable = false, insertable = false) // DB DEFAULT CURRENT_TIMESTAMP 사용
    private LocalDateTime created_at;

    private String note;
}
