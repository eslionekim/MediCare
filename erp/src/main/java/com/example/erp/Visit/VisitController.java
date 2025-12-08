package com.example.erp.Visit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erp.Chart.Chart;
import com.example.erp.Chart.ChartService;
import com.example.erp.Chart_diseases.Chart_diseases;
import com.example.erp.Chart_diseases.Chart_diseasesRepository;
import com.example.erp.Claim.Claim;
import com.example.erp.Claim.ClaimRepository;
import com.example.erp.Claim_item.Claim_item;
import com.example.erp.Claim_item.Claim_itemRepository;
import com.example.erp.Diseases_code.Diseases_code;
import com.example.erp.Diseases_code.Diseases_codeRepository;
import com.example.erp.Fee_item.Fee_item;
import com.example.erp.Patient.Patient;
import com.example.erp.Patient.PatientService;

import lombok.RequiredArgsConstructor;

@Controller // 타임리프 html페이지로 연결
@RequiredArgsConstructor //finall이나 nonnull 필드 생성자 자동생성
public class VisitController {
	private final VisitService visitService;
	private final ChartService chartService;
	private final PatientService patientService;
	private final Chart_diseasesRepository chart_diseasesRepository;
	private final Diseases_codeRepository diseases_codeRepository;
	private final ClaimRepository claimRepository;
	private final Claim_itemRepository claim_itemRepository;
	
	@GetMapping("/doctor/todayVisits") //의사 -> 금일 진료 리스트
	public String getTodayVisitList(Model model) { 
        List<TodayVisitDTO> todayList = visitService.getTodayVisitList();
        model.addAttribute("todayList", todayList);
        return "doctor/todayVisits";  // 타임리프 HTML 파일 경로
    }
	
	@GetMapping("/doctor/allVisits") //의사 -> 전체 진료 리스트
	public String getAllVisitList(Model model) {
	    List<AllVisitDTO> allVisits = visitService.getAllVisitList();
	    model.addAttribute("allVisits", allVisits);
	    return "doctor/allVisits"; // 타임리프 HTML 파일
	}

	
	@GetMapping("/doctor/chartWrite") //금일 진료 리스트 -> 진료시작 -> 차트 생성-> 차트 작성 페이지 이동
    public String chartWrite(@RequestParam("visit_id") Long visit_id, @RequestParam("patient_id") Long patient_id,Model model) {

		Chart chart = chartService.createBasicChart(visit_id); //userId로 차트 기본 생성 
        Patient patient = patientService.findById(patient_id); //patientId로 환자 정보 조회
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
            fee_item  = claim_item.stream()
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
	public String chartView(@RequestParam("visit_id") Long visit_id, @RequestParam("patient_id") Long patient_id,Model model) {
		Chart chart = chartService.createBasicChart(visit_id); //userId로 차트 기본 생성 
        Patient patient = patientService.findById(patient_id); //patientId로 환자 정보 조회
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
            fee_item  = claim_item.stream()
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
}
