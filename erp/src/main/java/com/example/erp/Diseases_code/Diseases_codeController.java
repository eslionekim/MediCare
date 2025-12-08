package com.example.erp.Diseases_code;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Diseases_codeController {
	@Autowired
    private Diseases_codeRepository diseases_codeRepository;
	
	@GetMapping("/doctor/popup/diseases") //의사-> 차트 작성 -> 상병정보 -> 검색
    public String popupDiseases(@RequestParam(value="keyword", required=false) String keyword, Model model) {
													// 사용자가 입력한 검색어
        List<Diseases_code> list; // 질병코드 리스트 조회

        if (keyword == null || keyword.isEmpty()) { // 
            list = diseases_codeRepository.findAll();   // 전체 목록
        } else {
            list = diseases_codeRepository.searchAll(keyword); //질병코드와 한글명 모두 검색
        }

        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);

        return "doctor/popup/diseasesPopup"; //팝업 띄우기
    }
}
