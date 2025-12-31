package com.example.erp.Dispense_item;

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
@Table(name = "dispense_item") //조제항목
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Dispense_item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dispense_item_id; //조제항목번호

    @Column(nullable = false)
    private Long dispense_id; //조제번호

    private Long prescription_item_id; //처방항목번호

    @Column(nullable = false, length = 50)
    private String item_code; //물품코드

    @Column(nullable = false)
    private BigDecimal quantity; //조제수량

    @Column(length = 255)
    private String substitute_reason; //대체사유
}

