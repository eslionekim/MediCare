package com.example.erp.Status_code;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Status_codeRepository  extends JpaRepository<Status_code, String> {
	@Query("SELECT s FROM Status_code s WHERE s.status_code = :code")
    Status_code findByCode(@Param("code") String code);
}
