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
public class AdminReportService {

    @PersistenceContext
    private EntityManager entityManager;

    public SalesReportData loadSalesReport(
            LocalDate startDate,
            LocalDate endDate,
            String departmentCode,
            String doctorId,
            String insuranceCode,
            String paymentMethodCode,
            String visitType) {
        LocalDate startBase = startDate != null ? startDate : LocalDate.now();
        LocalDate endBase = endDate != null ? endDate : startBase;
        LocalDateTime start = startBase.atStartOfDay();
        LocalDateTime end = endBase.plusDays(1).atStartOfDay().minusNanos(1);

        String dept = normalize(departmentCode);
        String doctor = normalize(doctorId);
        String insurance = normalize(insuranceCode);
        String paymentMethod = normalize(paymentMethodCode);
        String visit = normalize(visitType);

        Summary summary = querySummary(start, end, dept, doctor, insurance, paymentMethod, visit);
        List<DailySalesRow> dailyRows = queryDailySales(start, end, dept, doctor, insurance, paymentMethod, visit);
        List<PaymentMethodRow> paymentRows = queryPaymentMethods(start, end, dept, doctor, insurance, paymentMethod, visit);
        List<RankRow> rankRows = queryTopRanks(start, end, dept, doctor, insurance, paymentMethod, visit);

        List<Option> paymentMethodOptions = loadPaymentMethodOptions();
        return new SalesReportData(summary, dailyRows, paymentRows, rankRows, paymentMethodOptions);
    }

    public InsuranceSalesData loadInsuranceSales(
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

        List<InsuranceRow> rows = queryInsuranceSales(start, end, dept, doctor, insurance);
        return new InsuranceSalesData(rows);
    }

    private Summary querySummary(LocalDateTime start, LocalDateTime end, String dept, String doctor,
            String insurance, String paymentMethod, String visitType) {
        String sql = buildVisitSql("""
                select
                  coalesce(sum(c.total_amount),0) as total_amount,
                  coalesce(sum(c.discount_amount),0) as discount_amount,
                  coalesce(sum(p.amount),0) as paid_amount,
                  coalesce(sum(case when p.status_code = 'PAY_REFUND' then p.amount else 0 end),0) as refund_amount
                from visit v
                join claim c on c.visit_id = v.visit_id
                join payment p on p.visit_id = v.visit_id
                where v.visit_datetime between :start and :end
                  and p.status_code in ('PAY_COMPLETED','PAY_REFUND')
                """, dept, doctor, insurance, paymentMethod, visitType);
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, dept, doctor, insurance, paymentMethod, visitType);
        Object[] row = (Object[]) query.getSingleResult();
        long total = toLong(row[0]);
        long discount = toLong(row[1]);
        long paid = toLong(row[2]);
        long refund = toLong(row[3]);
        long net = total - refund;
        return new Summary(total, discount, paid, refund, net);
    }

    private List<DailySalesRow> queryDailySales(LocalDateTime start, LocalDateTime end, String dept,
            String doctor, String insurance, String paymentMethod, String visitType) {
        String sql = buildVisitSql("""
                select date(v.visit_datetime) as day,
                       coalesce(sum(c.total_amount),0) as total_amount,
                       coalesce(sum(c.discount_amount),0) as discount_amount,
                       coalesce(sum(p.amount),0) as paid_amount
                from visit v
                join claim c on c.visit_id = v.visit_id
                join payment p on p.visit_id = v.visit_id
                where v.visit_datetime between :start and :end
                  and p.status_code = 'PAY_COMPLETED'
                group by date(v.visit_datetime)
                order by day
                """, dept, doctor, insurance, paymentMethod, visitType);
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, dept, doctor, insurance, paymentMethod, visitType);
        List<Object[]> rows = query.getResultList();
        List<DailySalesRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            String day = row[0] != null ? row[0].toString() : "";
            long total = toLong(row[1]);
            long discount = toLong(row[2]);
            long paid = toLong(row[3]);
            results.add(new DailySalesRow(day, total, discount, paid));
        }
        return results;
    }

    private List<PaymentMethodRow> queryPaymentMethods(LocalDateTime start, LocalDateTime end, String dept,
            String doctor, String insurance, String paymentMethod, String visitType) {
        String sql = buildVisitSql("""
                select pm.name, coalesce(sum(p.amount),0) as amount
                from visit v
                join payment p on p.visit_id = v.visit_id
                left join payment_method pm on p.payment_method_code = pm.payment_method_code
                where v.visit_datetime between :start and :end
                  and p.status_code = 'PAY_COMPLETED'
                group by pm.name
                order by amount desc
                """, dept, doctor, insurance, paymentMethod, visitType);
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, dept, doctor, insurance, paymentMethod, visitType);
        List<Object[]> rows = query.getResultList();
        List<PaymentMethodRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            String name = row[0] != null ? row[0].toString() : "";
            long amount = toLong(row[1]);
            results.add(new PaymentMethodRow(name, amount));
        }
        return results;
    }

    private List<RankRow> queryTopRanks(LocalDateTime start, LocalDateTime end, String dept,
            String doctor, String insurance, String paymentMethod, String visitType) {
        String deptSql = buildVisitSql("""
                select d.name as name, coalesce(sum(c.total_amount),0) as amount
                from visit v
                join department d on v.department_code = d.department_code
                join claim c on c.visit_id = v.visit_id
                join payment p on p.visit_id = v.visit_id
                where v.visit_datetime between :start and :end
                  and p.status_code = 'PAY_COMPLETED'
                group by d.name
                order by amount desc
                limit 5
                """, dept, doctor, insurance, paymentMethod, visitType);
        Query deptQuery = entityManager.createNativeQuery(deptSql);
        applyFilters(deptQuery, start, end, dept, doctor, insurance, paymentMethod, visitType);
        List<Object[]> deptRows = deptQuery.getResultList();

        String docSql = buildVisitSql("""
                select u.name as name, coalesce(sum(c.total_amount),0) as amount
                from visit v
                join user_account u on v.user_id = u.user_id
                join claim c on c.visit_id = v.visit_id
                join payment p on p.visit_id = v.visit_id
                where v.visit_datetime between :start and :end
                  and p.status_code = 'PAY_COMPLETED'
                group by u.name
                order by amount desc
                limit 5
                """, dept, doctor, insurance, paymentMethod, visitType);
        Query docQuery = entityManager.createNativeQuery(docSql);
        applyFilters(docQuery, start, end, dept, doctor, insurance, paymentMethod, visitType);
        List<Object[]> docRows = docQuery.getResultList();

        List<RankRow> results = new ArrayList<>();
        for (Object[] row : deptRows) {
            String name = row[0] != null ? row[0].toString() : "";
            long amount = toLong(row[1]);
            results.add(new RankRow("진료과", name, amount));
        }
        for (Object[] row : docRows) {
            String name = row[0] != null ? row[0].toString() : "";
            long amount = toLong(row[1]);
            results.add(new RankRow("의사", name, amount));
        }
        return results;
    }

    private List<InsuranceRow> queryInsuranceSales(LocalDateTime start, LocalDateTime end, String dept,
            String doctor, String insurance) {
        String sql = buildVisitSql("""
                select coalesce(ic.name,'기타') as name, coalesce(sum(c.total_amount),0) as amount
                from visit v
                join claim c on c.visit_id = v.visit_id
                join payment p on p.visit_id = v.visit_id
                left join insurance_code ic on v.insurance_code = ic.insurance_code
                where v.visit_datetime between :start and :end
                  and p.status_code = 'PAY_COMPLETED'
                group by ic.name
                order by amount desc
                """, dept, doctor, insurance, null, null);
        Query query = entityManager.createNativeQuery(sql);
        applyFilters(query, start, end, dept, doctor, insurance, null, null);
        List<Object[]> rows = query.getResultList();
        List<InsuranceRow> results = new ArrayList<>();
        long max = 0;
        for (Object[] row : rows) {
            long amount = toLong(row[1]);
            if (amount > max) {
                max = amount;
            }
        }
        for (Object[] row : rows) {
            String name = row[0] != null ? row[0].toString() : "";
            long amount = toLong(row[1]);
            int percent = max == 0 ? 0 : BigDecimal.valueOf(amount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(max), 0, RoundingMode.HALF_UP)
                    .intValue();
            results.add(new InsuranceRow(name, amount, percent));
        }
        return results;
    }

    public List<Option> loadPaymentMethodOptions() {
        Query query = entityManager.createNativeQuery("""
                select payment_method_code, name
                from payment_method
                where is_active = 1
                order by name
                """);
        List<Object[]> rows = query.getResultList();
        List<Option> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new Option(row[0].toString(), row[1].toString()));
        }
        return results;
    }

    private String buildVisitSql(String sql, String departmentCode, String doctorId,
            String insuranceCode, String paymentMethod, String visitType) {
        StringBuilder sb = new StringBuilder();
        if (departmentCode != null) {
            sb.append(" and v.department_code = :departmentCode");
        }
        if (doctorId != null) {
            sb.append(" and v.user_id = :doctorId");
        }
        if (insuranceCode != null) {
            sb.append(" and v.insurance_code = :insuranceCode");
        }
        if (paymentMethod != null) {
            sb.append(" and p.payment_method_code = :paymentMethod");
        }
        if (visitType != null) {
            sb.append(" and v.visit_type = :visitType");
        }
        return sql + sb;
    }

    private void applyFilters(Query query, LocalDateTime start, LocalDateTime end, String dept,
            String doctor, String insurance, String paymentMethod, String visitType) {
        query.setParameter("start", start);
        query.setParameter("end", end);
        if (dept != null) {
            query.setParameter("departmentCode", dept);
        }
        if (doctor != null) {
            query.setParameter("doctorId", doctor);
        }
        if (insurance != null) {
            query.setParameter("insuranceCode", insurance);
        }
        if (paymentMethod != null) {
            query.setParameter("paymentMethod", paymentMethod);
        }
        if (visitType != null) {
            query.setParameter("visitType", visitType);
        }
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0;
        }
        return ((Number) value).longValue();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    public record SalesReportData(
            Summary summary,
            List<DailySalesRow> dailyRows,
            List<PaymentMethodRow> paymentMethods,
            List<RankRow> rankRows,
            List<Option> paymentMethodOptions) {
    }

    public record Summary(long totalAmount, long coveredAmount, long noncoveredAmount, long refundAmount,
            long netAmount) {
    }

    public record DailySalesRow(String day, long totalAmount, long coveredAmount, long noncoveredAmount) {
    }

    public record PaymentMethodRow(String paymentMethodName, long amount) {
    }

    public record RankRow(String type, String name, long amount) {
    }

    public record InsuranceSalesData(List<InsuranceRow> rows) {
    }

    public record InsuranceRow(String insuranceName, long amount, int percent) {
    }

    public record Option(String code, String name) {
    }
}
