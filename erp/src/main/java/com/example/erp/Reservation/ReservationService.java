package com.example.erp.Reservation;

import com.example.erp.Department.Department;
import com.example.erp.Department.DepartmentRepository;
import com.example.erp.Patient.Patient;
import com.example.erp.Patient.PatientRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final User_accountRepository userAccountRepository;
    private final Status_codeRepository statusCodeRepository;

    @Transactional(readOnly = true)
    public Patient findPatient(Long patientId) {
        if (patientId == null) return null;
        return patientRepository.findById(patientId).orElse(null);
    }

    public String getCalendarTitle(LocalDate baseDate) {
        return baseDate.getYear() + "-" + String.format("%02d", baseDate.getMonthValue());
    }

    public String getPrevMonth(LocalDate baseDate) {
        return baseDate.minusMonths(1).toString();
    }

    public String getNextMonth(LocalDate baseDate) {
        return baseDate.plusMonths(1).toString();
    }

    public List<LocalDate> buildCalendar(LocalDate baseDate) {
        int length = baseDate.lengthOfMonth();
        return IntStream.rangeClosed(1, length)
                .mapToObj(baseDate::withDayOfMonth)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Department> getDepartmentCodes() {
        try {
            // 활성 진료과 우선
            return departmentRepository.findActive();
        } catch (Exception e) {
            return departmentRepository.findAll();
        }
    }

    @Transactional(readOnly = true)
    public List<User_account> getDoctors() {
        return userAccountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DailySlotDto> getDailySlots(LocalDate date) {
        if (date == null) return Collections.emptyList();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Reservation> reservations = reservationRepository.findByStartTimeBetween(start, end);
        return reservations.stream().map(r -> {
            DailySlotDto dto = new DailySlotDto();
            dto.setTime(r.getStart_time() != null ? r.getStart_time().toLocalTime().toString() : null);
            dto.setReservationId(r.getReservation_id());
            dto.setPatientName(r.getPatient() != null ? r.getPatient().getName() : "");
            dto.setDepartmentName(r.getDepartment() != null ? r.getDepartment().getName() : "");
            dto.setDoctorName(r.getUser() != null ? r.getUser().getName() : "");
            dto.setStatusName(r.getStatus_code() != null ? r.getStatus_code().getName() : "");
            dto.setReservable(false);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Reservation saveReservation(ReservationRequestDto dto) {
        Reservation reservation = new Reservation();
        if (dto.getReservationId() != null) {
            reservation = reservationRepository.findById(dto.getReservationId()).orElse(new Reservation());
        }

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("환자를 찾을 수 없습니다: " + dto.getPatientId()));
        Department department = departmentRepository.findById(dto.getDepartmentCode())
                .orElseThrow(() -> new IllegalArgumentException("진료과를 찾을 수 없습니다: " + dto.getDepartmentCode()));
        User_account doctor = userAccountRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("의사를 찾을 수 없습니다: " + dto.getUserId()));
        // 예약 기본 상태코드: 전달값이 없거나 미등록이면 RES_PENDING 사용
        String statusCodeValue = (dto.getStatusCode() == null || dto.getStatusCode().isBlank())
                ? "RES_PENDING"
                : dto.getStatusCode();

        Status_code status = statusCodeRepository.findById(statusCodeValue)
                .orElseThrow(() -> new IllegalArgumentException("상태코드를 찾을 수 없습니다: " + statusCodeValue));

        reservation.setPatient(patient);
        reservation.setDepartment(department);
        reservation.setUser(doctor);
        reservation.setStatus_code(status);

        LocalDate date = LocalDate.parse(dto.getDate());
        LocalTime start = LocalTime.parse(dto.getStartTime());
        reservation.setStart_time(LocalDateTime.of(date, start));
        if (dto.getEndTime() != null && !dto.getEndTime().isBlank()) {
            LocalTime end = LocalTime.parse(dto.getEndTime());
            reservation.setEnd_time(LocalDateTime.of(date, end));
        } else {
            reservation.setEnd_time(null);
        }

        reservation.setNote(dto.getNote());
        return reservationRepository.save(reservation);
    }

    public ReservationRepository getReservationRepository() {
        return reservationRepository;
    }
}
