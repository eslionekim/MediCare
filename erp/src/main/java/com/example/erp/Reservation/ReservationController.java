package com.example.erp.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erp.Patient.Patient;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 예약 화면
     */
    @GetMapping
    public String reservationPage(
            @RequestParam(value = "patientId", required = false) Long patientId,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "departmentCode", required = false) String departmentCode,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "edit", required = false) Long editReservationId,
            @RequestParam(value = "time", required = false) String selectedTime,
            Model model) {

        // 1) 기준 날짜
        LocalDate baseDate = (dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr)
                : LocalDate.now();

        // 2) 환자 정보
        Patient selectedPatient = reservationService.findPatient(patientId);

        // 3) 수정 모드: 예약 정보로 환자/날짜/필터/시간을 고정
        Reservation selectedReservation = null;
        if (editReservationId != null) {
            selectedReservation = reservationService
                    .getReservationRepository()
                    .findById(editReservationId)
                    .orElse(null);
            if (selectedReservation != null) {
                if (selectedReservation.getStart_time() != null) {
                    baseDate = selectedReservation.getStart_time().toLocalDate();
                    if (selectedTime == null) {
                        selectedTime = selectedReservation.getStart_time().toLocalTime().toString();
                    }
                }
                if (selectedReservation.getPatient() != null) {
                    selectedPatient = selectedReservation.getPatient();
                }
                if ((departmentCode == null || departmentCode.isBlank()) && selectedReservation.getDepartment() != null) {
                    departmentCode = selectedReservation.getDepartment().getDepartment_code();
                }
                if ((userId == null || userId.isBlank()) && selectedReservation.getUser() != null) {
                    userId = selectedReservation.getUser().getUser_id();
                }
            }
        }

        // 4) 달력 데이터
        model.addAttribute("selectedDate", baseDate.toString());
        model.addAttribute("calendarTitle", reservationService.getCalendarTitle(baseDate));
        model.addAttribute("prevMonth", reservationService.getPrevMonth(baseDate));
        model.addAttribute("nextMonth", reservationService.getNextMonth(baseDate));
        model.addAttribute("dates", reservationService.buildCalendar(baseDate));

        // 5) 진료과 / 의사 리스트
        model.addAttribute("departments", reservationService.getDepartmentCodes());
        model.addAttribute("doctors", reservationService.getDoctors());

        // 6) 해당 날짜 타임테이블 (필터 반영)
        model.addAttribute("dailySlots", reservationService.getDailySlots(baseDate, departmentCode, userId));
        model.addAttribute("selectedDepartmentCode", departmentCode);
        model.addAttribute("selectedUserId", userId);

        // 7) 시간 슬롯 (1시간 간격)
        List<String> hourSlots = IntStream.range(0, 24)
                .mapToObj(h -> String.format("%02d:00", h))
                .collect(Collectors.toList());
        model.addAttribute("hourSlots", hourSlots);

        // 8) 선택 환자/예약/시간
        model.addAttribute("selectedPatient", selectedPatient);
        model.addAttribute("selectedReservation", selectedReservation);
        model.addAttribute("selectedTime", selectedTime);

        return "staff/reservation";
    }

    /**
     * 예약 저장 (신규/수정 공통)
     */
    @PostMapping("/save")
    public String saveReservation(ReservationRequestDto dto) {

        if (dto.getStatusCode() == null || dto.getStatusCode().isBlank()) {
            dto.setStatusCode("RES_PENDING");
        }

        reservationService.saveReservation(dto);

        Long patientId = dto.getPatientId();
        String date = dto.getDate();

        return "redirect:/reservations?patientId=" + patientId + "&date=" + date;
    }
}
