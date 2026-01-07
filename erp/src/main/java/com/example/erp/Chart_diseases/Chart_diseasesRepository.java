package com.example.erp.Chart_diseases;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.erp.Chart.Chart;

public interface Chart_diseasesRepository extends JpaRepository<Chart_diseases, Long> {
    List<Chart_diseases> findByChart(Chart chart); //의사> 차트 조회 (chart로 상병코드리스트 조회) by 은서

    // 약사->조제리스트->조제팝업->주증상
    @Query("""
            SELECT cd
            FROM Chart_diseases cd
            WHERE cd.chart = :chart
            ORDER BY cd.is_primary DESC, cd.chart_diseases_id ASC
        """)
    List<Chart_diseases> findByChartOrderByPrimary(@Param("chart") Chart chart);
}
