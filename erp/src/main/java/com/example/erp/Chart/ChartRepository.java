package com.example.erp.Chart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChartRepository extends JpaRepository<Chart, Long> {
	// 의사 -> 진료 시작 버튼 누를 시 -> 이미 차트 테이블 생성되어있는지 확인
	@Query("SELECT c FROM Chart c WHERE c.visit.visit_id = :visit_id")
	Optional<Chart> findByVisitId(@Param("visit_id") Long visit_id);
}
