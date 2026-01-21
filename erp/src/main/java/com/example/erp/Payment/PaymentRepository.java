package com.example.erp.Payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("select (count(p) > 0) from Payment p where p.visit.visit_id = :visitId")
    boolean existsByVisitId(@Param("visitId") Long visitId);

    @Query("select p from Payment p where p.visit.visit_id = :visitId")
    Optional<Payment> findByVisitId(@Param("visitId") Long visitId);

    @Query("select p from Payment p where p.visit.visit_id = :visitId order by p.paid_at desc, p.payment_id desc")
    List<Payment> findLatestByVisitId(@Param("visitId") Long visitId, Pageable pageable);

    @Query("select p from Payment p where p.visit.visit_id in :visitIds")
    List<Payment> findByVisitIds(@Param("visitIds") List<Long> visitIds);
}
