package com.example.erp.Reservation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 일정 날짜 구간(시작~끝) 예약 조회
    @Query("select r from Reservation r where r.start_time between :start and :end")
    List<Reservation> findByStartTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 특정 환자의 예약 조회(상태코드 복수) (접수 화면에서 사용)
    @Query("""
            select r
            from Reservation r
            join fetch r.user u
            join fetch r.department d
            join fetch r.status_code sc
            where r.patient.patient_id = :patientId
              and r.start_time between :start and :end
              and sc.status_code in :statusCodes
            order by r.start_time asc
            """)
    List<Reservation> findByPatientAndStartTimeBetweenAndStatusCodes(
            @Param("patientId") Long patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statusCodes") List<String> statusCodes);
}
