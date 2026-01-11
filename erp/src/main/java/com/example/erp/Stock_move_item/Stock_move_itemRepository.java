package com.example.erp.Stock_move_item;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Stock_move_itemRepository extends JpaRepository<Stock_move_item, Long>{
	//물류->출고리스트->재고이동항목 접근 by 은서
	@Query("select smi from Stock_move_item smi where smi.stock_move_id = :stockMoveId")
	Stock_move_item findByStockMoveId(@Param("stockMoveId") Long stockMoveId);
}
