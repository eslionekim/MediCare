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
    // ?? -> ?? ?? ???
    @Query("SELECT v FROM Visit v JOIN FETCH v.patient WHERE v.visit_datetime BETWEEN :start AND :end")
    List<Visit> findByVisitDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ?? -> ?? ?? ??? -> ?? ???(?? ??? ??)
    @Query("select max(v.visit_datetime) from Visit v where v.patient.patient_id = :patient_id and date(v.visit_datetime) < :today")
    LocalDateTime findLastVisitBeforeToday(@Param("patient_id") Long patient_id, @Param("today") LocalDate today);

    // ?? ??? ?? ?? ??
    @Query("select v from Visit v left join fetch v.user_account ua where v.patient.patient_id = :patientId order by v.visit_datetime desc")
    List<Visit> findRecentByPatient(@Param("patientId") Long patientId);
}
