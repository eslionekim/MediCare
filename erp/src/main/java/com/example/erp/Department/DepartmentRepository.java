package com.example.erp.Department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    // 활성 진료과만 조회
    @org.springframework.data.jpa.repository.Query("select d from Department d where d.is_active = true")
    List<Department> findActive();
}
