package com.example.erp.Work_schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface Work_scheduleRepository extends JpaRepository<Work_schedule, Long> {

	// 인사 -> 스케줄 부여 by 은서
    @Query("SELECT w FROM Work_schedule w WHERE w.user_account.user_id = :user_id AND w.work_date BETWEEN :start AND :end")
    List<Work_schedule> findByUserAndMonth(@Param("user_id") String user_id,@Param("start") LocalDate start,@Param("end") LocalDate end);

    // 의사 -> 스케줄 조회 by 은서
    @Query("""
	 SELECT new com.example.erp.Work_schedule.ScheduleCalendarDTO(
	        ws.work_date,
	        wt.work_type_code,
	        wt.work_name,
	        ws.start_time,
	        ws.end_time,
	        sc.name
	    )
	    FROM Work_schedule ws
	    JOIN ws.work_type wt
	    JOIN ws.user_account u
	    LEFT JOIN ws.status_code sc
	    WHERE u.user_id = :userId
	      AND YEAR(ws.work_date) = :year
	      AND MONTH(ws.work_date) = :month
    	""")
    List<ScheduleCalendarDTO> findDoctorMonthlySchedule(
    	        @Param("userId") String userId,
    	        @Param("year") int year,
    	        @Param("month") int month
    );
    
 // 약사 -> 스케줄 조회 by 은서
    @Query("""
	 SELECT new com.example.erp.Work_schedule.ScheduleCalendarDTO(
	        ws.work_date,
	        wt.work_type_code,
	        wt.work_name,
	        ws.start_time,
	        ws.end_time,
	        sc.name
	    )
	    FROM Work_schedule ws
	    JOIN ws.work_type wt
	    JOIN ws.user_account u
	    LEFT JOIN ws.status_code sc
	    WHERE u.user_id = :userId
	      AND YEAR(ws.work_date) = :year
	      AND MONTH(ws.work_date) = :month
    	""")
    List<ScheduleCalendarDTO> findPharmMonthlySchedule(
    	        @Param("userId") String userId,
    	        @Param("year") int year,
    	        @Param("month") int month
    );
    
    // 원무 -> 스케줄 조회 by 은서
    @Query("""
	 SELECT new com.example.erp.Work_schedule.ScheduleCalendarDTO(
	        ws.work_date,
	        wt.work_type_code,
	        wt.work_name,
	        ws.start_time,
	        ws.end_time,
	        sc.name
	    )
	    FROM Work_schedule ws
	    JOIN ws.work_type wt
	    JOIN ws.user_account u
	    LEFT JOIN ws.status_code sc
	    WHERE u.user_id = :userId
	      AND YEAR(ws.work_date) = :year
	      AND MONTH(ws.work_date) = :month
    	""")
    List<ScheduleCalendarDTO> findStaffMonthlySchedule(
    	        @Param("userId") String userId,
    	        @Param("year") int year,
    	        @Param("month") int month
    );

    // 인사 -> 전체 스케줄 조회 by 은서
    @Query(""" 
    	    SELECT new com.example.erp.Work_schedule.Work_scheduleDTO$HrDailyScheduleItem(
    	        d.name,
    	        u.user_id,
    	        u.name,
    	        wt.work_name,
    	        vt.type_name
    	    )
    	    FROM Work_schedule ws
    	    JOIN ws.department d
    	    JOIN ws.user_account u
    	    JOIN ws.work_type wt
    	    LEFT JOIN Vacation v
			    ON v.user_account = u
			    AND :date BETWEEN v.start_date AND v.end_date
			LEFT JOIN v.vacation_type vt
    	    WHERE ws.work_date = :date
    	""")
    List<Work_scheduleDTO.HrDailyScheduleItem> findDailySchedule(@Param("date") LocalDate date);

    //의사 -> 출근,퇴근 버튼 by 은서
    @Query("""
    	    SELECT ws
    	    FROM Work_schedule ws
    	    JOIN ws.user_account u
    	    WHERE u.user_id = :userId
    	      AND ws.work_date = :workDate
    	""")
    Optional<Work_schedule> findByUser_account_UserIdAndWork_date(@Param("userId") String userId, @Param("workDate") LocalDate workDate);

    // work_schedule -> 출퇴근시간 토대로 status_code 지정 -> 어제 날짜까지 by 은서
    @Query("""
    	    SELECT ws
    	    FROM Work_schedule ws
    	    JOIN FETCH ws.work_type wt
    	    JOIN FETCH ws.user_account u
    	    WHERE ws.work_date <= :targetDate
    	      AND ws.status_code IS NULL
    	    """)
    List<Work_schedule> findTargetSchedules(@Param("targetDate") LocalDate targetDate);

    //로그아웃 버튼-> 가장 최근 출근 기록 1개만 가져와서 퇴근찍었는지
    @Query("""
    	    SELECT ws
    	    FROM Work_schedule ws
    	    WHERE ws.user_account.user_id = :userId
    	      AND ws.start_time IS NOT NULL
    	    ORDER BY ws.work_date DESC, ws.start_time DESC
    	""")
    Optional<Work_schedule> findMostRecentWork(@Param("userId") String userId);

    // 퇴근 안 찍은 과거 출근들 전부 SCH_XOUT 로 처리
    @Transactional
    @Modifying
    @Query("""
	       UPDATE Work_schedule ws
		    SET ws.status_code = (SELECT s FROM Status_code s WHERE s.status_code = 'SCH_XOUT')
		    WHERE ws.user_account.user_id = :userId
		      AND ws.end_time IS NULL
		      AND ws.work_date < CURRENT_DATE
		      AND ws.status_code.status_code NOT IN ('SCH_CANCELLED')
    	       """)
    int markUnclosedWorkAsXout(@Param("userId") String userId);
   
    // 정상적으로 퇴근처리 해야 하는 "현재 미퇴근 1건 찾기"
    @Query("""
    	    SELECT ws
    	    FROM Work_schedule ws
    	    LEFT JOIN ws.status_code sc
    	    WHERE ws.user_account.user_id = :userId
    	      AND ws.start_time IS NOT NULL
    	      AND ws.end_time IS NULL
    	      AND (sc IS NULL OR sc.status_code <> 'SCH_XOUT')
    	    ORDER BY ws.start_time DESC
    	    """)
    Optional<Work_schedule> findOpenNormalWork(@Param("userId") String userId);
}

