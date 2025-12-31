package com.example.erp.Issue_request;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "issue_request") // 불출요청
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Issue_request {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long issue_request_id; //불출요청번호

    @Column(nullable = false, length = 50)
    private String department_code; //요청부서

    @Column(nullable = false, length = 100)
    private String user_id; //요청자ID

    @Column(nullable = false, updatable = false)
    private LocalDateTime requested_at; 

    @Column(nullable = false, length = 50)
    private String status_code;

    @Column(length = 255)
    private String note;
}

