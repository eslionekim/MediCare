package com.example.erp.Payment_method;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_method")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment_method { //결제 수단
	@Id
    private String payment_method_code; //결제수단코드 필드
    
    private String name; //명칭 필드
    
    private boolean isActive = true; //사용여부 필드
}
