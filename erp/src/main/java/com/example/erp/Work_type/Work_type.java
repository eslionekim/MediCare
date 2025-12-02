package com.example.erp.Work_type;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.example.erp.Role_code.Role_code;
import com.example.erp.Work_schedule.Work_schedule;

@Entity
@Table(name = "work_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Work_type { //근무 종류
	@Id
    private String work_type_code; //근무종류코드

    @ManyToOne
    @JoinColumn(name = "role_code", nullable = false) //role_code:외래키로 받아올 자바 필드명 
    private Role_code role_code;  //역할코드 필드

    private String work_name; //근무종류명 필드

    private LocalTime start_time; //근무시작시각 필드

    private LocalTime end_time; //근무종료시각 필드

    private String note; //비고 필드
    
    @OneToMany(mappedBy = "work_type_code") //work_type_code : 다른 엔터티에서 나를 참조할 자바 필드명
    private List<Work_schedule> work_schedule = new ArrayList<>(); //근무스케줄 리스트
}
