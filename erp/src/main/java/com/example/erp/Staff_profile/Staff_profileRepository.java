package com.example.erp.Staff_profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    // 인사->직원 리스트->관리자 제외 by 은서
    @Query("""
            select sp
            from Staff_profile sp
            join fetch sp.user_account ua
            join fetch sp.department d
            where not exists (
                select 1
                from User_role ur
                where ur.user_account = ua
                and ur.role_code.role_code = 'ADMIN'
            )
        """)
        List<Staff_profile> findAllExceptAdmin();
        
    //마이페이지->user_id로 staff_profile불러오기
    @Query("SELECT s FROM Staff_profile s WHERE s.user_account.user_id = :userId")
    Optional<Staff_profile> findByUser_account_User_id(@Param("userId") String userId);
}
