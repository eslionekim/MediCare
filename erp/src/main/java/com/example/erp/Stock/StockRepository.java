package com.example.erp.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock,Long>{

	// 물류->불출요청리스트->승인->가용재고 by 은서
	@Query("""
		    select coalesce(sum(s.quantity), 0)
		    from Stock s
		    where s.item_code = :itemCode
		      and s.outbound_deadline >= CURRENT_DATE
		""")
	BigDecimal findTotalAvailableQty(@Param("itemCode") Long itemCode);

	// 물류->불출요청리스트->승인->lot리스트 by 은서
	@Query("""
		    select new com.example.erp.Stock.LotcodeDTO(
		        s.stock_id,
		        s.lot_code,
		        s.outbound_deadline,
		        s.quantity
		    )
		    from Stock s
		    where s.item_code = :itemCode
		      and (s.outbound_deadline is null or s.outbound_deadline >= CURRENT_DATE)
		    order by s.outbound_deadline asc
		""")
		List<LotcodeDTO> findOutboundLots(@Param("itemCode") Long itemCode);


	// 물류 -> 불출요청리스트 -> 출고 by 은서
	@Query("SELECT s FROM Stock s WHERE s.lot_code = :lotCode")
    Optional<Stock> findByLotCode(@Param("lotCode") String lotCode);
}
