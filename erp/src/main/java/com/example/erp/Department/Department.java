package com.example.erp.Department;

import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import com.example.erp.Reservation.Reservation;
import com.example.erp.Staff_profile.Staff_profile;
import com.example.erp.Visit.Visit;
import com.example.erp.Work_schedule.Work_schedule;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "department")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department { //진료과
	@Id
    private String department_code;  // 진료과코드 필드

    private String name; //명칭 필드

    private boolean is_active = true; //사용여부 필드
    
    @OneToMany(mappedBy = "department_code")  //department_code : 다른 엔터티에서 나를 참조할 자바 필드명
    private List<Staff_profile> staff_profile = new ArrayList<>(); // 의료진 프로필 리스트
    
    @OneToMany(mappedBy = "department_code")
    private List<Reservation> reservation = new ArrayList<>(); //예약 리스트

    @OneToMany(mappedBy = "department_code")
    private List<Visit> visit = new ArrayList<>(); // 방문 리스트

    @OneToMany(mappedBy = "department_code")
    private List<Work_schedule> work_schedule = new ArrayList<>(); //근무스케줄 리스트
}
