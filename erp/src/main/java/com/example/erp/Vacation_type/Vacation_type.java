package com.example.erp.Vacation_type;

import java.util.ArrayList;
import java.util.List;

import com.example.erp.Vacation.Vacation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vacation_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vacation_type { //휴가분류
	@Id
    private String vacation_type_code;  //휴가분류코드 필드

    private String type_name; //분류명 필드

    private boolean is_active = true; //사용여부 필드
    
    @OneToMany(mappedBy = "vacation_type") //vacation_type_code:다른 엔터티에서 나를 참조할 자바 필드명
    private List<Vacation> vacation = new ArrayList<>();//휴가 리스트
}
