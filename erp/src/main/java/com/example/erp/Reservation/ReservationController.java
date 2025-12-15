// src/main/java/com/example/erp/Reservation/ReservationController.java
package com.example.erp.Reservation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.erp.Patient.Patient;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 예약 화면
     * - patientId : 환자 ID (환자 등록/조회 화면에서 넘어옴)
     * - date : 선택한 날짜 (yyyy-MM-dd), 없으면 오늘
     * - edit : 수정할 예약ID (null이면 신규)
     * - time : 슬롯에서 선택한 시간 (HH:mm) - 신규 예약 시 사용
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
        // 1) 선택 날짜
        LocalDate baseDate;
        if (dateStr != null && !dateStr.isBlank()) {
            baseDate = LocalDate.parse(dateStr);
        } else {
            baseDate = LocalDate.now();
        }

        // 2) 환자 정보
        Patient selectedPatient = reservationService.findPatient(patientId);

        // 3) 달력 관련 데이터
        model.addAttribute("selectedDate", baseDate.toString());
        model.addAttribute("calendarTitle", reservationService.getCalendarTitle(baseDate));
        model.addAttribute("prevMonth", reservationService.getPrevMonth(baseDate));
        model.addAttribute("nextMonth", reservationService.getNextMonth(baseDate));
        model.addAttribute("dates", reservationService.buildCalendar(baseDate));

        // 4) 진료과 / 의사 리스트
        model.addAttribute("departments", reservationService.getDepartmentCodes());
        model.addAttribute("doctors", reservationService.getDoctors());

        // 5) 해당 날짜 타임테이블
        List<DailySlotDto> dailySlots = reservationService.getDailySlots(baseDate);
        model.addAttribute("dailySlots", dailySlots);

        // 6) 선택된 예약(수정 모드)
        Reservation selectedReservation = null;
        if (editReservationId != null) {
            selectedReservation = reservationService
                    .getReservationRepository()
                    .findById(editReservationId)
                    .orElse(null);
        }

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

        // statusCode 값이 비어 있으면 기본값 설정 (예: "RESERVED")
        if (dto.getStatusCode() == null || dto.getStatusCode().isBlank()) {
            dto.setStatusCode("RES_PENDING"); // 네가 만든 상태코드 값으로 변경
        }

        Reservation saved = reservationService.saveReservation(dto);

        Long patientId = dto.getPatientId();
        String date = dto.getDate();

        // 저장 후 해당 날짜/환자로 다시 예약 화면으로 이동
        return "redirect:/reservations?patientId=" + patientId + "&date=" + date;
    }
}
