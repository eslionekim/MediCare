package com.example.erp.Claim_item;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.erp.Claim.Claim;

public interface Claim_itemRepository extends JpaRepository<Claim_item, Long> {
	 // 의사 > 차트 조회 (Claim에 연결된 Claim_item 리스트 조회) by 은서
	@Query("SELECT ci FROM Claim_item ci WHERE ci.claim.visit.visit_id = :visitId")
	List<Claim_item> findAllByVisitId(@Param("visitId") Long visitId);

	//의사 > 차트조회
	 @Query("""
        SELECT ci
        FROM Claim_item ci
        WHERE ci.claim = :claim
    """)
    List<Claim_item> findAllByClaim(@Param("claim") Claim claim);
	
	// 준광이 코드에서 에러나서 은서가 만듦 PaymentService 52줄
	@Query("SELECT ci FROM Claim_item ci WHERE ci.claim = :claim")
    List<Claim_item> findByClaim(@Param("claim") Claim claim);
    
    //의사->차트 재저장
    @Modifying
    @Query("DELETE FROM Claim_item ci WHERE ci.claim = :claim")
    void deleteByClaim(@Param("claim") Claim claim);

}
