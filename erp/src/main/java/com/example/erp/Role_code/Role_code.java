package com.example.erp.Role_code;

import java.util.ArrayList;
import java.util.List;

import com.example.erp.User_role.User_role;
import com.example.erp.Work_type.Work_type;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "role_code")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role_code { //역할코드
	@Id
    private String role_code; //역할코드 필드

    private String name; //명칭 필드

    private boolean is_active = true; //사용여부 필드
    
    @OneToMany(mappedBy = "role_code", cascade = CascadeType.ALL, orphanRemoval = true) 
    // role_code : 다른 엔터티에서 나를 참조할 자바 필드명 , cascade = CascadeType.ALL :여기에 무슨일 일어나면 자식엔터티도 영향(삭제 제외) , orphanRemoval = true: 삭제시 자식 엔터티 자동삭제
    private List<Work_type> Work_type = new ArrayList<>(); // 근무종류 리스트

    @OneToMany(mappedBy = "role_code")
    private List<User_role> user_role = new ArrayList<>(); // 사용자역할 리스트
    
    @OneToMany(mappedBy = "role_code")
    private List<Work_type> work_type = new ArrayList<>(); // 근무종류 리스트
}
