package com.example.erp.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.erp.Dispense.DispenseLotDTO;

@Repository
public interface StockRepository extends JpaRepository<Stock,Long>{

	// 물류->불출요청리스트->승인->가용재고 by 은서
	@Query("""
		    select coalesce(sum(s.quantity), 0)
		    from Stock s
		    JOIN Warehouse w
			  ON s.warehouse_code = w.warehouse_code
		    where s.item_code = :itemCode
		      and (
			        s.outbound_deadline is null
			        or s.outbound_deadline >= CURRENT_DATE
			      )
		     AND w.name = '물류창고'
		""")
	BigDecimal findTotalAvailableQty(@Param("itemCode") String itemCode);

	// 물류->불출요청리스트->승인->lot리스트 by 은서
	@Query("""
		    select new com.example.erp.Stock.LotcodeDTO(
		        s.stock_id,
		        s.lot_code,
		        s.outbound_deadline,
		        s.quantity
		    )
		    from Stock s
		    join Warehouse w
			    on s.warehouse_code = w.warehouse_code
		    where s.item_code = :itemCode
		      and w.name = '물류창고'
		      and (s.outbound_deadline is null or s.outbound_deadline >= CURRENT_DATE)
		    order by s.outbound_deadline asc
		""")
	List<LotcodeDTO> findOutboundLots(@Param("itemCode") String itemCode);


	// 물류 -> 불출요청리스트 -> 출고 by 은서
	@Query("SELECT s FROM Stock s WHERE s.lot_code = :lotCode")
    Optional<Stock> findByLotCode(@Param("lotCode") String lotCode);
	
	// 물류 -> 전체재고현황 -> lot를 물류창고에서만 가져오기 by 은서
	@Query("SELECT s FROM Stock s JOIN Warehouse w ON s.warehouse_code = w.warehouse_code WHERE s.item_code = :itemCode AND w.name = '물류창고' AND s.quantity > 0")
    List<Stock> findByItemCode(@Param("itemCode") String itemCode);
	
	// 약사-> 조제리스트-> 조제 팝업창->유통기한을 넘기지 않은 해당 아이템 구하기 by은서
	@Query("""
		    SELECT new com.example.erp.Dispense.DispenseLotDTO(
		        s.stock_id,
		        s.lot_code,
		        s.quantity,
		        s.expiry_date,
		        CONCAT(w.location, '-', w.zone)
		    )
		    FROM Stock s
		    JOIN Warehouse w
		      ON s.warehouse_code = w.warehouse_code
		    WHERE s.item_code = :itemCode
		      AND w.name = '약제창고'
		      AND s.quantity > 0
		      AND s.expiry_date >= CURRENT_DATE
		    ORDER BY s.expiry_date ASC
		""")
		List<DispenseLotDTO> findPharmLots(@Param("itemCode") String itemCode);



	//약사->조제리스트->조제팝업창->재고 by은서
	@Query("""
	        SELECT COALESCE(SUM(s.quantity), 0)
	        FROM Stock s
	        WHERE s.item_code = :itemCode
	          AND s.expiry_date >= CURRENT_DATE
			  AND s.warehouse_code in (select warehouse_code from Warehouse
			  where name='약제창고')
	    """)
	BigDecimal sumAvailableStock(@Param("itemCode") String itemCode);
	
	//약사->전체재고현황->약제창고인 것만 가져오기->by은서
	@Query("SELECT s FROM Stock s " +
		       "JOIN Warehouse w ON s.warehouse_code = w.warehouse_code " +
		       "WHERE s.item_code = :itemCode " +
		       "AND w.name = '약제창고' AND s.quantity > 0")
		List<Stock> findDrugWarehouseStockByItemCode(@Param("itemCode") String itemCode);
	
	//원무->전체재고현황->약제창고인 것만 가져오기->by은서
	@Query("SELECT s FROM Stock s " +
		       "JOIN Warehouse w ON s.warehouse_code = w.warehouse_code " +
		       "WHERE s.item_code = :itemCode " +
		       "AND w.name = '원무창고' AND s.quantity > 0")
		List<Stock> findExWarehouseStockByItemCode(@Param("itemCode") String itemCode);

	//원무->출고리스트
	@Query("SELECT s FROM Stock s WHERE s.warehouse_code = :warehouseCode")
    Stock findByWarehouseCode(@Param("warehouseCode") String warehouseCode);

	 // 의사->차트 조회
    @Query("""
	    SELECT s
	    FROM Stock s
	    WHERE s.item_code IN (
	        SELECT i.item_code
	        FROM Item i
	        WHERE i.fee_item_code = :feeItemCode
	          AND i.is_active = true
	    )
	      AND s.quantity > 0
	      AND s.expiry_date IS NOT NULL
	    ORDER BY s.expiry_date ASC
	""")
	List<Stock> findByFeeItemCodeOrderByExpiry(
	        @Param("feeItemCode") String feeItemCode
	);
}
