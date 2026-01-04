package com.example.erp.Stock_move;

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
@Table(name = "stock_move") //재고이동
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Stock_move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stock_move_id; //재고이동번호

    @Column(nullable = false, length = 20)
    private String move_type; //

    @Column(length = 50)
    private String from_warehouse_code; //출발 과

    @Column(length = 50)
    private String to_warehouse_code; //도착과

    private Long dispense_id; //조제번호
    private Long issue_request_id; //불출번호
    private Long claim_id; //청구번호

    @Column(nullable = false, updatable = false)
    private LocalDateTime moved_at;

    @Column(nullable = false, length = 50)
    private String status_code;

    @Column(length = 255)
    private String note;
    
    @Column(length = 255)
    private String quantity;
}

