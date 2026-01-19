package com.example.erp.Admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class AdminCodeService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<CodeRow> loadDepartmentCodes(String keyword, String active) {
        return querySimpleCodes("department", "department_code", "name", keyword, active);
    }

    public List<InsuranceRow> loadInsuranceCodes(String keyword, String active) {
        Query query = entityManager.createNativeQuery("""
                select insurance_code, name, discount_rate, is_active
                from insurance_code
                where 1=1
                """ + buildKeywordActive(keyword, active, "insurance_code", "name") + """
                order by name
                """);
        applyKeywordActive(query, keyword, active);
        List<Object[]> rows = query.getResultList();
        List<InsuranceRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new InsuranceRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    toActive(row[3])));
        }
        return results;
    }

    public List<PaymentMethodRow> loadPaymentMethods(String keyword, String active) {
        Query query = entityManager.createNativeQuery("""
                select payment_method_code, name, is_active
                from payment_method
                where 1=1
                """ + buildKeywordActive(keyword, active, "payment_method_code", "name") + """
                order by name
                """);
        applyKeywordActive(query, keyword, active);
        List<Object[]> rows = query.getResultList();
        List<PaymentMethodRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new PaymentMethodRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    toActive(row[2])));
        }
        return results;
    }

    public List<FeeItemRow> loadFeeItems(String keyword, String active) {
        Query query = entityManager.createNativeQuery("""
                select fee_item_code, category, name, base_price, is_active
                from fee_item
                where 1=1
                """ + buildKeywordActive(keyword, active, "fee_item_code", "name") + """
                order by name
                """);
        applyKeywordActive(query, keyword, active);
        List<Object[]> rows = query.getResultList();
        List<FeeItemRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new FeeItemRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    toActive(row[4])));
        }
        return results;
    }

    public List<DiseaseRow> loadDiseases(String keyword, String active) {
        Query query = entityManager.createNativeQuery("""
                select diseases_code, name_kor, name_eng, department, is_active
                from diseases_code
                where 1=1
                """ + buildKeywordActive(keyword, active, "diseases_code", "name_kor") + """
                order by name_kor
                """);
        applyKeywordActive(query, keyword, active);
        List<Object[]> rows = query.getResultList();
        List<DiseaseRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new DiseaseRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    toActive(row[4])));
        }
        return results;
    }

    public List<StatusRow> loadStatusCodes(String keyword, String active) {
        Query query = entityManager.createNativeQuery("""
                select status_code, category, name, is_active
                from status_code
                where 1=1
                """ + buildKeywordActive(keyword, active, "status_code", "name") + """
                order by category, status_code
                """);
        applyKeywordActive(query, keyword, active);
        List<Object[]> rows = query.getResultList();
        List<StatusRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new StatusRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    toActive(row[3])));
        }
        return results;
    }

    public List<RoleRow> loadRoles() {
        Query query = entityManager.createNativeQuery("""
                select role_code, name, is_active
                from role_code
                order by role_code
                """);
        List<Object[]> rows = query.getResultList();
        List<RoleRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new RoleRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    toActive(row[2])));
        }
        return results;
    }

    private List<CodeRow> querySimpleCodes(String table, String codeColumn, String nameColumn,
            String keyword, String active) {
        String sql = """
                select %s, %s, is_active
                from %s
                where 1=1
                %s
                order by %s
                """.formatted(codeColumn, nameColumn, table,
                buildKeywordActive(keyword, active, codeColumn, nameColumn), nameColumn);
        Query query = entityManager.createNativeQuery(sql);
        applyKeywordActive(query, keyword, active);
        List<Object[]> rows = query.getResultList();
        List<CodeRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new CodeRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    toActive(row[2])));
        }
        return results;
    }

    private String buildKeywordActive(String keyword, String active, String codeColumn, String nameColumn) {
        StringBuilder sb = new StringBuilder();
        if (keyword != null && !keyword.isBlank()) {
            sb.append(" and (").append(codeColumn).append(" like :keyword or ")
                    .append(nameColumn).append(" like :keyword)");
        }
        if (active != null && !active.isBlank()) {
            sb.append(" and is_active = :active");
        }
        return sb.toString();
    }

    private void applyKeywordActive(Query query, String keyword, String active) {
        if (keyword != null && !keyword.isBlank()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (active != null && !active.isBlank()) {
            query.setParameter("active", Integer.parseInt(active));
        }
    }

    private String toActive(Object value) {
        if (value == null) {
            return "비활성";
        }
        if (value instanceof Number number) {
            return number.intValue() != 0 ? "활성" : "비활성";
        }
        String text = value.toString().trim();
        if ("1".equals(text) || "true".equalsIgnoreCase(text) || "y".equalsIgnoreCase(text)) {
            return "활성";
        }
        return "비활성";
    }

    public record CodeRow(String code, String name, String active) {
    }

    public record InsuranceRow(String code, String name, String discountRate, String active) {
    }

    public record PaymentMethodRow(String code, String name, String active) {
    }

    public record FeeItemRow(String code, String category, String name, String basePrice, String active) {
    }

    public record DiseaseRow(String code, String nameKor, String nameEng, String department, String active) {
    }

    public record StatusRow(String code, String category, String name, String active) {
    }

    public record RoleRow(String code, String name, String active) {
    }
}

