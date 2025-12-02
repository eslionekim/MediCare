package com.example.erp.Visit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
	//의사->금일 진료 리스트
	@Query("SELECT v FROM Visit v JOIN FETCH v.patient_id WHERE v.visit_datetime BETWEEN :start AND :end")
	// JOIN FETCH : visit과 관련된 parient 정보 가져오기,  
	List<Visit> findByVisitDate(@Param("start") LocalDateTime start,@Param("end") LocalDateTime end); // 의사-> 금일 진료리스트
	
	
	// 의사->금일 진료 리스트 -> 최종내원일 (오늘 방문은 제외)
	@Query("select max(v.visit_datetime) from Visit v where v.patient_id.patient_id= :patient_id and date(v.visit_datetime)< :today")
	// 가장 큰 visit_datetime, v.patient_id.patient_id : v.patient_id->patient 테이블 접근, :patient_id -> 매개변수와 일치하는 id , :today -> 매개변수보다 작은
	LocalDateTime findLastVisitBeforeToday(@Param("patient_id") Long patient_id,@Param("today") LocalDate today);
}