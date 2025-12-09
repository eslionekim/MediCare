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
    LocalDateTime findLastVisitBeforeToday(@Param("patient_id") Long patientId, @Param("today") LocalDate today);

    // 환자 상세용 최근 방문 내역 (의사 포함)
    @Query("select v from Visit v left join fetch v.user_account ua where v.patient.patient_id = :patientId order by v.visit_datetime desc")
    List<Visit> findRecentByPatient(@Param("patientId") Long patientId);

    // 특정 기간 방문 (시간순)
    @Query("select v from Visit v where v.visit_datetime between :start and :end order by v.visit_datetime asc")
    List<Visit> findByVisitDatetimeBetweenOrderByVisitDatetimeAsc(@Param("start") LocalDateTime start,
                                                                  @Param("end") LocalDateTime end);

    // 환자별 방문 이력 (최근순)
    @Query("select v from Visit v where v.patient = :patient order by v.visit_datetime desc")
    List<Visit> findByPatientOrderByVisitDatetimeDesc(@Param("patient") Patient patient);

    // 환자별 방문 이력 (환자 ID로 조회)
    @Query("select v from Visit v where v.patient.patient_id = :patientId order by v.visit_datetime desc")
    List<Visit> findByPatientId(@Param("patientId") Long patientId);

    // 진료 시간대 조회 (의사 화면)
    @Query("SELECT v FROM Visit v JOIN FETCH v.patient WHERE v.visit_datetime BETWEEN :start AND :end")
    List<Visit> findByVisitDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 차트 작성 시 department, insurance 함께 조회
    @Query("SELECT v FROM Visit v JOIN FETCH v.department JOIN FETCH v.insurance_code WHERE v.visit_id = :visit_id")
    Visit findWithDepartmentAndInsurance(@Param("visit_id") Long visitId);

    // 청구 완료/종료 방문 조회
    @Query("SELECT v FROM Visit v JOIN FETCH v.patient LEFT JOIN FETCH v.user_account LEFT JOIN FETCH v.insurance_code JOIN v.status_code s WHERE s.status_code IN ('VIS_CLAIMED', 'VIS_COMPLETED') ORDER BY v.created_at DESC")
    List<Visit> findAllVisits();
}

