package com.example.erp.Dispense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DispenseRepository extends JpaRepository<Dispense, Long>{

}
