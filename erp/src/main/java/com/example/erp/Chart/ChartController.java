package com.example.erp.Chart;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erp.Patient.PatientService;
import com.example.erp.Visit.VisitService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChartController {
	private final ChartService chartService;
	
	@PostMapping("/doctor/chartSave") // 의사-> 차트 저장 버튼 by 은서
	public String saveChart(			
	        @RequestParam("visit_id") Long visit_id,
	        @RequestParam("chart_id") Long chart_id,
	        @RequestParam("patient_id") Long patient_id,
	        @RequestParam("subjective") String subjective,
	        @RequestParam("objective") String objective,
	        @RequestParam("assessment") String assessment,
	        @RequestParam("plan") String plan,
	        @RequestParam("note") String note,

	        // 질병코드 배열
	        @RequestParam(value = "diseases_code[]",required = false) List<String> diseases_code,

	        // ===== 일반 수가 =====
	        @RequestParam(value = "normal_fee_item_code[]", required = false)
	        List<Long> normal_fee_item_code,

	        @RequestParam(value = "normal_quantity[]", required = false)
	        List<Integer> normal_quantity,

	        // ===== 약품 =====
	        @RequestParam(value = "drug_fee_item_code[]", required = false)
	        List<Long> drug_fee_item_code,

	        @RequestParam(value = "dose[]", required = false)
	        List<Integer> dose,

	        @RequestParam(value = "times_per_day[]", required = false)
	        List<Integer> times_per_day,

	        @RequestParam(value = "days[]", required = false)
	        List<Integer> days
			) {

		chartService.saveAll(
			    visit_id,
			    chart_id,
			    subjective, objective, assessment, plan, note,
			    diseases_code,

			    // 일반 수가
			    normal_fee_item_code,
			    normal_quantity,

			    // 약품
			    drug_fee_item_code,
			    dose,
			    times_per_day,
			    days
		);


	    return "redirect:/doctor/chartView?visit_id=" + visit_id + "&patient_id=" + patient_id;
	}

}
