package com.example.erp.User_role;

import com.example.erp.Role_code.Role_code;
import com.example.erp.User_account.User_account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User_role { //사용자역할
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//자동생성
    private Long user_role_id; // 사용자역할번호

    @OneToOne
    @JoinColumn(name = "user_id") //user_id: 외래키로 받아올 자바 필드명
    private User_account user_id; //사용자번호

    @ManyToOne
    @JoinColumn(name = "role_code") //role_code: 외래키로 받아올 자바 필드명
    private Role_code role_code;
}
