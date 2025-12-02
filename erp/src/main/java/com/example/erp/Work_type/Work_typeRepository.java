package com.example.erp.Work_type;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Work_typeRepository extends JpaRepository<Work_type, String> {
}
