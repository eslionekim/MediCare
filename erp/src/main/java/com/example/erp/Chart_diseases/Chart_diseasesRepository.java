package com.example.erp.Chart_diseases;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.erp.Chart.Chart;

public interface Chart_diseasesRepository extends JpaRepository<Chart_diseases, Long> {
    List<Chart_diseases> findByChart(Chart chart); //의사> 차트 조회 (chart로 상병코드리스트 조회) by 은서
}
