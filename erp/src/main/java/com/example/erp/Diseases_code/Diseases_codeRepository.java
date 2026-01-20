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

	// 의사->진단 코드 본인과 -> keyword가 없을 경우 department 기준 조회
    @Query("SELECT d FROM Diseases_code d WHERE d.department = :department AND d.is_active = true")
    List<Diseases_code> findByDepartment(@Param("department") String department);

    // keyword 포함 + department 기준 조회
    @Query("SELECT d FROM Diseases_code d " +
           "WHERE d.department = :department AND d.is_active = true " +
           "AND (d.diseases_code LIKE %:keyword% OR d.name_kor LIKE %:keyword%)")
    List<Diseases_code> searchByDepartmentAndKeyword(@Param("department") String department,
                                                     @Param("keyword") String keyword);
}
