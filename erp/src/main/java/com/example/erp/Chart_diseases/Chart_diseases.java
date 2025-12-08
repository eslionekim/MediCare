package com.example.erp.Chart_diseases;

import com.example.erp.Chart.Chart;
import com.example.erp.Diseases_code.Diseases_code;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chart_diseases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chart_diseases { //차트진단
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 생성
    private Long chart_diseases_id; // 차트진단코드번호 필드

    @ManyToOne(fetch = FetchType.LAZY) // LAZY: 지연 로딩 -> 필요할때만
    @JoinColumn(name = "chart_id", nullable = false) // chart_id: 외래키로 받아올 자바 필드명
    private Chart chart; // 차트 번호 필드

    @ManyToOne(fetch = FetchType.LAZY) // LAZY: 지연 로딩 -> 필요할때만
    @JoinColumn(name = "diseases_code", nullable = false) // diseases_code : 외래키로 받아올 자바 필드명
    private Diseases_code diseases_code; // 진단코드 필드

    private boolean is_primary = false; // 주진단여부 필드
}
