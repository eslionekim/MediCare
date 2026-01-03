package com.example.erp.Item;

import java.math.BigDecimal;
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
@Table(name = "item") // 물품
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item { 

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성
    private Long item_code; //물품 코드

    @Column(nullable = false, length = 20)
    private String item_type; //분류(약품/소모품/자재)

    @Column(nullable = false, length = 200)
    private String name; //물품명

    @Column(nullable = false, length = 50)
    private String base_unit; //기본단위(정/개/병)

    @Column(length = 50)
    private String pack_unit_name; //포장단위명

    private Integer pack_unit_qty; //포장단위수량

    private BigDecimal safety_stock; //안전재고
 
    private Integer unit_price; //기본 단가

    @Column(nullable = false)
    private Boolean is_active = true; //사용여부

    @Column(length = 50)
    private Long fee_item_code; //수가항목 코드

    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at; //일시
}
