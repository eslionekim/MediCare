package com.example.erp.Department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    // 활성 진료과만 조회
    @org.springframework.data.jpa.repository.Query("select d from Department d where d.is_active = true")
    List<Department> findActive();
    
    // 인사->직원 등록-> 진료과 이름으로 진료과 찾기
    @Query("SELECT d FROM Department d WHERE d.name = :name")
    Optional<Department> findByName(@Param("name") String name);
}
