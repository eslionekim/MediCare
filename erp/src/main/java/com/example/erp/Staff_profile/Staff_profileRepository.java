package com.example.erp.Staff_profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Staff_profileRepository extends JpaRepository<Staff_profile, Long> {

    @Query("select sp from Staff_profile sp join fetch sp.user_account ua join fetch sp.department d")
    List<Staff_profile> findAllWithUserAndDepartment();

    @Query("select sp from Staff_profile sp join fetch sp.user_account ua join fetch sp.department d where d.department_code = :departmentCode")
    List<Staff_profile> findByDepartmentCode(@Param("departmentCode") String departmentCode);

    
    // 의사- > 전체 스케줄 조회 -> 검색창 -> 진료과로 의사이름 by 은서
    @Query("""
    	    SELECT u.name
    	    FROM Staff_profile sp
    	    JOIN sp.user_account u
    	    JOIN sp.department d
    	    WHERE d.name = :deptName
    	""")
    	List<String> findDoctorNamesByDepartmentName(@Param("deptName") String deptName);

}
