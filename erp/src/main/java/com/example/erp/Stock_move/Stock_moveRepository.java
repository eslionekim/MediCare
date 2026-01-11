package com.example.erp.Stock_move;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

}
