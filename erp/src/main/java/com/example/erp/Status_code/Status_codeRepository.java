package com.example.erp.Status_code;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Status_codeRepository  extends JpaRepository<Status_code, String> {
	// 인사 -> 휴가 리스트 -> 상태코드 수정, 의사 -> 휴가 신청 -> 승인 대기로 by 은서
	@Query("SELECT s FROM Status_code s WHERE s.status_code = :code")
	Optional<Status_code> findByCode(@Param("code") String code);
}
