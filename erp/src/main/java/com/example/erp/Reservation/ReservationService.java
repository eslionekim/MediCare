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
import java.util.ArrayList;

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

    public List<CalendarDateDto> buildCalendar(LocalDate baseDate) {
        LocalDate first = baseDate.withDayOfMonth(1);
        int length = baseDate.lengthOfMonth();
        int startDow = first.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        int offset = startDow % 7; // Sunday=0, Monday=1 ...

        List<CalendarDateDto> dates = new ArrayList<>();

        // 이전 달 채우기
        LocalDate prevMonth = first.minusMonths(1);
        int prevLength = prevMonth.lengthOfMonth();
        for (int i = offset - 1; i >= 0; i--) {
            LocalDate d = prevMonth.withDayOfMonth(prevLength - i);
            dates.add(new CalendarDateDto(d.getDayOfMonth(), d.toString(), false, true));
        }

        // 이번 달
        for (int day = 1; day <= length; day++) {
            LocalDate d = first.withDayOfMonth(day);
            boolean selected = d.equals(baseDate);
            dates.add(new CalendarDateDto(day, d.toString(), selected, false));
        }

        // 다음 달로 채워서 주 단위 맞추기 (최대 6주, 42칸)
        LocalDate nextMonth = first.plusMonths(1);
        int nextDay = 1;
        while (dates.size() % 7 != 0 || dates.size() < 42) {
            LocalDate d = nextMonth.withDayOfMonth(nextDay++);
            dates.add(new CalendarDateDto(d.getDayOfMonth(), d.toString(), false, true));
            if (dates.size() >= 42 && dates.size() % 7 == 0) break;
        }

        return dates;
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
    public List<DailySlotDto> getDailySlots(LocalDate date, String departmentCode, String userId) {
        if (date == null) return Collections.emptyList();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Reservation> reservations = reservationRepository.findByStartTimeBetween(start, end);

        // 필터: 진료과, 의사
        if (departmentCode != null && !departmentCode.isBlank()) {
            reservations = reservations.stream()
                    .filter(r -> r.getDepartment() != null
                            && departmentCode.equalsIgnoreCase(r.getDepartment().getDepartment_code()))
                    .collect(Collectors.toList());
        }
        if (userId != null && !userId.isBlank()) {
            reservations = reservations.stream()
                    .filter(r -> r.getUser() != null
                            && userId.equalsIgnoreCase(r.getUser().getUser_id()))
                    .collect(Collectors.toList());
        }

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
        String statusCodeValueTemp = (dto.getStatusCode() == null || dto.getStatusCode().isBlank())
                ? "RES_PENDING"
                : dto.getStatusCode();
        // 과거 값 'RESERVED'로 들어오면 신규 코드로 매핑
        final String statusCodeValue = "RESERVED".equalsIgnoreCase(statusCodeValueTemp) ? "RES_PENDING" : statusCodeValueTemp;

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
