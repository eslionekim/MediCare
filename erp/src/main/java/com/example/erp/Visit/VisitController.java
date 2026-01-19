package com.example.erp.Visit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.erp.Chart.Chart;
import com.example.erp.Chart.ChartRepository;
import com.example.erp.Chart.ChartService;
import com.example.erp.Chart_diseases.Chart_diseases;
import com.example.erp.Chart_diseases.Chart_diseasesRepository;
import com.example.erp.Claim.Claim;
import com.example.erp.Claim.ClaimRepository;
import com.example.erp.Claim_item.Claim_item;
import com.example.erp.Claim_item.Claim_itemRepository;
import com.example.erp.Department.Department;
import com.example.erp.Department.DepartmentRepository;
import com.example.erp.Diseases_code.Diseases_code;
import com.example.erp.Diseases_code.Diseases_codeRepository;
import com.example.erp.Fee_item.DrugViewDTO;
import com.example.erp.Fee_item.Fee_item;
import com.example.erp.Insurance_code.Insurance_code;
import com.example.erp.Insurance_code.Insurance_codeRepository;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Patient.Patient;
import com.example.erp.Patient.PatientService;
import com.example.erp.Patient.VisitHistoryDto;
import com.example.erp.Prescription.Prescription;
import com.example.erp.Prescription.PrescriptionRepository;
import com.example.erp.Prescription_item.Prescription_item;
import com.example.erp.Prescription_item.Prescription_itemRepository;
import com.example.erp.Reservation.Reservation;
import com.example.erp.Reservation.ReservationRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.Stock.Stock;
import com.example.erp.Stock.StockRepository;
import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;
import com.example.erp.Visit.OutHistoryDTO;
import com.example.erp.Staff_profile.Staff_profileRepository;
import com.example.erp.Staff_profile.Staff_profile;

import lombok.RequiredArgsConstructor;

@Controller // 타임리프 html페이지로 연결
@RequiredArgsConstructor // finall이나 nonnull 필드 생성자 자동생성
public class VisitController {
        private final VisitService visitService;
        private final ChartService chartService;
        private final PatientService patientService;
        private final Chart_diseasesRepository chart_diseasesRepository;
        private final ChartRepository chartRepository;
        private final Diseases_codeRepository diseases_codeRepository;
        private final ClaimRepository claimRepository;
        private final Claim_itemRepository claim_itemRepository;
        private final VisitRepository visitRepository; //
        private final User_accountRepository user_accountRepository;
        private final DepartmentRepository departmentRepository;
        private final Insurance_codeRepository insuranceCodeRepository;
        private final Status_codeRepository statusCodeRepository;
        private final Staff_profileRepository staffProfileRepository;
        private final ReservationRepository reservationRepository;
        private final Prescription_itemRepository prescription_itemRepository;
        private final PrescriptionRepository prescriptionRepository;
        private final StockRepository stockRepository;
        private final ItemRepository itemRepository;

        // ====================== 의사용 기능 ====================== by 은서

        @GetMapping("/doctor/todayVisits") // 의사 -> 금일 진료 리스트
        public String getTodayVisitList(Model model) {
        	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        	String userId = auth.getName(); // 로그인 ID

        	// User_account 테이블에서 userId로 검색
        	Optional<User_account> user = user_accountRepository.findByUser_id(userId);
        	String userName = (user != null) ? user.get().getName() : "알 수 없음";

        	model.addAttribute("userId", userId);
        	model.addAttribute("userName", userName);

            List<TodayVisitDTO> todayList = visitService.getTodayVisitListByUser(userId);
            model.addAttribute("todayList", todayList);
            return "doctor/todayVisits"; // 타임리프 HTML 파일 경로
        }

        @GetMapping("/doctor/allVisits") // 의사 -> 전체 진료 리스트
        public String getAllVisitList(Model model) {
                List<AllVisitDTO> allVisits = visitService.getAllVisitList();
                model.addAttribute("allVisits", allVisits);
                model.addAttribute("departments", departmentRepository.findAll());
                return "doctor/allVisits"; // 타임리프 HTML 파일
        }
        
        @GetMapping("/doctor/allVisits/search") // 의사-> 전체 진료 리스트 - 검색창
        public String searchVisits(
                @RequestParam(value = "department",required = false) String department,
                @RequestParam(value = "doctor",required = false) String doctor,
                @RequestParam(value = "keyword",required = false) String keyword,
                @RequestParam(value = "date",required = false) String date,
                Model model) {

            model.addAttribute("allVisits",
                visitService.searchVisits(department, doctor, keyword, date));

            return "doctor/allVisits :: table"; // 타임리프 fragment
        }


        @GetMapping("/doctor/chartWrite") 
        public String chartWrite(@RequestParam("visit_id") Long visit_id, @RequestParam("patient_id") Long patient_id,
                                Model model) {

        	// ✅ 있으면 가져오고 없으면 생성
            Chart chart = chartRepository.findByVisit_VisitId(visit_id)
                    .orElseGet(() -> chartService.createBasicChart(visit_id));
            Patient patient = patientService.findById(patient_id);
            List<Visit> pastVisits = visitService.findByPatientId(patient_id);
            Visit visit = visitService.findWithDepartmentAndInsurance(visit_id);

            // 상병 조회
            List<Chart_diseases> chart_diseases = chart_diseasesRepository.findByChart(chart);
            List<Diseases_code> diseasesList = chart_diseases.stream()
                            .map(Chart_diseases::getDiseases_code)
                            .collect(Collectors.toList());

            // Claim + Prescription → DTO
		    List<Claim> claims = claimRepository.findAllByVisitId(visit_id);
		
		    List<Claim_item> normalClaimItems = new ArrayList<>();
		    List<DrugViewDTO> drugItems = new ArrayList<>();
		    
		    List<Prescription_item> prescriptionItems =
		            prescription_itemRepository.findAllByVisitId(visit_id);

            for (Claim claim : claims) {
                for (Claim_item ci : claim_itemRepository.findAllByClaim(claim)) {

                    String category = ci.getFee_item().getCategory();

                    // 진찰료 제외
                    if ("진찰료".equals(category)) continue;

                    // 약품
					if ("약품".equals(category)) {
					    DrugViewDTO dto = new DrugViewDTO();
					
					    dto.setFeeItemCode(ci.getFee_item().getFee_item_code());
					    dto.setCategory(category);
					    dto.setName(ci.getFee_item().getName());
					    dto.setBasePrice(ci.getFee_item().getBase_price());
					
					    // item_code → Prescription_item 조회
					    itemRepository.findByFeeItemCode(ci.getFee_item().getFee_item_code())
					    .ifPresent(item -> {
					        List<Prescription_item> pis = prescription_itemRepository.findAllByItemCodeAndVisitId(item.getItem_code(), visit_id);
					        for (Prescription_item pi : pis) {
					            dto.setDose(pi.getDose());
					            dto.setFrequency(pi.getFrequency());
					            dto.setDays(pi.getDays());
					        }
					    });

					
					    drugItems.add(dto);
					} else {
					    normalClaimItems.add(ci);
					}
                }
            }

            model.addAttribute("chart", chart);
            model.addAttribute("patient", patient);
            model.addAttribute("pastVisits", pastVisits);
            model.addAttribute("visit", visit);
            model.addAttribute("diseases_code", diseasesList);
            model.addAttribute("normal_claim_items", normalClaimItems);
            model.addAttribute("drugItems", drugItems);

            return "doctor/chartWrite";
        }


        @GetMapping("/doctor/chartView")
		public String chartView(
		        @RequestParam("visit_id") Long visit_id,
		        @RequestParam("patient_id") Long patient_id,
		        Model model) {
		
		    Patient patient = patientService.findById(patient_id);
		    List<Visit> pastVisits = visitService.findByPatientId(patient_id);
		    Visit visit = visitService.findWithDepartmentAndInsurance(visit_id);
		
		    Chart chart = chartRepository.findByVisit_VisitId(visit_id)
		            .orElseThrow(() -> new IllegalStateException("해당 방문의 차트가 존재하지 않습니다."));
		    // 상병
		    List<Chart_diseases> chart_diseases =
		            chart_diseasesRepository.findByChart(chart);
		
		    List<Diseases_code> diseasesList = chart_diseases.stream()
		            .map(Chart_diseases::getDiseases_code)
		            .collect(Collectors.toList());
		
		    // Claim + Prescription → DTO
		    List<Claim> claims = claimRepository.findAllByVisitId(visit_id);
		
		    List<Claim_item> normalClaimItems = new ArrayList<>();
		    List<DrugViewDTO> drugItems = new ArrayList<>();
		    
		    List<Prescription_item> prescriptionItems =
		            prescription_itemRepository.findAllByVisitId(visit_id);

		
		    for (Claim claim : claims) {
		        for (Claim_item ci : claim_itemRepository.findAllByClaim(claim)) {
		
		            String category = ci.getFee_item().getCategory();
		
		            // 진찰료 제외
		            if ("진찰료".equals(category)) continue;
		
		            // 약품
					if ("약품".equals(category)) {
					    DrugViewDTO dto = new DrugViewDTO();
					
					    dto.setFeeItemCode(ci.getFee_item().getFee_item_code());
					    dto.setCategory(category);
					    dto.setName(ci.getFee_item().getName());
					    dto.setBasePrice(ci.getFee_item().getBase_price());
					
					    // item_code → Prescription_item 조회
					    itemRepository.findByFeeItemCode(ci.getFee_item().getFee_item_code())
					    .ifPresent(item -> {
					        List<Prescription_item> pis = prescription_itemRepository.findAllByItemCodeAndVisitId(item.getItem_code(), visit_id);
					        for (Prescription_item pi : pis) {
					            dto.setDose(pi.getDose());
					            dto.setFrequency(pi.getFrequency());
					            dto.setDays(pi.getDays());
					        }
					    });

					
					    drugItems.add(dto);
					} else {
					    normalClaimItems.add(ci);
					}

		        }
		    }
		
		    model.addAttribute("chart", chart);
		    model.addAttribute("patient", patient);
		    model.addAttribute("pastVisits", pastVisits);
		    model.addAttribute("visit", visit);
		    model.addAttribute("diseases_code", diseasesList);
		
		    model.addAttribute("normal_claim_items", normalClaimItems);
		    model.addAttribute("drug_items", drugItems); // ⭐ 핵심
		
		    return "doctor/chartView";
		}


        // ====================== 원무과: 접수 화면 ======================
        @GetMapping("/receptions")
        public String receptionPage(
                        @RequestParam(value = "patientId", required = false) Long patientId,
                        @RequestParam(value = "patientKeyword", required = false) String patientKeyword,
                        @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                        Model model) {
	        	
        		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        	String userId = auth.getName(); // 로그인 ID
				// User_account 테이블에서 userId로 검색
	        	Optional<User_account> user = user_accountRepository.findByUser_id(userId);
	        	String userName = (user != null) ? user.get().getName() : "알 수 없음";
        	
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
                                && v.getStatus_code().getStatus_code().equalsIgnoreCase("VIS_WAITING"))
                                .collect(Collectors.toList());
                
                model.addAttribute("userId", userId);
            	model.addAttribute("userName", userName);

                model.addAttribute("todayTotal", todays.size());
                model.addAttribute("todayWaiting", waiting.size());
                model.addAttribute("waitingVisits", waiting);
                model.addAttribute("date", date);

                // 3) 환자 화면에서 넘어온 patientId 로 선택 환자 + 히스토리 조회
                Patient selectedPatient = null;
                List<Visit> visitHistories = Collections.emptyList();
                List<OutHistoryDTO> outHistories = Collections.emptyList();
                List<Reservation> confirmedReservations = Collections.emptyList();

                if (patientId != null) {
                        selectedPatient = patientService.findById(patientId); // 이미 있는 서비스 활용

                        if (selectedPatient != null) {
                                // 히스토리는 visitService 써도 되고, repository 써도 됨
                                visitHistories = visitRepository.findByPatientOrderByVisitDatetimeDesc(selectedPatient);

                                List<Visit> visits = visitRepository.findRecentByPatient(patientId);
                                outHistories = visits.stream()
                                                .map(v -> new OutHistoryDTO(
                                                                v.getVisit_datetime() != null
                                                                                ? v.getVisit_datetime().toLocalDate()
                                                                                : null,
                                                                v.getUser_account() != null
                                                                                ? v.getUser_account().getName()
                                                                                : "",
                                                                v.getVisit_type(),
                                                                v.getNote()))
                                                .collect(Collectors.toList());

                                // 오늘 확정 예약(RES_CONFIRMED) 목록 (이미 접수된 예약은 제외)
                                List<Reservation> candidates = reservationRepository
                                                .findByPatientAndStartTimeBetweenAndStatusCodes(
                                                                patientId, start, end, List.of("RES_CONFIRMED", "RES_PENDING"));
                                confirmedReservations = candidates.stream()
                                                .filter(r -> !visitRepository.existsByReservationId(
                                                                r.getReservation_id()))
                                                .collect(Collectors.toList());
                        }
                }

                model.addAttribute("selectedPatient", selectedPatient);
                model.addAttribute("visitHistories", visitHistories);
                model.addAttribute("outHistories", outHistories);
                model.addAttribute("confirmedReservations", confirmedReservations);
                model.addAttribute("patientKeyword", patientKeyword);
                model.addAttribute("patientSearchResults", patientService.searchPatients(patientKeyword));

                // 4) 드롭다운 데이터 (DB 연동)
                List<Department> departments = departmentRepository.findActive();
                List<Staff_profile> doctorProfiles = staffProfileRepository.findAllWithUserAndDepartment();
                List<Insurance_code> insuranceCodes = insuranceCodeRepository.findAll();

                model.addAttribute("departments", departments);
                model.addAttribute("doctorProfiles", doctorProfiles);
                model.addAttribute("insuranceCodes", insuranceCodes);
                model.addAttribute("visit_types", List.of("초진", "재진"));
                model.addAttribute("visitRoutes", List.of("당일방문", "예약"));

                return "staff/reception";
        }

        // 접수 등록
        @PostMapping("/receptions/save")
        public String saveReception(
                        @RequestParam(name = "patientId") Long patientId, // 환자 번호
                        @RequestParam(name = "user_id") String user_id, // 담당 의사 user_id (문자 PK)
                        @RequestParam(name = "departmentCode") String departmentCode, // 진료과 코드 (예: ORTHO)
                        @RequestParam(name = "visit_type") String visit_type, // first / follow-up
                        @RequestParam(name = "visitRoute") String visitRoute, // walk-in / reservation
                        @RequestParam(name = "reservationId", required = false) Long reservationId, // 예약 기반 접수(선택)
                        @RequestParam(name = "insurance_code", required = false) String insuranceCode, // 보험 코드
                        @RequestParam(name = "note", required = false) String note,
                        RedirectAttributes redirectAttributes) {

                // 1) 연관 엔티티 조회
                Patient patient = patientService.findById(patientId);

                User_account doctor = user_accountRepository.findById(user_id)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 의사 ID입니다."));

                Department department = departmentRepository.findById(departmentCode)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 진료과 코드입니다."));

                Reservation reservation = null;
                if (reservationId != null) {
                        reservation = reservationRepository.findById(reservationId)
                                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

                        if (reservation.getPatient() == null
                                        || reservation.getPatient().getPatient_id() == null
                                        || !reservation.getPatient().getPatient_id().equals(patientId)) {
                                throw new IllegalArgumentException("해당 환자의 예약이 아닙니다.");
                        }

                        String resStatus = reservation.getStatus_code() != null ? reservation.getStatus_code().getStatus_code()
                                        : null;
                        if (resStatus == null
                                        || !(resStatus.equalsIgnoreCase("RES_CONFIRMED")
                                                        || resStatus.equalsIgnoreCase("RES_PENDING"))) {
                                throw new IllegalArgumentException("접수 가능한 예약 상태가 아닙니다.");
                        }

                        if (visitRepository.existsByReservationId(reservationId)) {
                                throw new IllegalArgumentException("이미 접수된 예약입니다.");
                        }

                        // 예약 정보로 의사/진료과를 강제 매핑 (폼 값보다 우선)
                        doctor = reservation.getUser();
                        department = reservation.getDepartment();
                        visitRoute = "예약";
                }

                Insurance_code insurance = null;
                if (insuranceCode != null && !insuranceCode.isBlank()) {
                        insurance = insuranceCodeRepository.findById(insuranceCode)
                                        .orElse(null); // 없으면 null (선택값이면 이렇게 처리)
                }

                Status_code waitStatus = statusCodeRepository.findById("VIS_WAITING")
                                .orElseThrow(() -> new IllegalArgumentException("기본 대기 상태코드가 없습니다."));

                // 2) Visit 엔티티 생성 및 값 셋팅
                LocalDateTime now = LocalDateTime.now();

                Visit visit = new Visit();
                visit.setPatient(patient);
                visit.setReservation(reservation); // 예약 기반 접수면 연결
                visit.setUser_account(doctor);
                visit.setDepartment(department);
                visit.setVisit_datetime(now);
                visit.setVisit_route(visitRoute);
                visit.setVisit_type(visit_type);
                visit.setInsurance_code(insurance);
                visit.setStatus_code(waitStatus);
                visit.setNote(note);
                visit.setCreated_at(now);

                // 3) 저장
                visitRepository.save(visit);

                // 4) 예약 기반 접수면 예약 상태를 RES_COMPLETED 로 변경
                if (reservation != null) {
                        Status_code completed = statusCodeRepository.findById("RES_COMPLETED")
                                        .orElseThrow(() -> new IllegalArgumentException("예약 완료 상태코드가 없습니다."));
                        reservation.setStatus_code(completed);
                        reservationRepository.save(reservation);
                }

                redirectAttributes.addFlashAttribute("message", "접수가 등록되었습니다.");
                return "redirect:/receptions?patientId=" + patientId;
        }

}
