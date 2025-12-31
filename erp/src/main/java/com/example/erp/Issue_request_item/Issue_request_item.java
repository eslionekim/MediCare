package com.example.erp.Issue_request_item;

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
@Table(name = "issue_request_item") //불출요청항목
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Issue_request_item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long issue_request_item_id; //불출요청항목번호

    @Column(nullable = false)
    private Long issue_request_id; //불출요청번호

    @Column(nullable = false, length = 50)
    private String item_code; //물품코드

    @Column(nullable = false)
    private BigDecimal requested_qty; //요청수량

    private BigDecimal approved_qty; //승인수량
} 
