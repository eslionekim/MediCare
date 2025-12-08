package com.example.erp.Claim;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.erp.Chart_diseases.Chart_diseases;
import com.example.erp.Claim_item.Claim_item;
import com.example.erp.User_account.User_account;
import com.example.erp.Visit.Visit;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "claim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Claim { //청구
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 생성
    private Long claim_id; // 청구 번호 필드

    @OneToOne(fetch = FetchType.LAZY) //LAZY: 지연 로딩 -> 필요할때만
    @JoinColumn(name = "visit_id", nullable = false) // visit_id : 외래키로 받아올 자바 필드명
    private Visit visit; // 방문번호 필드

    private int total_amount; //총액 필드
    private int discount_amount; //할인액 필드
    private boolean is_confirmed; //확정 여부 필드

    private LocalDateTime created_at; //생성일시 필드
    
    @OneToMany(mappedBy = "claim") // claim_id : 다른 엔터티에서 나를 참조할 자바 필드명
    private List<Claim_item> claim_item = new ArrayList<>(); // 청구 항목 리스트

}
