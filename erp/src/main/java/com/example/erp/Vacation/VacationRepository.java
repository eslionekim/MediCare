package com.example.erp.Vacation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VacationRepository extends JpaRepository<Vacation, Long> {
	// 인사 -> 휴가 리스트 by 은서
	@Query("SELECT v FROM Vacation v JOIN FETCH v.user_account u JOIN FETCH v.vacation_type vt JOIN FETCH v.status_code sc LEFT JOIN FETCH u.staff_profile sp LEFT JOIN FETCH sp.department d")
	List<Vacation> vacationList();
	
	// 의사 -> 휴가 신청 -> 로그인한 직원(user_id)의 휴가만 조회 by 은서
    @Query("SELECT v FROM Vacation v JOIN FETCH v.vacation_type vt JOIN FETCH v.status_code sc WHERE v.user_account.user_id = :user_id")
    List<Vacation> findVacationByUserId(@Param("user_id") String user_id);

    // 출퇴근 버튼->status_code업데이트  by 은서
    @Query("""
    		SELECT COUNT(v) > 0 FROM Vacation v
    		WHERE v.user_account.user_id = :userId
    		AND :date BETWEEN v.start_date AND v.end_date
    		AND v.status_code.status_code = 'VAC_APPROVED'
    		""")
    		boolean existsApprovedVacation(
    		    @Param("userId") String userId,
    		    @Param("date") LocalDate date
    		);

    // 의사-> 스케줄 조회-> 휴가리스트 -> 검색창
    @Query("""
    	    SELECT v
    	    FROM Vacation v
    	    JOIN v.vacation_type vt
    	    JOIN v.status_code sc
    	    WHERE v.user_account.user_id = :userId
    	      AND (:typeCode IS NULL OR vt.vacation_type_code = :typeCode)
    	      AND (:statusCode IS NULL OR sc.status_code = :statusCode)
    	      AND (
    	            :date IS NULL
    	            OR (:date BETWEEN v.start_date AND v.end_date)
    	      )
    	    ORDER BY v.start_date DESC
    	""")
    	List<Vacation> searchVacations(
    	    @Param("userId") String userId,
    	    @Param("typeCode") String typeCode,
    	    @Param("statusCode") String statusCode,
    	    @Param("date") LocalDate date
    	);

    
}
