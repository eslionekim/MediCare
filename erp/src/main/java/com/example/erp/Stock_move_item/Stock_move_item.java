package com.example.erp.Stock_move_item;

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
@Table(name = "stock_move_item") //재고이동항목
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Stock_move_item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stock_move_item_id; //재고이동항목번호

    @Column(nullable = false)
    private Long stock_move_id; //재고이동번호

    @Column(nullable = false, length = 50)
    private String item_code; //물품코드

    @Column(length = 100)
    private String lot_code; //로트코드

    @Column(nullable = false)
    private BigDecimal quantity; //수량

    private Integer unit_price; //단가
    private LocalDate expiry_date;
}

