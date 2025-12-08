package com.example.erp.Vacation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Chart.ChartService;
import com.example.erp.Status_code.Status_codeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class VacationController {
	private final VacationRepository vacationRepository;
	private final Status_codeRepository status_codeRepository;
	
	@GetMapping("/hr/vacationList")
    public String vacationList(Model model) {
        List<Vacation> vacation = vacationRepository.vacationList(); // 필요하면 status_code, user_account, staff_profile join fetch
        model.addAttribute("vacation", vacation);
        return "hr/vacationList";
    }
	
	// 상태 변경 AJAX
    @PostMapping("/hr/vacation/updateStatus")
    @ResponseBody
    public Map<String, Object> updateStatus(@RequestBody Map<String, String> params) {
        try {
            Long vacationId = Long.valueOf(params.get("vacationId"));
            String status = params.get("status");

            Vacation vacation = vacationRepository.findById(vacationId).orElse(null);
            if (vacation != null) {
                vacation.setStatus_code(status_codeRepository.findByCode(status)); //상태코드 설정
                vacationRepository.save(vacation); //설정한대로 저장
                return Map.of("success", true); //성공 여부를 JSON으로 반환
            }
        } catch (Exception e) { 
            e.printStackTrace();
        }
        return Map.of("success", false);
    }
}
