package com.example.erp.Claim_item;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.erp.Claim.Claim;

public interface Claim_itemRepository extends JpaRepository<Claim_item, Long> {
	 // 의사 > 차트 조회 (Claim에 연결된 Claim_item 리스트 조회)
    @Query("SELECT ci FROM Claim_item ci WHERE ci.claim = :claim")
    List<Claim_item> findByClaim(@Param("claim") Claim claim);
}
