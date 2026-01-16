package com.example.erp.Admin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class AdminWorkService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<WorkStatusRow> loadWorkStatus(LocalDate workDate, String departmentCode, String workTypeCode) {
        StringBuilder sql = new StringBuilder("""
                select ws.work_date, u.name, d.name, wt.work_name, ws.status_code
                from work_schedule ws
                join user_account u on ws.user_id = u.user_id
                left join department d on ws.department_code = d.department_code
                left join work_type wt on ws.work_type_code = wt.work_type_code
                where 1=1
                """);
        if (workDate != null) {
            sql.append(" and ws.work_date = :workDate");
        }
        if (departmentCode != null && !departmentCode.isBlank()) {
            sql.append(" and ws.department_code = :departmentCode");
        }
        if (workTypeCode != null && !workTypeCode.isBlank()) {
            sql.append(" and ws.work_type_code = :workTypeCode");
        }
        sql.append(" order by ws.work_date desc");
        Query query = entityManager.createNativeQuery(sql.toString());
        if (workDate != null) {
            query.setParameter("workDate", workDate);
        }
        if (departmentCode != null && !departmentCode.isBlank()) {
            query.setParameter("departmentCode", departmentCode);
        }
        if (workTypeCode != null && !workTypeCode.isBlank()) {
            query.setParameter("workTypeCode", workTypeCode);
        }
        List<Object[]> rows = query.getResultList();
        List<WorkStatusRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new WorkStatusRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    row[4] != null ? row[4].toString() : ""));
        }
        return results;
    }

    public List<WorkTypeRow> loadWorkTypes() {
        Query query = entityManager.createNativeQuery("""
                select wt.work_type_code, wt.role_code, wt.work_name, wt.start_time, wt.end_time, wt.note
                from work_type wt
                order by wt.work_type_code
                """);
        List<Object[]> rows = query.getResultList();
        List<WorkTypeRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new WorkTypeRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    row[4] != null ? row[4].toString() : "",
                    row[5] != null ? row[5].toString() : ""));
        }
        return results;
    }

    public record WorkStatusRow(
            String workDate,
            String staffName,
            String departmentName,
            String workTypeName,
            String statusCode) {
    }

    public record WorkTypeRow(
            String workTypeCode,
            String roleCode,
            String workName,
            String startTime,
            String endTime,
            String note) {
    }
}
