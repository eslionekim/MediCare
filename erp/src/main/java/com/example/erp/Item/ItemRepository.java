package com.example.erp.Item;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item,Long>{
	// 차트 저장 시 처방항목 저장
	@Query("SELECT i FROM Item i WHERE i.fee_item_code = :fee_item_code")
	Optional<Item> findByFeeItemCode(@Param("fee_item_code") Long fee_item_code);
}
