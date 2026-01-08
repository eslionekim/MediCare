package com.example.erp.Dispense_item;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Dispense_itemRepository extends JpaRepository<Dispense_item, Long>{
	//약사->조제팝업->조제완료 시 Dispense_item 생성
	@Modifying
    @Query("INSERT INTO Dispense_item(dispense_id, prescription_item_id, item_code, quantity) " +
           "VALUES (:dispenseId, :prescriptionItemId, :itemCode, :quantity)")
    void insertItem(@Param("dispenseId") Long dispenseId,
                    @Param("prescriptionItemId") Long prescriptionItemId,
                    @Param("itemCode") String itemCode,
                    @Param("quantity") BigDecimal quantity);
}
