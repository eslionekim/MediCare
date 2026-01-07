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
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        long outpatientCount = queryLong("""
                select count(*)
                from visit
                where visit_datetime between :start and :end
                """, start, end);

        long reservationCount = queryLong("""
                select count(*)
                from reservation
                where start_time between :start and :end
                """, start, end);

        long visitWaiting = queryLong("""
                select count(*)
                from visit
                where visit_datetime between :start and :end
                  and status_code = 'VIS_WAITING'
                """, start, end);

        long visitCompleted = queryLong("""
                select count(*)
                from visit
                where visit_datetime between :start and :end
                  and status_code in ('VIS_COMPLETED','VIS_CLAIMED')
                """, start, end);

        long receptionCount = outpatientCount;
        long inpatientCount = 0;

        long salesTotal = queryLong("""
                select coalesce(sum(amount),0)
                from payment
                where paid_at between :start and :end
                """, start, end);

        Number[] coveredNoncovered = queryPair("""
                select
                  coalesce(sum(case when fi.is_active = 1 then ci.total else 0 end),0) as covered,
                  coalesce(sum(case when fi.is_active = 0 then ci.total else 0 end),0) as noncovered
                from claim_item ci
                join claim c on ci.claim_id = c.claim_id
                join visit v on c.visit_id = v.visit_id
                join fee_item fi on ci.fee_item_code = fi.fee_item_code
                where v.visit_datetime between :start and :end
                """, start, end);

        long coveredAmount = coveredNoncovered[0].longValue();
        long noncoveredAmount = coveredNoncovered[1].longValue();
        Ratio coveredRatio = toRatio(coveredAmount, noncoveredAmount);

        Number[] receivable = queryPair("""
                select
                  coalesce(count(*),0) as cnt,
                  coalesce(sum(c.total_amount - c.discount_amount),0) as amt
                from claim c
                join visit v on c.visit_id = v.visit_id
                left join payment p on p.visit_id = v.visit_id
                where v.visit_datetime between :start and :end
                  and p.payment_id is null
                """, start, end);
        long receivableCount = receivable[0].longValue();
        long receivableAmount = receivable[1].longValue();

        Number[] claimStatus = queryPair("""
                select
                  coalesce(sum(case when c.is_confirmed = 0 then 1 else 0 end),0) as pending_cnt,
                  coalesce(sum(case when c.is_confirmed = 1 then 1 else 0 end),0) as done_cnt
                from claim c
                join visit v on c.visit_id = v.visit_id
                where v.visit_datetime between :start and :end
                """, start, end);
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

        List<DeptStat> deptStats = queryDeptStats(start, end);
        List<DoctorStat> doctorStats = queryDoctorStats(start, end);
        List<InsuranceStat> insuranceStats = queryInsuranceStats(start, end);
        List<StockAlert> stockAlerts = queryStockAlerts();

        PipelineCounts pipeline = queryPipelineCounts(start, end);

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

    private long queryLong(String sql, LocalDateTime start, LocalDateTime end) {
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        Object result = query.getSingleResult();
        return result == null ? 0 : ((Number) result).longValue();
    }

    private Number[] queryPair(String sql, LocalDateTime start, LocalDateTime end) {
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
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

    private List<DeptStat> queryDeptStats(LocalDateTime start, LocalDateTime end) {
        Query query = entityManager.createNativeQuery("""
                select d.name, count(*) as visit_count, count(distinct v.patient_id) as patient_count
                from visit v
                join department d on v.department_code = d.department_code
                where v.visit_datetime between :start and :end
                group by d.name
                order by visit_count desc
                """);
        query.setParameter("start", start);
        query.setParameter("end", end);
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

    private List<DoctorStat> queryDoctorStats(LocalDateTime start, LocalDateTime end) {
        Query query = entityManager.createNativeQuery("""
                select u.name, d.name, count(*) as visit_count
                from visit v
                join user_account u on v.user_id = u.user_id
                join department d on v.department_code = d.department_code
                where v.visit_datetime between :start and :end
                group by u.name, d.name
                order by visit_count desc
                """);
        query.setParameter("start", start);
        query.setParameter("end", end);
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

    private List<InsuranceStat> queryInsuranceStats(LocalDateTime start, LocalDateTime end) {
        Query query = entityManager.createNativeQuery("""
                select coalesce(ic.name, '미지정') as name, coalesce(sum(ci.total),0) as amount
                from claim_item ci
                join claim c on ci.claim_id = c.claim_id
                join visit v on c.visit_id = v.visit_id
                left join insurance_code ic on v.insurance_code = ic.insurance_code
                where v.visit_datetime between :start and :end
                group by ic.name
                order by amount desc
                """);
        query.setParameter("start", start);
        query.setParameter("end", end);
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

    private PipelineCounts queryPipelineCounts(LocalDateTime start, LocalDateTime end) {
        Query query = entityManager.createNativeQuery("""
                select
                  sum(case when status_code = 'VIS_REGISTERED' then 1 else 0 end) as registered_cnt,
                  sum(case when status_code = 'VIS_IN_PROGRESS' then 1 else 0 end) as in_progress_cnt,
                  sum(case when status_code = 'VIS_COMPLETED' then 1 else 0 end) as completed_cnt,
                  sum(case when status_code = 'VIS_WAITING' then 1 else 0 end) as waiting_cnt,
                  sum(case when status_code = 'VIS_CLAIMED' then 1 else 0 end) as claimed_cnt
                from visit
                where visit_datetime between :start and :end
                """);
        query.setParameter("start", start);
        query.setParameter("end", end);
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
}
