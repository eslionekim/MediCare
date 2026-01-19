package com.example.erp.Admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class AdminCodeService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<CodeRow> loadDepartmentCodes(String keyword, String active) {
        Query query = entityManager.createNativeQuery("""
                select department_code, name, is_active
                from department
                where 1=1
                """ + buildKeywordActive(keyword, active, "department_code", "name") + """
                order by name
                """);
        applyKeywordActive(query, keyword, active);
        List<Object[]> rows = query.getResultList();
        List<CodeRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new CodeRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    toDepartmentActive(row[2])));
        }
        return results;
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
            String category = row[1] != null ? row[1].toString() : "";
            results.add(new FeeItemRow(
                    row[0] != null ? row[0].toString() : "",
                    category,
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    toCoverage(category),
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

    @Transactional
    public void createDepartment(String code, String name, String active) {
        Query query = entityManager.createNativeQuery("""
                insert into department (department_code, name, is_active)
                values (:code, :name, :active)
                """);
        query.setParameter("code", code);
        query.setParameter("name", name);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void updateDepartment(String code, String name, String active) {
        Query query = entityManager.createNativeQuery("""
                update department
                set name = :name,
                    is_active = :active
                where department_code = :code
                """);
        query.setParameter("code", code);
        query.setParameter("name", name);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void deactivateDepartment(String code) {
        deactivate("department", "department_code", code);
    }

    @Transactional
    public void createInsurance(String code, String name, String discountRate, String active) {
        Query query = entityManager.createNativeQuery("""
                insert into insurance_code (insurance_code, name, discount_rate, is_active)
                values (:code, :name, :discountRate, :active)
                """);
        query.setParameter("code", code);
        query.setParameter("name", name);
        query.setParameter("discountRate", discountRate);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void updateInsurance(String code, String name, String discountRate, String active) {
        Query query = entityManager.createNativeQuery("""
                update insurance_code
                set name = :name,
                    discount_rate = :discountRate,
                    is_active = :active
                where insurance_code = :code
                """);
        query.setParameter("code", code);
        query.setParameter("name", name);
        query.setParameter("discountRate", discountRate);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void deactivateInsurance(String code) {
        deactivate("insurance_code", "insurance_code", code);
    }

    @Transactional
    public void createPaymentMethod(String code, String name, String active) {
        Query query = entityManager.createNativeQuery("""
                insert into payment_method (payment_method_code, name, is_active)
                values (:code, :name, :active)
                """);
        query.setParameter("code", code);
        query.setParameter("name", name);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void updatePaymentMethod(String code, String name, String active) {
        Query query = entityManager.createNativeQuery("""
                update payment_method
                set name = :name,
                    is_active = :active
                where payment_method_code = :code
                """);
        query.setParameter("code", code);
        query.setParameter("name", name);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void deactivatePaymentMethod(String code) {
        deactivate("payment_method", "payment_method_code", code);
    }

    @Transactional
    public void createFeeItem(String code, String category, String name, String basePrice, String active) {
        Query query = entityManager.createNativeQuery("""
                insert into fee_item (fee_item_code, category, name, base_price, is_active)
                values (:code, :category, :name, :basePrice, :active)
                """);
        query.setParameter("code", code);
        query.setParameter("category", category);
        query.setParameter("name", name);
        query.setParameter("basePrice", basePrice);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void updateFeeItem(String code, String category, String name, String basePrice, String active) {
        Query query = entityManager.createNativeQuery("""
                update fee_item
                set category = :category,
                    name = :name,
                    base_price = :basePrice,
                    is_active = :active
                where fee_item_code = :code
                """);
        query.setParameter("code", code);
        query.setParameter("category", category);
        query.setParameter("name", name);
        query.setParameter("basePrice", basePrice);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void deactivateFeeItem(String code) {
        deactivate("fee_item", "fee_item_code", code);
    }

    @Transactional
    public void createDisease(String code, String nameKor, String nameEng, String department, String active) {
        Query query = entityManager.createNativeQuery("""
                insert into diseases_code (diseases_code, name_kor, name_eng, department, is_active)
                values (:code, :nameKor, :nameEng, :department, :active)
                """);
        query.setParameter("code", code);
        query.setParameter("nameKor", nameKor);
        query.setParameter("nameEng", nameEng);
        query.setParameter("department", department);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void updateDisease(String code, String nameKor, String nameEng, String department, String active) {
        Query query = entityManager.createNativeQuery("""
                update diseases_code
                set name_kor = :nameKor,
                    name_eng = :nameEng,
                    department = :department,
                    is_active = :active
                where diseases_code = :code
                """);
        query.setParameter("code", code);
        query.setParameter("nameKor", nameKor);
        query.setParameter("nameEng", nameEng);
        query.setParameter("department", department);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void deactivateDisease(String code) {
        deactivate("diseases_code", "diseases_code", code);
    }

    @Transactional
    public void createStatus(String code, String category, String name, String active) {
        Query query = entityManager.createNativeQuery("""
                insert into status_code (status_code, category, name, is_active)
                values (:code, :category, :name, :active)
                """);
        query.setParameter("code", code);
        query.setParameter("category", category);
        query.setParameter("name", name);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void updateStatus(String code, String category, String name, String active) {
        Query query = entityManager.createNativeQuery("""
                update status_code
                set category = :category,
                    name = :name,
                    is_active = :active
                where status_code = :code
                """);
        query.setParameter("code", code);
        query.setParameter("category", category);
        query.setParameter("name", name);
        query.setParameter("active", normalizeActive(active));
        query.executeUpdate();
    }

    @Transactional
    public void deactivateStatus(String code) {
        deactivate("status_code", "status_code", code);
    }

    private void deactivate(String table, String codeColumn, String code) {
        Query query = entityManager.createNativeQuery(String.format("""
                update %s
                set is_active = 0
                where %s = :code
                """, table, codeColumn));
        query.setParameter("code", code);
        query.executeUpdate();
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

    private int normalizeActive(String active) {
        if (active == null || active.isBlank()) {
            return 1;
        }
        return Integer.parseInt(active);
    }

    private String toActive(Object value) {
        return isTruthy(value) ? "활성" : "비활성";
    }

    private String toDepartmentActive(Object value) {
        return isTruthy(value) ? "진료 가능" : "진료 불가능";
    }

    private String toCoverage(Object categoryValue) {
        if (categoryValue == null) {
            return "";
        }
        String text = categoryValue.toString().trim();
        if (text.contains("비급여")) {
            return "비급여";
        }
        if (text.contains("급여")) {
            return "급여";
        }
        return text;
    }

    private boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        String text = value.toString().trim();
        return "1".equals(text) || "true".equalsIgnoreCase(text) || "y".equalsIgnoreCase(text);
    }

    public record CodeRow(String code, String name, String active) {
    }

    public record InsuranceRow(String code, String name, String discountRate, String active) {
    }

    public record PaymentMethodRow(String code, String name, String active) {
    }

    public record FeeItemRow(String code, String category, String name, String basePrice, String coverage,
            String active) {
    }

    public record DiseaseRow(String code, String nameKor, String nameEng, String department, String active) {
    }

    public record StatusRow(String code, String category, String name, String active) {
    }

    public record RoleRow(String code, String name, String active) {
    }
}