package com.example.erp.Admin;

import java.time.DayOfWeek;
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
public class AdminRevenueCycleService {

    @PersistenceContext
    private EntityManager entityManager;

    public MonitoringData loadMonitoring(
            String range,
            String departmentCode,
            String doctorId,
            String insuranceCode,
            String status) {
        LocalDate today = LocalDate.now();
        LocalDate startBase;
        if ("week".equalsIgnoreCase(range)) {
            startBase = today.with(DayOfWeek.MONDAY);
        } else if ("month".equalsIgnoreCase(range)) {
            startBase = today.withDayOfMonth(1);
        } else {
            startBase = today;
        }
        LocalDateTime start = startBase.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        String dept = normalize(departmentCode);
        String doctor = normalize(doctorId);
        String insurance = normalize(insuranceCode);
        String statusFilter = normalize(status);

        List<KanbanColumn> kanbanColumns = buildKanbanColumns(start, end, dept, doctor, insurance);
        List<VisitRow> visitRows = queryVisitRows(start, end, dept, doctor, insurance, statusFilter);
        VisitDetail selectedVisit = visitRows.isEmpty() ? null : VisitDetail.from(visitRows.get(0));
        return new MonitoringData(kanbanColumns, visitRows, selectedVisit);
    }

    private List<KanbanColumn> buildKanbanColumns(LocalDateTime start, LocalDateTime end,
            String departmentCode, String doctorId, String insuranceCode) {
        List<KanbanColumn> columns = new ArrayList<>();
        columns.add(buildColumn("RECEPTION_WAIT", "접수 대기", "v.status_code = 'VIS_REGISTERED'",
                start, end, departmentCode, doctorId, insuranceCode));
        columns.add(buildColumn("IN_TREATMENT", "진료중", "v.status_code = 'VIS_IN_PROGRESS'",
                start, end, departmentCode, doctorId, insuranceCode));
        columns.add(buildColumn("TREATMENT_DONE", "진료 완료",
                "v.status_code in ('VIS_COMPLETED','VIS_CLAIMED')",
                start, end, departmentCode, doctorId, insuranceCode));
        columns.add(buildColumn("PAY_WAIT", "수납 대기", "v.status_code = 'VIS_WAITING'",
                start, end, departmentCode, doctorId, insuranceCode));
        columns.add(buildColumn("PAY_DONE", "수납 완료", "pmt.status_code = 'PAY_COMPLETED'",
                start, end, departmentCode, doctorId, insuranceCode));
        columns.add(buildColumn("CLAIM_WAIT", "청구 대기", "c.is_confirmed = 0",
                start, end, departmentCode, doctorId, insuranceCode));
        columns.add(buildColumn("CLAIM_DONE", "청구 완료", "c.is_confirmed = 1",
                start, end, departmentCode, doctorId, insuranceCode));
        return columns;
    }

    private KanbanColumn buildColumn(String key, String title, String statusCondition,
            LocalDateTime start, LocalDateTime end, String departmentCode, String doctorId,
            String insuranceCode) {
        String baseSql = """
                select count(distinct v.visit_id)
                from visit v
                left join claim c on c.visit_id = v.visit_id
                left join payment pmt on pmt.payment_id = (
                    select p2.payment_id
                    from payment p2
                    where p2.visit_id = v.visit_id
                    order by p2.paid_at desc
                    limit 1
                )
                where v.visit_datetime between :start and :end
                """;
        String sql = buildVisitSql(baseSql, "v", departmentCode, doctorId, insuranceCode);
        sql += " and " + statusCondition;
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, departmentCode, doctorId, insuranceCode);
        long count = ((Number) query.getSingleResult()).longValue();

        List<KanbanItem> items = queryTopItems(statusCondition, start, end, departmentCode, doctorId, insuranceCode);
        return new KanbanColumn(key, title, count, items);
    }

    private List<KanbanItem> queryTopItems(String statusCondition, LocalDateTime start, LocalDateTime end,
            String departmentCode, String doctorId, String insuranceCode) {
        String baseSql = """
                select v.visit_id, p.name, d.name, v.visit_datetime
                from visit v
                join patient p on v.patient_id = p.patient_id
                left join department d on v.department_code = d.department_code
                left join claim c on c.visit_id = v.visit_id
                left join payment pmt on pmt.payment_id = (
                    select p2.payment_id
                    from payment p2
                    where p2.visit_id = v.visit_id
                    order by p2.paid_at desc
                    limit 1
                )
                where v.visit_datetime between :start and :end
                """;
        String sql = buildVisitSql(baseSql, "v", departmentCode, doctorId, insuranceCode);
        sql += " and " + statusCondition + " order by v.visit_datetime desc limit 3";
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, departmentCode, doctorId, insuranceCode);
        List<Object[]> rows = query.getResultList();
        List<KanbanItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            Long visitId = row[0] == null ? null : ((Number) row[0]).longValue();
            String patientName = row[1] == null ? "" : row[1].toString();
            String deptName = row[2] == null ? "" : row[2].toString();
            String visitTime = row[3] == null ? "" : row[3].toString();
            items.add(new KanbanItem(visitId, patientName, deptName, visitTime));
        }
        return items;
    }

    private List<VisitRow> queryVisitRows(LocalDateTime start, LocalDateTime end,
            String departmentCode, String doctorId, String insuranceCode, String status) {
        String baseSql = """
                select v.visit_id,
                       p.name,
                       d.name,
                       u.name,
                       ic.name,
                       sc.name,
                       v.visit_datetime,
                       pmt.status_code,
                       c.is_confirmed
                from visit v
                join patient p on v.patient_id = p.patient_id
                left join department d on v.department_code = d.department_code
                left join user_account u on v.user_id = u.user_id
                left join insurance_code ic on v.insurance_code = ic.insurance_code
                left join status_code sc on v.status_code = sc.status_code
                left join claim c on c.visit_id = v.visit_id
                left join payment pmt on pmt.payment_id = (
                    select p2.payment_id
                    from payment p2
                    where p2.visit_id = v.visit_id
                    order by p2.paid_at desc
                    limit 1
                )
                where v.visit_datetime between :start and :end
                """;
        String sql = buildVisitSql(baseSql, "v", departmentCode, doctorId, insuranceCode);
        String statusWhere = buildStatusFilter(status);
        if (statusWhere != null) {
            sql += " and " + statusWhere;
        }
        sql += " order by v.visit_datetime desc";
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, departmentCode, doctorId, insuranceCode);
        List<Object[]> rows = query.getResultList();
        List<VisitRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            Long visitId = row[0] == null ? null : ((Number) row[0]).longValue();
            String patientName = row[1] == null ? "" : row[1].toString();
            String deptName = row[2] == null ? "" : row[2].toString();
            String doctorName = row[3] == null ? "" : row[3].toString();
            String insuranceName = row[4] == null ? "" : row[4].toString();
            String statusName = row[5] == null ? "" : row[5].toString();
            String receptionTime = row[6] == null ? "" : row[6].toString();
            String paymentStatus = row[7] == null ? "" : row[7].toString();
            String claimStatus = row[8] == null ? "" : (row[8].toString().equals("1") ? "완료" : "대기");
            String payLabel = "PAY_COMPLETED".equals(paymentStatus) ? "완료" : "대기";
            results.add(new VisitRow(
                    visitId,
                    patientName,
                    deptName,
                    doctorName,
                    insuranceName,
                    statusName,
                    receptionTime,
                    payLabel,
                    claimStatus));
        }
        return results;
    }

    private String buildStatusFilter(String status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case "RECEPTION_WAIT" -> "v.status_code = 'VIS_REGISTERED'";
            case "IN_TREATMENT" -> "v.status_code = 'VIS_IN_PROGRESS'";
            case "TREATMENT_DONE" -> "v.status_code in ('VIS_COMPLETED','VIS_CLAIMED')";
            case "PAY_WAIT" -> "v.status_code = 'VIS_WAITING'";
            case "PAY_DONE" -> "pmt.status_code = 'PAY_COMPLETED'";
            case "CLAIM_WAIT" -> "c.is_confirmed = 0";
            case "CLAIM_DONE" -> "c.is_confirmed = 1";
            default -> null;
        };
    }

    private void applyFilters(Query query, LocalDateTime start, LocalDateTime end,
            String departmentCode, String doctorId, String insuranceCode) {
        var params = query.getParameters();
        if (hasParam(params, "start")) {
            query.setParameter("start", start);
        }
        if (hasParam(params, "end")) {
            query.setParameter("end", end);
        }
        if (departmentCode != null && hasParam(params, "departmentCode")) {
            query.setParameter("departmentCode", departmentCode);
        }
        if (doctorId != null && hasParam(params, "doctorId")) {
            query.setParameter("doctorId", doctorId);
        }
        if (insuranceCode != null && hasParam(params, "insuranceCode")) {
            query.setParameter("insuranceCode", insuranceCode);
        }
    }

    private boolean hasParam(java.util.Set<jakarta.persistence.Parameter<?>> params, String name) {
        for (jakarta.persistence.Parameter<?> param : params) {
            if (name.equals(param.getName())) {
                return true;
            }
        }
        return false;
    }

    private String buildVisitSql(String sql, String alias, String departmentCode, String doctorId,
            String insuranceCode) {
        StringBuilder sb = new StringBuilder();
        if (departmentCode != null) {
            sb.append(" and ").append(alias).append(".department_code = :departmentCode");
        }
        if (doctorId != null) {
            sb.append(" and ").append(alias).append(".user_id = :doctorId");
        }
        if (insuranceCode != null) {
            sb.append(" and ").append(alias).append(".insurance_code = :insuranceCode");
        }
        if (sb.length() == 0) {
            return sql;
        }
        return sql + sb;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    public record MonitoringData(
            List<KanbanColumn> kanbanColumns,
            List<VisitRow> visitRows,
            VisitDetail selectedVisit) {
    }

    public record KanbanColumn(String key, String title, long count, List<KanbanItem> items) {
    }

    public record KanbanItem(Long visitId, String patientName, String departmentName, String visitTime) {
    }

    public record VisitRow(
            Long visitId,
            String patientName,
            String departmentName,
            String doctorName,
            String insuranceName,
            String statusName,
            String receptionTime,
            String paymentStatus,
            String claimStatus) {
    }

    public record VisitDetail(
            Long visitId,
            String patientName,
            String departmentName,
            String doctorName,
            String insuranceName,
            String statusName,
            String receptionTime,
            String paymentStatus,
            String claimStatus) {

        public static VisitDetail from(VisitRow row) {
            return new VisitDetail(
                    row.visitId,
                    row.patientName,
                    row.departmentName,
                    row.doctorName,
                    row.insuranceName,
                    row.statusName,
                    row.receptionTime,
                    row.paymentStatus,
                    row.claimStatus);
        }
    }

    @Transactional
    public void requestStatusCorrection(Long visitId) {
        Query query = entityManager.createNativeQuery("""
                update visit
                set status_code = 'VIS_WAITING'
                where visit_id = :visitId
                """);
        query.setParameter("visitId", visitId);
        query.executeUpdate();
    }

    @Transactional
    public void requestPaymentClaimCorrection(Long visitId) {
        Query claimQuery = entityManager.createNativeQuery("""
                update claim
                set is_confirmed = 0
                where visit_id = :visitId
                """);
        claimQuery.setParameter("visitId", visitId);
        claimQuery.executeUpdate();

        Query paymentQuery = entityManager.createNativeQuery("""
                update payment
                set status_code = 'PAY_PENDING'
                where payment_id = (
                    select p2.payment_id
                    from payment p2
                    where p2.visit_id = :visitId
                    order by p2.paid_at desc
                    limit 1
                )
                """);
        paymentQuery.setParameter("visitId", visitId);
        paymentQuery.executeUpdate();
    }
}

