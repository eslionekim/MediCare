package com.example.erp.Vacation_type;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Vacation_typeRepository extends JpaRepository<Vacation_type, String> {

}