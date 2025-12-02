package com.example.erp.Visit;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller // 타임리프 html페이지로 연결
@RequiredArgsConstructor //finall이나 nonnull 필드 생성자 자동생성
public class VisitController {
	private final VisitService visitService;
	
	@GetMapping("/doctor/todayVisits")
	public String getTodayVisitList(Model model) { //의사 -> 금일 진료 리스트
        List<TodayVisitDTO> todayList = visitService.getTodayVisitList();
        
        model.addAttribute("todayList", todayList);
        return "doctor/todayVisits";  // 타임리프 HTML 파일 경로
    }
	

}
