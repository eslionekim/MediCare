package com.example.erp.Claim;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
	@Query("SELECT c FROM Claim c WHERE c.visit.visit_id = :visit_id") // 의사-> 차트 저장 버튼 (visit_id가 같은 claim찾기)
	Optional<Claim> findByVisitId(@Param("visit_id") Long visit_id);
}
