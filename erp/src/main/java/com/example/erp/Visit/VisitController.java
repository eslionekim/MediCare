package com.example.erp.Visit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.erp.Chart.Chart;
import com.example.erp.Chart.ChartService;
import com.example.erp.Chart_diseases.Chart_diseases;
import com.example.erp.Chart_diseases.Chart_diseasesRepository;
import com.example.erp.Claim.Claim;
import com.example.erp.Claim.ClaimRepository;
import com.example.erp.Claim_item.Claim_item;
import com.example.erp.Claim_item.Claim_itemRepository;
import com.example.erp.Department.Department;
import com.example.erp.Diseases_code.Diseases_code;
import com.example.erp.Diseases_code.Diseases_codeRepository;
import com.example.erp.Fee_item.Fee_item;
import com.example.erp.Insurance_code.Insurance_code;
import com.example.erp.Patient.Patient;
import com.example.erp.Patient.PatientService;
import com.example.erp.Patient.VisitHistoryDto;
import com.example.erp.Status_code.Status_code;
import com.example.erp.User_account.User_account;

import lombok.RequiredArgsConstructor;

@Controller // 타임리프 html페이지로 연결
@RequiredArgsConstructor // finall이나 nonnull 필드 생성자 자동생성
public class VisitController {
        private final VisitService visitService;
        private final ChartService chartService;
        private final PatientService patientService;
        private final Chart_diseasesRepository chart_diseasesRepository;
        private final Diseases_codeRepository diseases_codeRepository;
        private final ClaimRepository claimRepository;
        private final Claim_itemRepository claim_itemRepository;
        private final VisitRepository visitRepository; //
        // 추가
        private final com.example.erp.User_account.User_accountRepository userAccountRepository;
        private final com.example.erp.Department.DepartmentRepository departmentRepository;
        private final com.example.erp.Insurance_code.Insurance_codeRepository insuranceCodeRepository;
        private final com.example.erp.Status_code.Status_codeRepository statusCodeRepository;

        // ====================== 의사용 기능 ======================

        @GetMapping("/doctor/todayVisits") // 의사 -> 금일 진료 리스트
        public String getTodayVisitList(Model model) {
                List<TodayVisitDTO> todayList = visitService.getTodayVisitList();
                model.addAttribute("todayList", todayList);
                return "doctor/todayVisits"; // 타임리프 HTML 파일 경로
        }

        @GetMapping("/doctor/allVisits") // 의사 -> 전체 진료 리스트
        public String getAllVisitList(Model model) {
                List<AllVisitDTO> allVisits = visitService.getAllVisitList();
                model.addAttribute("allVisits", allVisits);
                return "doctor/allVisits"; // 타임리프 HTML 파일
        }

        @GetMapping("/doctor/chartWrite") // 금일 진료 리스트 -> 진료시작 -> 차트 생성-> 차트 작성 페이지 이동
        public String chartWrite(@RequestParam("visit_id") Long visit_id, @RequestParam("patient_id") Long patient_id,
                        Model model) {

                Chart chart = chartService.createBasicChart(visit_id); // userId로 차트 기본 생성
                Patient patient = patientService.findById(patient_id); // patientId로 환자 정보 조회
                List<Visit> pastVisits = visitService.findByPatientId(patient_id); // 해당 환자의 과거 방문 기록
                Visit visit = visitService.findWithDepartmentAndInsurance(visit_id); // 진료과, 보험명 조회

                // 차트 조회 > 상병 조회 (Chart_diseases -> Diseases_code)
                List<Chart_diseases> chart_diseases = chart_diseasesRepository.findByChart(chart);
                List<Diseases_code> diseasesList = chart_diseases.stream()
                                .map(Chart_diseases::getDiseases_code) // Chart_diseases -> Diseases_code
                                .collect(Collectors.toList());

                // 차트 조회 > 처방 조회 (Claim -> Claim_item -> Fee_item)
                Claim claim = claimRepository.findByVisitId(visit_id).orElse(null);
                List<Fee_item> fee_item = new ArrayList<>();
                List<Claim_item> claim_item = new ArrayList<>();
                if (claim != null) {
                        claim_item = claim_itemRepository.findByClaim(claim);
                        fee_item = claim_item.stream()
                                        .map(Claim_item::getFee_item) // Claim_item -> Fee_item
                                        .collect(Collectors.toList());
                }

                model.addAttribute("chart", chart);
                model.addAttribute("patient", patient);
                model.addAttribute("pastVisits", pastVisits);
                model.addAttribute("visit", visit);
                model.addAttribute("diseases_code", diseasesList);
                model.addAttribute("fee_item", fee_item);
                model.addAttribute("claim_item", claim_item);

                return "doctor/chartWrite";
        }

        @GetMapping("/doctor/chartView")
        public String chartView(@RequestParam("visit_id") Long visit_id, @RequestParam("patient_id") Long patient_id,
                        Model model) {
                Chart chart = chartService.createBasicChart(visit_id); // userId로 차트 기본 생성
                Patient patient = patientService.findById(patient_id); // patientId로 환자 정보 조회
                List<Visit> pastVisits = visitService.findByPatientId(patient_id); // 해당 환자의 과거 방문 기록
                Visit visit = visitService.findWithDepartmentAndInsurance(visit_id); // 진료과, 보험명 조회

                // 차트 조회 > 상병 조회 (Chart_diseases -> Diseases_code)
                List<Chart_diseases> chart_diseases = chart_diseasesRepository.findByChart(chart);
                List<Diseases_code> diseasesList = chart_diseases.stream()
                                .map(Chart_diseases::getDiseases_code) // Chart_diseases -> Diseases_code
                                .collect(Collectors.toList());

                // 차트 조회 > 처방 조회 (Claim -> Claim_item -> Fee_item)
                Claim claim = claimRepository.findByVisitId(visit_id).orElse(null);
                List<Fee_item> fee_item = new ArrayList<>();
                List<Claim_item> claim_item = new ArrayList<>();
                if (claim != null) {
                        claim_item = claim_itemRepository.findByClaim(claim);
                        fee_item = claim_item.stream()
                                        .map(Claim_item::getFee_item) // Claim_item -> Fee_item
                                        .collect(Collectors.toList());
                }

                model.addAttribute("chart", chart);
                model.addAttribute("patient", patient);
                model.addAttribute("pastVisits", pastVisits);
                model.addAttribute("visit", visit);
                model.addAttribute("diseases_code", diseasesList);
                model.addAttribute("fee_item", fee_item);
                model.addAttribute("claim_item", claim_item);

                return "doctor/chartView";
        }

        // ====================== 원무과: 접수 화면 ======================
        @GetMapping("/receptions")
        public String receptionPage(
                        @RequestParam(value = "patientId", required = false) Long patientId,
                        @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                        Model model) {

                if (date == null) {
                        date = LocalDate.now();
                }

                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(LocalTime.MAX);

                // 1) 오늘 방문 리스트
                List<Visit> todays = visitRepository.findByVisitDatetimeBetweenOrderByVisitDatetimeAsc(start, end);

                // 2) 오늘 대기자만 (status_code.code == WAIT 이런 식으로 필터)
                List<Visit> waiting = todays.stream()
                                .filter(v -> v.getStatus_code() != null
                                                && v.getStatus_code().getStatus_code() != null
                                                && v.getStatus_code().getStatus_code().equalsIgnoreCase("WAIT"))
                                .collect(Collectors.toList());

                model.addAttribute("todayTotal", todays.size());
                model.addAttribute("todayWaiting", waiting.size());
                model.addAttribute("waitingVisits", waiting);
                model.addAttribute("date", date);

                // 3) 환자 화면에서 넘어온 patientId 로 선택 환자 + 히스토리 조회
                Patient selectedPatient = null;
                List<Visit> visitHistories = Collections.emptyList();

                if (patientId != null) {
                        selectedPatient = patientService.findById(patientId); // 이미 있는 서비스 활용

                        if (selectedPatient != null) {
                                // 히스토리는 visitService 써도 되고, repository 써도 됨
                                visitHistories = visitRepository.findByPatientOrderByVisitDatetimeDesc(selectedPatient);
                        }
                }

                model.addAttribute("selectedPatient", selectedPatient);
                model.addAttribute("visitHistories", visitHistories);

                // 4) 드롭다운 데이터 (임시 하드코딩, 나중에 코드/마스터 테이블 연동 가능)
                model.addAttribute("departments",
                                List.of("ORTHO", "GS", "ENDO", "PULMO")); // 진료과 코드 예시
                model.addAttribute("visitTypes",
                                List.of("first", "follow-up"));
                model.addAttribute("visitRoutes",
                                List.of("walk-in", "reservation"));

                return "staff/reception";
        }

        // 접수 등록
        @PostMapping("/receptions/save")
        public String saveReception(
                        @RequestParam Long patientId, // 환자 번호
                        @RequestParam Long doctorUserId, // 담당 의사 user_id
                        @RequestParam String departmentCode, // 진료과 코드 (예: ORTHO)
                        @RequestParam String visitType, // first / follow-up
                        @RequestParam String visitRoute, // walk-in / reservation
                        @RequestParam(required = false) String insuranceCode, // 보험 코드
                        @RequestParam(required = false) String note,
                        RedirectAttributes redirectAttributes) {

                // 1) 연관 엔티티 조회
                Patient patient = patientService.findById(patientId);

                User_account doctor = userAccountRepository.findById(doctorUserId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 의사 ID입니다."));

                Department department = departmentRepository.findById(departmentCode)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 진료과 코드입니다."));

                Insurance_code insurance = null;
                if (insuranceCode != null && !insuranceCode.isBlank()) {
                        insurance = insuranceCodeRepository.findById(insuranceCode)
                                        .orElse(null); // 없으면 null (선택값이면 이렇게 처리)
                }

                // 🔥 상태코드 WAIT 기본값 로딩 (여기는 네 DB 코드에 맞춰 수정)
                // 예: status_code 테이블의 PK가 "VISIT_WAIT" 이라면 그 값 넣어줘야 함.
                Status_code waitStatus = statusCodeRepository.findById("VISIT_WAIT")
                                .orElseThrow(() -> new IllegalArgumentException("기본 대기 상태코드가 없습니다."));

                // 2) Visit 엔티티 생성 및 값 셋팅
                LocalDateTime now = LocalDateTime.now();

                Visit visit = new Visit();
                visit.setPatient(patient);
                visit.setReservation(null); // 예약 기반 접수면 나중에 설정
                visit.setUser_account(doctor);
                visit.setDepartment(department);
                visit.setVisit_datetime(now);
                visit.setVisit_route(visitRoute);
                visit.setVisit_type(visitType);
                visit.setInsurance_code(insurance);
                visit.setStatus_code(waitStatus);
                visit.setNote(note);
                visit.setCreated_at(now);

                // 3) 저장
                visitRepository.save(visit);

                redirectAttributes.addFlashAttribute("message", "접수가 등록되었습니다.");
                return "redirect:/receptions?patientId=" + patientId;
        }

}
