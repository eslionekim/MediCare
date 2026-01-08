package com.example.erp.Dispense;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.erp.Prescription.Prescription;

import jakarta.transaction.Transactional;

@Repository
public interface DispenseRepository extends JpaRepository<Dispense, Long>{
	//처방id로 조제가 있는지 확인-> 조제 대기인지 아닌지
	@Query("SELECT d FROM Dispense d WHERE d.prescription_id = :prescriptionId")
    Optional<Dispense> findByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

	//약사->조제리스트->처방id로 조제 있나 확인 by 은서
	@Query("""
		    SELECT (COUNT(d) > 0)
		    FROM Dispense d
		    WHERE d.prescription_id = :prescriptionId
		""")
		boolean existsByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

	// 약사-> 조제리스트->조제팝업->닫기 버튼 by은서
	@Modifying
    @Transactional
    @Query("DELETE FROM Dispense d WHERE d.prescription_id = :prescriptionId")
	void deleteByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
	

}
