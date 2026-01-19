package com.example.erp.Admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class AdminReceivableService {

    @PersistenceContext
    private EntityManager entityManager;

    public ReceivableData loadReceivables(
            LocalDate startDate,
            LocalDate endDate,
            String receivableStatus,
            String postpaid,
            String insuranceCode,
            String keyword) {
        LocalDate startBase = startDate != null ? startDate : LocalDate.now();
        LocalDate endBase = endDate != null ? endDate : startBase;
        LocalDateTime start = startBase.atStartOfDay();
        LocalDateTime end = endBase.plusDays(1).atStartOfDay().minusNanos(1);

        List<ReceivableRow> rows = queryReceivables(start, end, receivableStatus, insuranceCode, keyword);
        ReceivableRow selected = rows.isEmpty() ? null : rows.get(0);
        return new ReceivableData(rows, selected);
    }

    private List<ReceivableRow> queryReceivables(LocalDateTime start, LocalDateTime end,
            String receivableStatus, String insuranceCode, String keyword) {
        StringBuilder sql = new StringBuilder("""
                select p.name,
                       v.visit_id,
                       v.visit_datetime,
                       c.total_amount,
                       c.discount_amount,
                       coalesce(sum(case when pay.status_code = 'PAY_COMPLETED' then pay.amount else 0 end),0) as paid_amount,
                       v.user_id
                from visit v
                join patient p on v.patient_id = p.patient_id
                join claim c on c.visit_id = v.visit_id
                left join payment pay on pay.visit_id = v.visit_id
                where v.visit_datetime between :start and :end
                """);
        if (insuranceCode != null && !insuranceCode.isBlank()) {
            sql.append(" and v.insurance_code = :insuranceCode");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and (p.name like :keyword or cast(v.visit_id as char) like :keyword)");
        }
        sql.append(" group by p.name, v.visit_id, v.visit_datetime, c.total_amount, c.discount_amount, v.user_id");
        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("start", start);
        query.setParameter("end", end);
        if (insuranceCode != null && !insuranceCode.isBlank()) {
            query.setParameter("insuranceCode", insuranceCode);
        }
        if (keyword != null && !keyword.isBlank()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        List<Object[]> rows = query.getResultList();
        List<ReceivableRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            String patientName = row[0] != null ? row[0].toString() : "";
            Long visitId = row[1] == null ? null : ((Number) row[1]).longValue();
            String occurredAt = row[2] != null ? row[2].toString() : "";
            long totalAmount = row[3] == null ? 0 : ((Number) row[3]).longValue();
            long discountAmount = row[4] == null ? 0 : ((Number) row[4]).longValue();
            long paidAmount = row[5] == null ? 0 : ((Number) row[5]).longValue();
            long balance = (totalAmount - discountAmount) - paidAmount;
            String statusLabel = resolveStatus(balance, paidAmount);
            if (receivableStatus != null && !receivableStatus.isBlank()) {
                if ("OPEN".equals(receivableStatus) && balance <= 0) {
                    continue;
                }
                if ("PAID".equals(receivableStatus) && balance > 0) {
                    continue;
                }
                if ("PARTIAL".equals(receivableStatus) && (paidAmount == 0 || balance <= 0)) {
                    continue;
                }
            }
            String managerId = row[6] != null ? row[6].toString() : "";
            results.add(new ReceivableRow(
                    patientName,
                    visitId,
                    occurredAt,
                    totalAmount,
                    paidAmount,
                    balance,
                    statusLabel,
                    managerId,
                    ""));
        }
        return results;
    }

    private String resolveStatus(long balance, long paidAmount) {
        if (balance <= 0) {
            return "완납";
        }
        if (paidAmount > 0) {
            return "부분수납";
        }
        return "미수";
    }

    public record ReceivableData(List<ReceivableRow> rows, ReceivableRow selected) {
    }

    public record ReceivableRow(
            String patientName,
            Long visitId,
            String occurredAt,
            long totalAmount,
            long paidAmount,
            long balance,
            String status,
            String manager,
            String memo) {
    }

    @Transactional
    public void togglePostpaid(Long visitId) {
        Long pendingId = findLatestPaymentId(visitId, "PAY_PENDING");
        if (pendingId != null) {
            Query cancel = entityManager.createNativeQuery("""
                    update payment
                    set status_code = 'PAY_CANCELLED'
                    where payment_id = :paymentId
                    """);
            cancel.setParameter("paymentId", pendingId);
            cancel.executeUpdate();
            return;
        }
        long balance = calculateBalance(visitId);
        if (balance <= 0) {
            return;
        }
        String paymentMethod = findDefaultPaymentMethod();
        if (paymentMethod == null) {
            return;
        }
        insertPayment(visitId, paymentMethod, balance, "PAY_PENDING");
    }

    @Transactional
    public void markCollected(Long visitId) {
        long balance = calculateBalance(visitId);
        if (balance <= 0) {
            return;
        }
        String paymentMethod = findDefaultPaymentMethod();
        if (paymentMethod == null) {
            return;
        }
        insertPayment(visitId, paymentMethod, balance, "PAY_COMPLETED");

        Query cancelPending = entityManager.createNativeQuery("""
                update payment
                set status_code = 'PAY_CANCELLED'
                where visit_id = :visitId
                  and status_code = 'PAY_PENDING'
                """);
        cancelPending.setParameter("visitId", visitId);
        cancelPending.executeUpdate();
    }

    private long calculateBalance(Long visitId) {
        Query query = entityManager.createNativeQuery("""
                select c.total_amount,
                       c.discount_amount,
                       coalesce(sum(case when p.status_code = 'PAY_COMPLETED' then p.amount else 0 end),0)
                from claim c
                left join payment p on p.visit_id = c.visit_id
                where c.visit_id = :visitId
                group by c.total_amount, c.discount_amount
                """);
        query.setParameter("visitId", visitId);
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return 0;
        }
        Object[] row = rows.get(0);
        long total = row[0] == null ? 0 : ((Number) row[0]).longValue();
        long discount = row[1] == null ? 0 : ((Number) row[1]).longValue();
        long paid = row[2] == null ? 0 : ((Number) row[2]).longValue();
        return (total - discount) - paid;
    }

    private Long findLatestPaymentId(Long visitId, String statusCode) {
        Query query = entityManager.createNativeQuery("""
                select payment_id
                from payment
                where visit_id = :visitId
                  and status_code = :statusCode
                order by paid_at desc
                limit 1
                """);
        query.setParameter("visitId", visitId);
        query.setParameter("statusCode", statusCode);
        List<Object> rows = query.getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        return ((Number) rows.get(0)).longValue();
    }

    private String findDefaultPaymentMethod() {
        Query query = entityManager.createNativeQuery("""
                select payment_method_code
                from payment_method
                where is_active = 1
                order by payment_method_code
                limit 1
                """);
        List<Object> rows = query.getResultList();
        return rows.isEmpty() ? null : rows.get(0).toString();
    }

    private void insertPayment(Long visitId, String paymentMethod, long amount, String statusCode) {
        Query insert = entityManager.createNativeQuery("""
                insert into payment (visit_id, payment_method_code, amount, paid_at, status_code)
                values (:visitId, :paymentMethod, :amount, :paidAt, :statusCode)
                """);
        insert.setParameter("visitId", visitId);
        insert.setParameter("paymentMethod", paymentMethod);
        insert.setParameter("amount", amount);
        insert.setParameter("paidAt", LocalDateTime.now());
        insert.setParameter("statusCode", statusCode);
        insert.executeUpdate();
    }
}
