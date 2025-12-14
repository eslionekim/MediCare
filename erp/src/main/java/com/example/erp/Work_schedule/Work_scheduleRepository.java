package com.example.erp.Work_schedule;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Work_scheduleRepository extends JpaRepository<Work_schedule, Long> {

    @Query("SELECT w FROM Work_schedule w WHERE w.user_account.user_id = :user_id AND w.work_date BETWEEN :start AND :end")
    List<Work_schedule> findByUserAndMonth(@Param("user_id") String user_id,@Param("start") LocalDate start,@Param("end") LocalDate end);
}

