package com.example.erp.Patient;

import org.springframework.stereotype.Service;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class PatientService {
	private final PatientRepository patientRepository;

    public Patient findById(Long patientId) {  // patient_id로 환자 조회 by 은서
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("환자를 찾을 수 없습니다. id=" + patientId));
    }

    public List<Patient> searchPatients(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return patientRepository.findByNameContainingIgnoreCaseOrPhoneContainingOrRrnContaining(
                keyword, keyword, keyword);
    }
}
