package com.example.erp.Claim_item;

import com.example.erp.Claim.Claim;
import com.example.erp.Fee_item.Fee_item;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "claim_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Claim_item {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 생성
    private Long claim_item_id; //청구항목번호 필드

    @ManyToOne(fetch = FetchType.LAZY) // LAZY: 지연 로딩 -> 필요할때만
    @JoinColumn(name = "claim_id", nullable = false) //claim_id: 외래키로 받아올 자바 필드명
    private Claim claim_id; // 청구번호 필드

    @ManyToOne(fetch = FetchType.LAZY) // LAZY: 지연 로딩 -> 필요할때만
    @JoinColumn(name = "fee_item_code", nullable = false) //fee_item_code: 외래키로 받아올 자바 필드명
    private Fee_item fee_item_code; //수가항목 코드 필드

    private int unit_price; //단가 필드
    private int quantity; //수량 필드
    private int discount; //할인 필드
    private int total; //합계 필드
}
