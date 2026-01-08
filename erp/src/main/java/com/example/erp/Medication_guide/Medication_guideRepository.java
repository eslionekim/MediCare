package com.example.erp.Medication_guide;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Medication_guideRepository extends JpaRepository<Medication_guide, Long> {
	//약사->투약팝업->복약지도 by 은서
	@Query("select m from Medication_guide m where m.item_code = :itemCode")
	Optional<Medication_guide> findByItemCode(@Param("itemCode") String itemCode);
}
