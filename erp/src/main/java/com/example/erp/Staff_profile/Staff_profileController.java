package com.example.erp.Staff_profile;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Chart.ChartService;
import com.example.erp.Chart_diseases.Chart_diseasesRepository;
import com.example.erp.Claim.ClaimRepository;
import com.example.erp.Claim_item.Claim_itemRepository;
import com.example.erp.Diseases_code.Diseases_codeRepository;
import com.example.erp.Patient.PatientService;
import com.example.erp.Staff_profile.Staff_profileRepository;
import com.example.erp.Visit.VisitRepository;
import com.example.erp.Visit.VisitService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class Staff_profileController { // 의사- > 전체 스케줄 조회 -> 검색창 -> 진료과로 의사이름 by 은서

    private final Staff_profileRepository staff_profileRepository;

	@GetMapping("/doctor/doctors")
	@ResponseBody
	public List<String> getDoctorsByDepartment(@RequestParam("departmentName") String departmentName) {

	    return staff_profileRepository.findDoctorNamesByDepartmentName(departmentName);
	}

}
