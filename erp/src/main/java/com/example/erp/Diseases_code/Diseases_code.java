package com.example.erp.Diseases_code;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diseases_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diseases_code { //질병 코드
	@Id
    private String diseases_code;  // 질병코드 필드

    private String name_kor; //한글명 필드

    private String name_eng; //영문명 필드

    private String department; //진료과 필드

    private boolean is_active = true; //사용여부 필드
}
