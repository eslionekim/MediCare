package com.example.erp.Diseases_code;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erp.Staff_profile.Staff_profile;
import com.example.erp.Staff_profile.Staff_profileRepository;

@Controller
public class Diseases_codeController {
	@Autowired
    private Diseases_codeRepository diseases_codeRepository;
	@Autowired
	private Staff_profileRepository staff_profileRepository;
	
	@GetMapping("/doctor/popup/diseases") //의사-> 차트 작성 -> 상병정보 -> 검색
    public String popupDiseases(@RequestParam(value="keyword", required=false) String keyword, Model model) {
													// 사용자가 입력한 검색어
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String userId = auth.getName(); // 로그인한 user_id

	    // Staff_profile 조회
	    Staff_profile staff = staff_profileRepository.findByUserId(userId);
	    String departmentName = staff.getDepartment().getName(); // 진료과 이름

	    List<Diseases_code> list;

	    if (keyword == null || keyword.isEmpty()) {
	        list = diseases_codeRepository.findByDepartment(departmentName);
	    } else {
	        list = diseases_codeRepository.searchByDepartmentAndKeyword(departmentName, keyword);
	    }

        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);

        return "doctor/popup/diseasesPopup"; //팝업 띄우기
    }
}
