package com.example.erp.Fee_item;

import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

import com.example.erp.Claim_item.Claim_item;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fee_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fee_item { //수가항목
	@Id
    private String fee_item_code;  //수가항목코드 필드

    private String category; // 카테고리 필드 (이거 상태코드랑 엮는다했었나..?)

    private String name; // 명칭 필드

    private int base_price; // 기본 단가 필드

    private boolean is_active = true; //사용여부 필드
    
    @OneToMany(mappedBy = "fee_item_code") //fee_item_code : 다른 엔터티에서 나를 참조할 자바 필드명
    private List<Claim_item> claim_item = new ArrayList<>(); // 청구항목 리스트

}
