package com.example.erp.Admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class AdminDashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    public DashboardData loadTodayDashboard() {
        return loadDashboard(LocalDate.now(), LocalDate.now(), null, null, null);
    }

    public DashboardData loadDashboard(
            LocalDate startDate,
            LocalDate endDate,
            String departmentCode,
            String doctorId,
            String insuranceCode) {
        LocalDate startBase = startDate != null ? startDate : LocalDate.now();
        LocalDate endBase = endDate != null ? endDate : startBase;
        LocalDateTime start = startBase.atStartOfDay();
        LocalDateTime end = endBase.plusDays(1).atStartOfDay().minusNanos(1);

        String dept = normalize(departmentCode);
        String doctor = normalize(doctorId);
        String insurance = normalize(insuranceCode);

        long outpatientCount = queryLong(buildVisitSql("""
                select count(*)
                from visit v
                where v.visit_datetime between :start and :end
                """, "v", dept, doctor, insurance), start, end, dept, doctor, insurance);

        long reservationCount = queryLong(buildReservationSql("""
                select count(*)
                from reservation r
                where r.start_time between :start and :end
                """, dept, doctor), start, end, dept, doctor, null);

        long visitWaiting = queryLong(buildVisitSql("""
                select count(*)
                from visit v
                where v.visit_datetime between :start and :end
                  and v.status_code = 'VIS_WAITING'
                """, "v", dept, doctor, insurance), start, end, dept, doctor, insurance);

        long visitCompleted = queryLong(buildVisitSql("""
                select count(*)
                from visit v
                where v.visit_datetime between :start and :end
                  and v.status_code in ('VIS_COMPLETED','VIS_CLAIMED')
                """, "v", dept, doctor, insurance), start, end, dept, doctor, insurance);

        long receptionCount = outpatientCount;
        long inpatientCount = 0;

        Number[] paidAndDiscount = queryPair(buildVisitSql("""
                select
                  coalesce(sum(c.total_amount),0) as paid_amount,
                  coalesce(sum(c.discount_amount),0) as discount_amount
                from claim c
                join visit v on c.visit_id = v.visit_id
                join payment p on p.visit_id = v.visit_id
                where v.visit_datetime between :start and :end
                  and p.status_code = 'PAY_COMPLETED'
                """, "v", dept, doctor, insurance), start, end, dept, doctor, insurance);

        long noncoveredAmount = paidAndDiscount[0].longValue();
        long coveredAmount = paidAndDiscount[1].longValue();
        long salesTotal = noncoveredAmount + coveredAmount;
        Ratio coveredRatio = toRatio(coveredAmount, noncoveredAmount);

        Number[] receivable = queryPair(buildVisitSql("""
                select
                  coalesce(count(*),0) as cnt,
                  coalesce(sum(c.total_amount - c.discount_amount),0) as amt
                from claim c
                join visit v on c.visit_id = v.visit_id
                left join payment p on p.visit_id = v.visit_id
                where v.visit_datetime between :start and :end
                  and p.payment_id is null
                """, "v", dept, doctor, insurance), start, end, dept, doctor, insurance);
        long receivableCount = receivable[0].longValue();
        long receivableAmount = receivable[1].longValue();

        Number[] claimStatus = queryPair(buildVisitSql("""
                select
                  coalesce(sum(case when c.is_confirmed = 0 then 1 else 0 end),0) as pending_cnt,
                  coalesce(sum(case when c.is_confirmed = 1 then 1 else 0 end),0) as done_cnt
                from claim c
                join visit v on c.visit_id = v.visit_id
                where v.visit_datetime between :start and :end
                """, "v", dept, doctor, insurance), start, end, dept, doctor, insurance);
        long claimPending = claimStatus[0].longValue();
        long claimDone = claimStatus[1].longValue();

        Number[] stockWarn = queryPairNoParams("""
                select
                  coalesce(sum(case when qty <= safety_stock * 0.5 then 1 else 0 end),0) as critical_cnt,
                  coalesce(sum(case when qty <= safety_stock and qty > safety_stock * 0.5 then 1 else 0 end),0) as warn_cnt
                from (
                    select i.item_code, i.safety_stock, coalesce(sum(s.quantity),0) as qty
                    from item i
                    left join stock s on s.item_code = i.item_code
                    group by i.item_code, i.safety_stock
                ) t
                """);
        long stockCritical = stockWarn[0].longValue();
        long stockWarning = stockWarn[1].longValue();

        List<DeptStat> deptStats = queryDeptStats(start, end, dept, doctor, insurance);
        List<DoctorStat> doctorStats = queryDoctorStats(start, end, dept, doctor, insurance);
        List<InsuranceStat> insuranceStats = queryInsuranceStats(start, end, dept, doctor, insurance);
        List<StockAlert> stockAlerts = queryStockAlerts();

        PipelineCounts pipeline = queryPipelineCounts(start, end, dept, doctor, insurance);

        Summary summary = new Summary(
                outpatientCount,
                inpatientCount,
                receptionCount,
                reservationCount,
                visitCompleted,
                visitWaiting,
                salesTotal,
                coveredRatio,
                receivableCount,
                receivableAmount,
                claimPending,
                claimDone,
                stockWarning,
                stockCritical);

        return new DashboardData(summary, deptStats, doctorStats, insuranceStats, stockAlerts, pipeline);
    }

    private long queryLong(String sql, LocalDateTime start, LocalDateTime end,
            String departmentCode, String doctorId, String insuranceCode) {
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, departmentCode, doctorId, insuranceCode);
        Object result = query.getSingleResult();
        return result == null ? 0 : ((Number) result).longValue();
    }

    private Number[] queryPair(String sql, LocalDateTime start, LocalDateTime end,
            String departmentCode, String doctorId, String insuranceCode) {
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, departmentCode, doctorId, insuranceCode);
        Object[] row = (Object[]) query.getSingleResult();
        Number first = row[0] == null ? 0 : (Number) row[0];
        Number second = row[1] == null ? 0 : (Number) row[1];
        return new Number[] { first, second };
    }

    private Number[] queryPairNoParams(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        Object[] row = (Object[]) query.getSingleResult();
        Number first = row[0] == null ? 0 : (Number) row[0];
        Number second = row[1] == null ? 0 : (Number) row[1];
        return new Number[] { first, second };
    }

    private List<DeptStat> queryDeptStats(LocalDateTime start, LocalDateTime end,
            String departmentCode, String doctorId, String insuranceCode) {
        String sql = buildVisitSql("""
                select d.name, count(*) as visit_count, count(distinct v.patient_id) as patient_count
                from visit v
                join department d on v.department_code = d.department_code
                where v.visit_datetime between :start and :end
                group by d.name
                order by visit_count desc
                """, "v", departmentCode, doctorId, insuranceCode);
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, departmentCode, doctorId, insuranceCode);
        List<Object[]> rows = query.getResultList();
        List<DeptStat> stats = new ArrayList<>();
        long max = 0;
        for (Object[] row : rows) {
            long count = ((Number) row[1]).longValue();
            if (count > max) {
                max = count;
            }
        }
        for (Object[] row : rows) {
            String name = row[0] != null ? row[0].toString() : "";
            long visitCount = row[1] == null ? 0 : ((Number) row[1]).longValue();
            long patientCount = row[2] == null ? 0 : ((Number) row[2]).longValue();
            int percent = max == 0 ? 0 : (int) Math.round((visitCount * 100.0) / max);
            stats.add(new DeptStat(name, visitCount, patientCount, percent));
        }
        return stats;
    }

    private List<DoctorStat> queryDoctorStats(LocalDateTime start, LocalDateTime end,
            String departmentCode, String doctorId, String insuranceCode) {
        String sql = buildVisitSql("""
                select u.name, d.name, count(*) as visit_count
                from visit v
                join user_account u on v.user_id = u.user_id
                join department d on v.department_code = d.department_code
                where v.visit_datetime between :start and :end
                group by u.name, d.name
                order by visit_count desc
                """, "v", departmentCode, doctorId, insuranceCode);
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, departmentCode, doctorId, insuranceCode);
        List<Object[]> rows = query.getResultList();
        List<DoctorStat> stats = new ArrayList<>();
        for (Object[] row : rows) {
            String name = row[0] != null ? row[0].toString() : "";
            String dept = row[1] != null ? row[1].toString() : "";
            long count = row[2] == null ? 0 : ((Number) row[2]).longValue();
            stats.add(new DoctorStat(name, dept, count));
        }
        return stats;
    }

    private List<InsuranceStat> queryInsuranceStats(LocalDateTime start, LocalDateTime end,
            String departmentCode, String doctorId, String insuranceCode) {
        String sql = buildVisitSql("""
                select coalesce(ic.name, '미등록') as name, coalesce(sum(ci.total),0) as amount
                from claim_item ci
                join claim c on ci.claim_id = c.claim_id
                join visit v on c.visit_id = v.visit_id
                join payment p on p.visit_id = v.visit_id
                left join insurance_code ic on v.insurance_code = ic.insurance_code
                where v.visit_datetime between :start and :end
                  and p.status_code = 'PAY_COMPLETED'
                group by ic.name
                order by amount desc
                """, "v", departmentCode, doctorId, insuranceCode);
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, departmentCode, doctorId, insuranceCode);
        List<Object[]> rows = query.getResultList();
        List<InsuranceStat> stats = new ArrayList<>();
        long max = 0;
        for (Object[] row : rows) {
            long amount = row[1] == null ? 0 : ((Number) row[1]).longValue();
            if (amount > max) {
                max = amount;
            }
        }
        for (Object[] row : rows) {
            String name = row[0] != null ? row[0].toString() : "";
            long amount = row[1] == null ? 0 : ((Number) row[1]).longValue();
            int percent = max == 0 ? 0 : (int) Math.round((amount * 100.0) / max);
            stats.add(new InsuranceStat(name, amount, percent));
        }
        return stats;
    }
private List<StockAlert> queryStockAlerts() {
        Query query = entityManager.createNativeQuery("""
                select i.name, coalesce(sum(s.quantity),0) as qty, i.safety_stock
                from item i
                left join stock s on s.item_code = i.item_code
                group by i.item_code, i.name, i.safety_stock
                order by (coalesce(sum(s.quantity),0) - i.safety_stock) asc
                limit 5
                """);
        List<Object[]> rows = query.getResultList();
        List<StockAlert> alerts = new ArrayList<>();
        for (Object[] row : rows) {
            String name = row[0] != null ? row[0].toString() : "";
            BigDecimal qty = row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
            BigDecimal safety = row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString());
            boolean critical = safety.compareTo(BigDecimal.ZERO) > 0
                    && qty.compareTo(safety.multiply(BigDecimal.valueOf(0.5))) <= 0;
            alerts.add(new StockAlert(name, qty, safety, critical));
        }
        return alerts;
    }

    private PipelineCounts queryPipelineCounts(LocalDateTime start, LocalDateTime end,
            String departmentCode, String doctorId, String insuranceCode) {
        String sql = buildVisitSql("""
                select
                  sum(case when status_code = 'VIS_REGISTERED' then 1 else 0 end) as registered_cnt,
                  sum(case when status_code = 'VIS_IN_PROGRESS' then 1 else 0 end) as in_progress_cnt,
                  sum(case when status_code = 'VIS_COMPLETED' then 1 else 0 end) as completed_cnt,
                  sum(case when status_code = 'VIS_WAITING' then 1 else 0 end) as waiting_cnt,
                  sum(case when status_code = 'VIS_CLAIMED' then 1 else 0 end) as claimed_cnt
                from visit v
                where v.visit_datetime between :start and :end
                """, "v", departmentCode, doctorId, insuranceCode);
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, departmentCode, doctorId, insuranceCode);
        Object[] row = (Object[]) query.getSingleResult();
        long registered = row[0] == null ? 0 : ((Number) row[0]).longValue();
        long inProgress = row[1] == null ? 0 : ((Number) row[1]).longValue();
        long completed = row[2] == null ? 0 : ((Number) row[2]).longValue();
        long waiting = row[3] == null ? 0 : ((Number) row[3]).longValue();
        long claimed = row[4] == null ? 0 : ((Number) row[4]).longValue();
        return new PipelineCounts(registered, inProgress, completed, waiting, claimed);
    }

    private Ratio toRatio(long covered, long noncovered) {
        long total = covered + noncovered;
        if (total <= 0) {
            return new Ratio(0, 0, 0, 0);
        }
        BigDecimal coveredPct = BigDecimal.valueOf(covered)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP);
        BigDecimal noncoveredPct = BigDecimal.valueOf(noncovered)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP);
        return new Ratio(covered, noncovered, coveredPct.intValue(), noncoveredPct.intValue());
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
        return appendWhere(sql, sb.toString());
    }

    private String buildReservationSql(String sql, String departmentCode, String doctorId) {
        StringBuilder sb = new StringBuilder();
        if (departmentCode != null) {
            sb.append(" and r.department_code = :departmentCode");
        }
        if (doctorId != null) {
            sb.append(" and r.user_id = :doctorId");
        }
        return appendWhere(sql, sb.toString());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private String appendWhere(String sql, String extraWhere) {
        if (extraWhere == null || extraWhere.isBlank()) {
            return sql;
        }
        String lower = sql.toLowerCase();
        int groupIdx = lower.indexOf("group by");
        int orderIdx = lower.indexOf("order by");
        int insertIdx = -1;
        if (groupIdx >= 0 && orderIdx >= 0) {
            insertIdx = Math.min(groupIdx, orderIdx);
        } else if (groupIdx >= 0) {
            insertIdx = groupIdx;
        } else if (orderIdx >= 0) {
            insertIdx = orderIdx;
        }
        if (insertIdx < 0) {
            return sql + ensureLeadingSpace(extraWhere);
        }
        String prefix = sql.substring(0, insertIdx);
        String suffix = sql.substring(insertIdx);
        if (!prefix.isEmpty() && !Character.isWhitespace(prefix.charAt(prefix.length() - 1))) {
            prefix += " ";
        }
        String paddedExtra = ensureLeadingSpace(extraWhere);
        if (!paddedExtra.isEmpty() && !Character.isWhitespace(paddedExtra.charAt(paddedExtra.length() - 1))) {
            paddedExtra += " ";
        }
        return prefix + paddedExtra + suffix;
    }

    private String ensureLeadingSpace(String value) {
        if (value.startsWith(" ")) {
            return value;
        }
        return " " + value;
    }

    public List<Option> getDepartmentOptions() {
        Query query = entityManager.createNativeQuery("""
                select department_code, name
                from department
                where is_active = 1
                order by name
                """);
        List<Object[]> rows = query.getResultList();
        List<Option> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new Option(row[0].toString(), row[1].toString()));
        }
        return result;
    }

    public List<DoctorOption> getDoctorOptions() {
        Query query = entityManager.createNativeQuery("""
                select u.user_id, u.name, sp.department_code, d.name
                from staff_profile sp
                join user_account u on sp.user_id = u.user_id
                left join department d on sp.department_code = d.department_code
                order by u.name
                """);
        List<Object[]> rows = query.getResultList();
        List<DoctorOption> result = new ArrayList<>();
        for (Object[] row : rows) {
            String deptCode = row[2] == null ? "" : row[2].toString();
            String deptName = row[3] == null ? "" : row[3].toString();
            result.add(new DoctorOption(row[0].toString(), row[1].toString(), deptCode, deptName));
        }
        return result;
    }

    public List<Option> getInsuranceOptions() {
        Query query = entityManager.createNativeQuery("""
                select insurance_code, name
                from insurance_code
                where is_active = 1
                order by name
                """);
        List<Object[]> rows = query.getResultList();
        List<Option> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new Option(row[0].toString(), row[1].toString()));
        }
        return result;
    }

    public record DashboardData(
            Summary summary,
            List<DeptStat> deptStats,
            List<DoctorStat> doctorStats,
            List<InsuranceStat> insuranceStats,
            List<StockAlert> stockAlerts,
            PipelineCounts pipelineCounts) {
    }

    public record Summary(
            long outpatientCount,
            long inpatientCount,
            long receptionCount,
            long reservationCount,
            long visitCompleted,
            long visitWaiting,
            long salesTotal,
            Ratio coveredRatio,
            long receivableCount,
            long receivableAmount,
            long claimPending,
            long claimDone,
            long stockWarning,
            long stockCritical) {
    }

    public record Ratio(long coveredAmount, long noncoveredAmount, int coveredPercent, int noncoveredPercent) {
    }

    public record DeptStat(String departmentName, long visitCount, long patientCount, int percent) {
    }

    public record DoctorStat(String doctorName, String departmentName, long visitCount) {
    }

    public record InsuranceStat(String insuranceName, long amount, int percent) {
    }

    public record StockAlert(String itemName, BigDecimal quantity, BigDecimal safetyStock, boolean critical) {
    }

    public record PipelineCounts(
            long registered,
            long inProgress,
            long completed,
            long waiting,
            long claimed) {
    }

    public record Option(String code, String name) {
    }

    public record DoctorOption(String userId, String name, String departmentCode, String departmentName) {
    }
}




