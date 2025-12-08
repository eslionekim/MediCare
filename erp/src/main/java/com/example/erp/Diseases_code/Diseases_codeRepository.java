package com.example.erp.Diseases_code;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Diseases_codeRepository extends JpaRepository<Diseases_code, String> {
	@Query("SELECT d FROM Diseases_code d WHERE d.name_kor LIKE %:keyword% OR d.diseases_code LIKE %:keyword%")
	List<Diseases_code> searchAll(@Param("keyword") String keyword);

}
