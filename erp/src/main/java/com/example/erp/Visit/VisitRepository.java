package com.example.erp.Visit;

import com.example.erp.Patient.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    // 금일 진료 리스트 -> 최종 내원일(오늘 방문 제외)
    @Query("select max(v.visit_datetime) from Visit v where v.patient.patient_id = :patient_id and date(v.visit_datetime) < :today")
    LocalDateTime findLastVisitBeforeToday(@Param("patient_id") Long patient_id, @Param("today") LocalDateTime today);
    // 진료 시간으로 datetime컬럼 만든거라 LocalDateTime일수밖에 없음
    // 진료 시작 -> patient_id로 visit 리스트 조회 (최근 방문 순)

    // 의사->금일 진료 리스트 -> 최종내원일 (오늘 방문은 제외)
    @Query("select v from Visit v left join fetch v.user_account ua where v.patient.patient_id = :patientId order by v.visit_datetime desc")
    List<Visit> findRecentByPatient(@Param("patientId") Long patientId);

    // 의사->금일 진료 리스트
    @Query("SELECT v FROM Visit v JOIN FETCH v.patient WHERE v.visit_datetime BETWEEN :start AND :end")
    // JOIN FETCH : visit과 관련된 parient 정보 가져오기,
    List<Visit> findByVisitDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end); // 의사-> 금일 진료리스트

    // 의사 -> 차트 작성 -> 환자 번호로 내원 기록 조회
    @Query("SELECT v FROM Visit v WHERE v.patient.patient_id = :patient_id")
    List<Visit> findByPatientId(@Param("patient_id") Long patient_id);

    // 의사 -> 차트 작성 -> visit 한건 찾고 연관된 department,insurance_code 찾아서 진료과,보험 추출
    @Query("SELECT v FROM Visit v JOIN FETCH v.department JOIN FETCH v.insurance_code WHERE v.visit_id = :visit_id")
    Visit findWithDepartmentAndInsurance(@Param("visit_id") Long visit_id);

    @Query("SELECT v FROM Visit v JOIN FETCH v.patient LEFT JOIN FETCH v.user_account LEFT JOIN FETCH v.insurance_code JOIN v.status_code s WHERE s.status_code IN ('VIS_CLAIMED', 'VIS_COMPLETED') ORDER BY v.created_at DESC")
    List<Visit> findAllVisits();

}
