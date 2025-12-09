package com.example.erp.User_account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface User_accountRepository extends JpaRepository<User_account, String> {
	@Query("SELECT u FROM User_account u LEFT JOIN FETCH u.user_role ur LEFT JOIN FETCH ur.role_code rc WHERE u.user_id = :user_id") 
	Optional<User_account> findByUser_id(@Param("user_id") String user_id);
}
