package com.example.erp.Stock;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "stock") //재고
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stock_id; //재고번호

    @Column(nullable = false, length = 50)
    private String warehouse_code; //창고코드

    @Column(nullable = false, length = 50)
    private String item_code; //물품코드

    @Column(length = 100)
    private String lot_code; //로트코드

    @Column(nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO; //현재수량

    private LocalDate expiry_date; //유통기한
    private LocalDate outbound_deadline; //출고마감일
}

