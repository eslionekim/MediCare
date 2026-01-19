package com.example.erp.Prescription;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prescription") //처방전
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Prescription { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prescription_id; //처방번호

    @Column(nullable = false)
    private Long visit_id; //방문번호


    @Column(nullable = false, length = 50)
    private String status_code;

    @Column(nullable = false, updatable = false)
    private LocalDateTime prescribed_at;
}
