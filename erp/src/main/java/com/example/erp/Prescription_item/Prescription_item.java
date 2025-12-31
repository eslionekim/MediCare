package com.example.erp.Prescription_item;

import java.math.BigDecimal;

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
@Table(name = "prescription_item") // 처방항목
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Prescription_item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prescription_item_id; //처방항목번호

    @Column(nullable = false)
    private Long prescription_id; //처방번호

    @Column(nullable = false, length = 50)
    private String item_code; //물품코드

    private BigDecimal dose; //1회투여량
    private Integer frequency; //1일 횟수
    private Integer days; //일수
    private BigDecimal total_quantity; //총수량

    @Column(length = 255)
    private String usage_note; //용법 메모
}

