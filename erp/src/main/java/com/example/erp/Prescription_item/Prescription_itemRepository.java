package com.example.erp.Prescription_item;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Prescription_itemRepository extends JpaRepository<Prescription_item, Long>{
	// 약사->조제리스트 by은서
	@Query("SELECT pi FROM Prescription_item pi WHERE pi.prescription_id = :prescriptionId")
    List<Prescription_item> findByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
