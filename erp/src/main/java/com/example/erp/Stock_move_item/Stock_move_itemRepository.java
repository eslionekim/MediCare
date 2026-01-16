package com.example.erp.Stock_move_item;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.erp.Stock_move.StockInRequestDTO;

@Repository
public interface Stock_move_itemRepository extends JpaRepository<Stock_move_item, Long>{
	//물류->출고리스트->재고이동항목 접근 by 은서
	@Query("select smi from Stock_move_item smi where smi.stock_move_id = :stockMoveId")
	Stock_move_item findByStockMoveId(@Param("stockMoveId") Long stockMoveId);

	//약사->불출리스트
	@Query("""
		   SELECT new com.example.erp.Stock_move.StockInRequestDTO(
			        sm.stock_move_id,
			        i.name,
			        smi.quantity,
			        sm.moved_at
			    )
			    FROM Stock_move sm
			    JOIN Stock_move_item smi ON sm.stock_move_id = smi.stock_move_id
			    JOIN Item i ON smi.item_code = i.item_code
			    WHERE sm.issue_request_id IS NOT NULL
			      AND sm.to_warehouse_code IS NULL
			      AND sm.status_code = 'SM_request'
			      AND i.item_type = '약품'
			      AND smi.item_code = :itemCode
		""")
		List<StockInRequestDTO> findPendingDrugStockInList(@Param("itemCode") String itemCode);
	
	//원무->불출리스트
		@Query("""
			    SELECT new com.example.erp.Stock_move.StockInRequestDTO(
			        sm.stock_move_id,
			        i.name,
			        smi.quantity,
			        sm.moved_at
			    )
			    FROM Stock_move sm
			    JOIN Stock_move_item smi ON sm.stock_move_id = smi.stock_move_id
			    JOIN Item i ON smi.item_code = i.item_code
			    WHERE sm.issue_request_id IS NOT NULL
			      AND sm.to_warehouse_code IS NULL
			      AND sm.status_code = 'SM_request'
			      AND i.item_type <> '약품'
			      AND smi.item_code = :itemCode
			""")
			List<StockInRequestDTO> findPendingExStockInList(@Param("itemCode") String itemCode);

}
