package com.example.erp.Patient;

import com.example.erp.User_account.User_account;
import com.example.erp.Visit.OutHistoryDTO;
import com.example.erp.Visit.Visit;
import com.example.erp.Visit.VisitRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientRegistrationAndInquiry {

    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;

    // 목록/조회 화면
    @GetMapping
    public String patientList(
            @RequestParam(value = "patientId", required = false) Long patientId,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
    	// --- 로그인 사용자 정보 추가 ---
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            model.addAttribute("userId", auth.getName()); // 로그인 ID
            if (auth.getPrincipal() instanceof User_account user) {
                model.addAttribute("userName", user.getName()); // 실제 이름
            }
        }
    	
    	
        addPatientList(keyword, model);

        Patient selected = null;
        List<OutHistoryDTO> outHistories = Collections.emptyList();
        if (patientId != null) {
            selected = patientRepository.findById(patientId).orElse(null);

            List<Visit> visits = visitRepository.findRecentByPatient(patientId);
            outHistories = visits.stream()
                    .map(v -> new OutHistoryDTO(
                            v.getVisit_datetime() != null ? v.getVisit_datetime().toLocalDate() : null,
                            v.getUser_account() != null ? v.getUser_account().getName() : "",
                            v.getVisit_type(),
                            v.getNote()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("selected", selected);
        model.addAttribute("mode", "detail");
        model.addAttribute("outHistories", outHistories);
        model.addAttribute("inHistories", Collections.emptyList());

        return "staff/patient-register";
    }

    // 등록 화면
    @GetMapping("/register")
    public String patientRegister(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        addPatientList(keyword, model);
        model.addAttribute("selected", null);
        model.addAttribute("mode", "register");
        model.addAttribute("outHistories", Collections.emptyList());
        model.addAttribute("inHistories", Collections.emptyList());

        return "staff/patient-register";
    }

    // 등록 + 수정 공통 처리
    @PostMapping("/save")
    public String savePatient(
            @RequestParam(value = "patientId", required = false) Long patientId,
            @RequestParam("name") String name,
            @RequestParam(value = "rrn", required = false) String rrn,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "birth_date", required = false) String birthDateRaw,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "address1", required = false) String address1,
            @RequestParam(value = "address2", required = false) String address2,
            @RequestParam(value = "note", required = false) String note,
            RedirectAttributes redirectAttributes) {

        Patient patient;

        if (patientId != null) {
            patient = patientRepository.findById(patientId).orElse(new Patient());
        } else {
            patient = new Patient();
        }

        patient.setName(name);
        patient.setRrn(rrn);
        patient.setGender(gender);
        patient.setBirth_date(parseBirthDate(birthDateRaw));
        patient.setPhone(phone);
        patient.setEmail(email);
        patient.setAddress1(address1);
        patient.setAddress2(address2);
        patient.setNote(note);

        Patient saved = patientRepository.save(patient);

        redirectAttributes.addFlashAttribute("message", "환자 정보가 저장되었습니다.");

        return "redirect:/patients?mode=detail&patientId=" + saved.getPatient_id();
    }

    private LocalDate parseBirthDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().replace('/', '-').replace('.', '-');
        try {
            return LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void addPatientList(String keyword, Model model) {
        List<Patient> patients;
        if (keyword != null && !keyword.isBlank()) {
            patients = patientRepository.findByNameContainingIgnoreCaseOrPhoneContainingOrRrnContaining(
                    keyword, keyword, keyword);
        } else {
            patients = patientRepository.findAll();
        }
        Map<Long, LocalDate> lastVisitMap = new HashMap<>();
        for (Patient p : patients) {
            LocalDate lastDate = visitRepository.findByPatientIdOrderByVisitDatetimeDesc(p.getPatient_id()).stream()
                    .map(Visit::getVisit_datetime)
                    .filter(Objects::nonNull)
                    .map(LocalDateTime::toLocalDate)
                    .findFirst()
                    .orElse(null);
            lastVisitMap.put(p.getPatient_id(), lastDate);
        }
        model.addAttribute("patients", patients);
        model.addAttribute("keyword", keyword);
        model.addAttribute("lastVisitMap", lastVisitMap);
    }
}
