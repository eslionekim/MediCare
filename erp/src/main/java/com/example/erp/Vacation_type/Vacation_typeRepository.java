package com.example.erp.Vacation_type;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Vacation_typeRepository extends JpaRepository<Vacation_type, String> {
	// 의사 -> 휴가 신청 -> 선택한 type_name으로 vacation_type_code 찾으려고 by 은서
	@Query("select vt from Vacation_type vt where vt.type_name = :type_name")
	Optional<Vacation_type> findByTypeName(@Param("type_name") String type_name);
}