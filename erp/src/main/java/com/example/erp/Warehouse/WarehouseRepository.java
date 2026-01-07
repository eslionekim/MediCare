package com.example.erp.Warehouse;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String>{
	// 물류->전체재고->로트리스트->입고등록->위치 by 은서
	@Query("SELECT DISTINCT w.location FROM Warehouse w WHERE w.name = '물류창고' ORDER BY w.location ")
	List<String> findDistinctLocations();

	// 물류->전체재고->로트리스트->입고등록->구간 by 은서
	@Query("""
		    SELECT DISTINCT w.zone
		    FROM Warehouse w
		    WHERE w.name = '물류창고'
		      AND w.location = :location
		    ORDER BY w.zone
		""")
	List<String> findDistinctZonesByLocation(@Param("location") String location);

	// 물류->전체재고->로트리스트->입고등록->창고코드 찾기 by 은서
	@Query("""
	        SELECT w
	        FROM Warehouse w
	        WHERE w.name = :name
	          AND w.location = :location
	          AND w.zone = :zone
	    """)
	    Optional<Warehouse> findWarehouse(
	        @Param("name") String name,
	        @Param("location") String location,
	        @Param("zone") String zone
	    );
	
	
}
