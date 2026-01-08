package com.example.erp.Insurance_code;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Insurance_codeRepository extends JpaRepository<Insurance_code, String> {
	@Query("select i from Insurance_code i where i.insurance_code = :code")
	Optional<Insurance_code> findByInsuranceCode(@Param("code") String code);

}
