package com.example.erp.Dispense;

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
@Table(name = "dispense") //조제
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Dispense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dispense_id; //조제번호

    @Column(nullable = false)
    private Long prescription_id; //처방번호

    @Column(nullable = false, length = 100)
    private String user_id; 

    private LocalDateTime dispensed_at;

    @Column(nullable = false, length = 50)
    private String status_code;
}

