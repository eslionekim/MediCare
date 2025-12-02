package com.example.erp.Chart;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.erp.Chart_diseases.Chart_diseases;
import com.example.erp.User_account.User_account;
import com.example.erp.Visit.Visit;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chart { //차트
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성
    private Long chart_id; //차트 번호 필드

    @ManyToOne(fetch = FetchType.LAZY) // LAZY: 지연로딩 -> 필요할때만 가져옴
    @JoinColumn(name = "visit_id", nullable = false) //visit_id: 외래키로 받아올 자바 필드값
    private Visit visit; //방문번호 필드

    private String subjective; //주증상 필드
    private String objective; //객관적검사자료 필드
    private String assessment; //평가및진단 필드
    private String plan; //계획및치료법 필드
    private String note; //진료메모 필드

    private boolean is_locked = false; //잠금여부 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account") //user_id : 외래키로 받아올 자바 필드값
    private User_account user_id; //사용자번호 필드

    private LocalDateTime updated_at; // MySQL 테이블과 매핑
    
    @OneToMany(mappedBy = "chart") //char_id : 다른 엔터티에서 나를 참조할 자바 필드명
    private List<Chart_diseases> chart_diseases = new ArrayList<>(); // 차트 진단코드 리스트
}
