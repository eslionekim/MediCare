package com.example.erp.User_account;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.erp.Staff_profile.Staff_profile;
import com.example.erp.Vacation.Vacation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class User_account { //사용자
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) //자동생성
    private Long user_id;  // 사용자번호 필드

    private String name; //이름 필드

    @Column(unique = true, nullable = false) //중복x
    private String email; //이메일 필드

    @Column(nullable = false)
    private String password; //비밀번호 필드

    private boolean is_active = true; //활성여부 필드

    private LocalDateTime created_at = LocalDateTime.now(); //생성일시 필드
    
    @OneToMany(mappedBy = "user_account", cascade = CascadeType.ALL, orphanRemoval = true)
    // user_id : 다른 엔터티에서 나를 참조할 자바 필드명 , cascade = CascadeType.ALL :여기에 무슨일 일어나면 자식엔터티도 영향(삭제 제외) , orphanRemoval = true: 삭제시 자식 엔터티 자동삭제
    private List<Staff_profile> staff_profile = new ArrayList<>(); //의료진 프로필

    @OneToMany(mappedBy = "user_account")
    private List<Vacation> vacation = new ArrayList<>(); //

}
