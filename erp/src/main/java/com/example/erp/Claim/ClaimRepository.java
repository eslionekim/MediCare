package com.example.erp.Claim;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
	@Query("SELECT c FROM Claim c WHERE c.visit.visit_id = :visit_id") // doctor -> chart save (by visit_id)
	List<Claim> findByVisitId(@Param("visit_id") Long visit_id);

	@Query("SELECT c FROM Claim c WHERE c.visit.visit_id = :visitId") // doctor -> chart view
	List<Claim> findAllByVisitId(@Param("visitId") Long visitId);
}
