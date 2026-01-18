package com.example.erp.Prescription_item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.erp.Stock.Stock;

@Repository
public interface Prescription_itemRepository extends JpaRepository<Prescription_item, Long>{
	// 약사->조제리스트 by은서
	@Query("SELECT pi FROM Prescription_item pi WHERE pi.prescription_id = :prescriptionId")
    List<Prescription_item> findByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

    //의사-> 차트 조회
    @Query("""
            SELECT pi
            FROM Prescription_item pi, Prescription p
            WHERE pi.prescription_id = p.prescription_id
              AND p.visit_id = :visitId
              AND pi.item_code = :itemCode
        """)
        List<Prescription_item> findAllByItemCodeAndVisitId(
                @Param("itemCode") String itemCode,
                @Param("visitId") Long visitId
        );
        
        // 의사->차트 재작성
    @Modifying
    @Query("DELETE FROM Prescription_item pi WHERE pi.prescription_id = :pid")
    void deleteByPrescriptionId(@Param("pid") Long prescriptionId);
    
    //의사->차트 재작성
    @Query("""
            SELECT pi
            FROM Prescription_item pi
            JOIN Prescription p
                ON pi.prescription_id = p.prescription_id
            WHERE p.visit_id = :visitId
        """)
        List<Prescription_item> findAllByVisitId(
                @Param("visitId") Long visitId
        );

}
