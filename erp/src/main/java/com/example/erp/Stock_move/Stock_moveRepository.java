package com.example.erp.Stock_move;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.erp.Stock.Stock;

@Repository
public interface Stock_moveRepository extends JpaRepository<Stock_move, Long>{
	//물류->출고리스트
	@Query("""
			select sm
			from Stock_move sm
			left join Warehouse w 
			    on sm.from_warehouse_code = w.warehouse_code
			where 
			    sm.move_type = 'transfer'
			    or (
			        sm.move_type = 'outbound'
			        and w.name = '물류창고'
			    )
			""")
	List<Stock_move> findLogisOutboundMoves();
	
	//약사->출고리스트
	@Query("""
		    select sm
		    from Stock_move sm
		    left join Warehouse w 
		        on sm.from_warehouse_code = w.warehouse_code
		    where sm.move_type = 'outbound'
		       and (w.name = '약제창고' or sm.dispense_id is not null)
		""")
		List<Stock_move> findPharmOutboundMoves();
		
	//원무->출고리스트
	@Query("""
		    select sm
		    from Stock_move sm
		    left join Warehouse w 
		        on sm.from_warehouse_code = w.warehouse_code
		    where sm.move_type = 'outbound'
		       and (w.name = '원무창고' or sm.dispense_id is not null)
		""")
		List<Stock_move> findExOutboundMoves();


	//원무->출고리스트->로트코드
	@Query("SELECT s FROM Stock_move s WHERE s.from_warehouse_code = :code")
	Stock_move findByFromWarehouseCode(@Param("code") String code);
}
