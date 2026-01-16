package com.example.erp.Admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

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
}
