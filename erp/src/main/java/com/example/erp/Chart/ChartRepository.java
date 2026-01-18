package com.example.erp.Chart;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChartRepository extends JpaRepository<Chart, Long> {
	// 의사 -> 진료 시작 버튼 누를 시 -> 이미 차트 테이블 생성되어있는지 확인
	// chart_diseases까지 fetch join
	@Query("SELECT c FROM Chart c LEFT JOIN FETCH c.chart_diseases WHERE c.visit.visit_id = :visit_id")
	Optional<Chart> findByVisitIdWithDiseases(@Param("visit_id") Long visit_id);

	
	// 기존 Optional -> List로 변경
	@Query("SELECT c FROM Chart c WHERE c.visit.visit_id = :visit_id")
	List<Chart> findByVisitIdList(@Param("visit_id") Long visit_id);
	
	//의사->차트저장
	@Query("SELECT c FROM Chart c WHERE c.visit.visit_id = :visitId")
    Optional<Chart> findByVisitId(@Param("visitId") Long visitId);

	@Query("""
	        SELECT c
	        FROM Chart c
	        WHERE c.visit.visit_id = :visitId
	    """)
	    Optional<Chart> findByVisit_VisitId(@Param("visitId") Long visitId);
}
