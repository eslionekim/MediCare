package com.example.erp.Patient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByNameContainingIgnoreCaseOrPhoneContainingOrRrnContaining(String name, String phone, String rrn);

    Page<Patient> findByNameContainingIgnoreCaseOrPhoneContainingOrRrnContaining(
            String name, String phone, String rrn, Pageable pageable);
}
