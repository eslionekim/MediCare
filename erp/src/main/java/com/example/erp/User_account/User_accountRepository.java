package com.example.erp.User_account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface User_accountRepository extends JpaRepository<User_account, Long> {
}
