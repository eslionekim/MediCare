package com.example.erp.Work_schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // 인사 -> 전체 스케줄 조회 by 은서
    @Query(""" 
    	    SELECT new com.example.erp.Work_schedule.Work_scheduleDTO$HrDailyScheduleItem(
    	        d.name,
    	        u.user_id,
    	        u.name,
    	        wt.work_name,
    	        sc.name
    	    )
    	    FROM Work_schedule ws
    	    JOIN ws.department d
    	    JOIN ws.user_account u
    	    JOIN ws.work_type wt
    	    LEFT JOIN ws.status_code sc
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

}

