package com.example.erp.Admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class AdminUserService {

    @PersistenceContext
    private EntityManager entityManager;

    public UserData loadUsers(String keyword, String department, String role, String status) {
        StringBuilder sql = new StringBuilder("""
                select u.user_id,
                       u.name,
                       d.name as department_name,
                       sp.position,
                       u.is_active,
                       u.created_at,
                       group_concat(rc.name separator ', ') as roles
                from user_account u
                left join staff_profile sp on u.user_id = sp.user_id
                left join department d on sp.department_code = d.department_code
                left join user_role ur on u.user_id = ur.user_id
                left join role_code rc on ur.role_code = rc.role_code
                where 1=1
                """);
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and (u.user_id like :keyword or u.name like :keyword)");
        }
        if (department != null && !department.isBlank()) {
            sql.append(" and sp.department_code = :department");
        }
        if (role != null && !role.isBlank()) {
            sql.append(" and ur.role_code = :role");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" and u.is_active = :active");
        }
        sql.append(" group by u.user_id, u.name, d.name, sp.position, u.is_active, u.created_at");
        sql.append(" order by u.name");
        Query query = entityManager.createNativeQuery(sql.toString());
        if (keyword != null && !keyword.isBlank()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (department != null && !department.isBlank()) {
            query.setParameter("department", department);
        }
        if (role != null && !role.isBlank()) {
            query.setParameter("role", role);
        }
        if (status != null && !status.isBlank()) {
            query.setParameter("active", "ACTIVE".equals(status) ? 1 : 0);
        }
        List<Object[]> rows = query.getResultList();
        List<UserRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            String userId = row[0] != null ? row[0].toString() : "";
            String name = row[1] != null ? row[1].toString() : "";
            String deptName = row[2] != null ? row[2].toString() : "";
            String position = row[3] != null ? row[3].toString() : "";
            String isActive = row[4] != null && row[4].toString().equals("1") ? "활성" : "비활성";
            String lastLogin = row[5] != null ? row[5].toString() : "";
            String roles = row[6] != null ? row[6].toString() : "";
            results.add(new UserRow(name, deptName, position, isActive, lastLogin, roles, userId));
        }
        return new UserData(results);
    }

    public List<Option> loadRoleOptions() {
        Query query = entityManager.createNativeQuery("""
                select role_code, name
                from role_code
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

    public record UserData(List<UserRow> rows) {
    }

    public record UserRow(
            String name,
            String departmentName,
            String position,
            String status,
            String lastLogin,
            String roles,
            String userId) {
    }

    public record Option(String code, String name) {
    }
}
