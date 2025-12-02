package com.example.erp.Insurance_code;

import java.util.ArrayList;
import java.util.List;

import com.example.erp.Visit.Visit;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "insurance_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Insurance_code { //보험코드
	@Id
    private String insurance_code;  //보험코드 필드

    private String name; //명칭 필드

    private double discount_rate;  // 할인율 필드

    private boolean is_active = true; //사용여부 필드
    
    @OneToMany(mappedBy = "insurance_code") //insurance_code : 다른 엔터티에서 나를 참조할 자바 필드명
    private List<Visit> visit = new ArrayList<>(); //방문 리스트
}
