package com.example.erp.Work_type;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Work_typeRepository extends JpaRepository<Work_type, String> {
	@Query("""
	        select wt 
	        from Work_type wt
	        join wt.role_code rc
	        join rc.user_role ur
	        join ur.user_account ua
	        where ua.user_id = :user_id
	    """)
	    List<Work_type> findByUserRole(@Param("user_id") String user_id);
}
