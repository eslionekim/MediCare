package com.example.erp.Vacation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VacationRepository extends JpaRepository<Vacation, Long> {
	@Query("SELECT v FROM Vacation v JOIN FETCH v.user_account u JOIN FETCH v.vacation_type vt JOIN FETCH v.status_code sc LEFT JOIN FETCH u.staff_profile sp LEFT JOIN FETCH sp.department d")
	List<Vacation> vacationList();
	
	// 의사 -> 휴가 신청 -> 로그인한 직원(user_id)의 휴가만 조회
    @Query("SELECT v FROM Vacation v JOIN FETCH v.vacation_type vt JOIN FETCH v.status_code sc WHERE v.user_account.user_id = :user_id")
    List<Vacation> findVacationByUserId(@Param("user_id") String user_id);
}
