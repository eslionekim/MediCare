package com.example.erp.Issue_request_item;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Issue_request_itemRepository extends JpaRepository<Issue_request_item, Long>{

	// 물류 -> 불출요청리스트 -> 승인 -> 요청 환산 수량 by 은서
	@Query("""
	    select (iri.requested_qty * i.pack_unit_qty)
	    from Issue_request_item iri
	    join Item i on iri.item_code = i.item_code
	    where iri.issue_request_id = :issueRequestId
	      and iri.item_code = :itemCode
	""")
	BigDecimal findConvertedQty(
	    @Param("issueRequestId") Long issueRequestId,
	    @Param("itemCode") String itemCode
	);

	// issue_request_id로 해당 아이템 조회
    @Query("SELECT i FROM Issue_request_item i WHERE i.issue_request_id = :issueRequestId")
    Optional<Issue_request_item> findByIssueRequestId(@Param("issueRequestId") Long issueRequestId);
}
