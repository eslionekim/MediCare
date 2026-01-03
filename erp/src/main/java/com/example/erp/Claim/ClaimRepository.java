package com.example.erp.Claim;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
	@Query("SELECT c FROM Claim c WHERE c.visit.visit_id = :visit_id") // 의사-> 차트 저장 버튼 (visit_id가 같은 claim찾기) by 은서
	Optional<Claim> findByVisitId(@Param("visit_id") Long visit_id);
	
	@Query("SELECT c FROM Claim c WHERE c.visit.visit_id = :visitId") //의사-> 차트 조회
	List<Claim> findAllByVisitId(@Param("visitId") Long visitId); // visit_id로 모든 Claim 조회
}
