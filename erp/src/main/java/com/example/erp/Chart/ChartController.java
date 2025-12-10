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

	        // 처방 항목 배열
	        @RequestParam(value = "fee_item_code[]",required = false) List<String> fee_item_code,
	        @RequestParam(value = "base_price[]",required = false) List<Integer> base_price,
	        @RequestParam(value = "quantity[]",required = false) List<Integer> quantity
			) {

	    chartService.saveAll(
	            visit_id,
	            chart_id,
	            subjective, objective, assessment, plan, note,
	            diseases_code,
	            fee_item_code, base_price, quantity
	    );

	    return "redirect:/doctor/chartView?visit_id=" + visit_id + "&patient_id=" + patient_id;
	}

}
