package com.example.erp.Payment_method;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Payment_methodRepository  extends JpaRepository<Payment_method, String>{

}
