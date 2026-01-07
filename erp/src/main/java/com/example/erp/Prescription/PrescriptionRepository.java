package com.example.erp.Prescription;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long>{
	//약사-> 조제리스트-> 가장 최근 처방
	@Query("""
		    SELECT MAX(p.prescribed_at)
		    FROM Prescription p, Visit v
		    WHERE p.visit_id = v.visit_id
		      AND v.patient.patient_id = :patientId
		""")
	    Optional<LocalDateTime> findLatestPrescribedAtByPatient(
	        @Param("patientId") Long patientId
	    );
}
