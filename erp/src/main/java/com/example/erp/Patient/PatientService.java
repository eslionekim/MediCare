package com.example.erp.Patient;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class PatientService {
	private final PatientRepository patientRepository;

    public Patient findById(Long patientId) {  // patient_id로 환자 조회
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("환자를 찾을 수 없습니다. id=" + patientId));
    }
}
