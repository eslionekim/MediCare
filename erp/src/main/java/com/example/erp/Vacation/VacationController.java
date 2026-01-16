package com.example.erp.Vacation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Chart.ChartService;
import com.example.erp.Department.Department;
import com.example.erp.Department.DepartmentRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeDTO;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.User_account.User_account;
import com.example.erp.Vacation_type.Vacation_type;
import com.example.erp.Vacation_type.Vacation_typeDTO;
import com.example.erp.Vacation_type.Vacation_typeRepository;
import com.example.erp.notification.NotificationService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class VacationController {
	private final VacationRepository vacationRepository;
	private final Vacation_typeRepository vacation_typeRepository;
	private final Status_codeRepository status_codeRepository;
	private final DepartmentRepository departmentRepository;
	private final NotificationService notificationService;
	
	@GetMapping("/hr/vacationList") //인사 -> 휴가 리스트 by 은서
    public String vacationList(Model model) {
        List<Vacation> vacation = vacationRepository.vacationList(); // 필요하면 status_code, user_account, staff_profile join fetch
        String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("vacation", vacation);
        model.addAttribute("username", user_id);
        return "hr/vacationList";
    }
	
	@GetMapping("/hr/vacationList/types") //인사 -> 휴가 리스트->검색창 휴가분류 by 은서
	@ResponseBody
	public List<Vacation_typeDTO> getVacationTypes() {
	    return vacation_typeRepository.findByIsActiveTrue()
	            .stream()
	            .map(v -> new Vacation_typeDTO(
	                    v.getVacation_type_code(),
	                    v.getType_name()
	            ))
	            .toList();
	}
	
	@GetMapping("/hr/vacation/status")//인사 -> 휴가 리스트->검색창 휴가상태 by 은서
	@ResponseBody
	public List<Status_codeDTO> getVacationStatus() {
	    return status_codeRepository
	            .findByCategoryAndIsActiveTrue("vacation")
	            .stream()
	            .map(s -> new Status_codeDTO(
	                    s.getStatus_code(),
	                    s.getName()
	            ))
	            .toList();
	}


	
	// 상태 변경 AJAX
    @PostMapping("/hr/vacation/updateStatus") //인사 -> 휴가 승인/취소/반려 by 은서
    @ResponseBody
    public Map<String, Object> updateStatus(@RequestBody Map<String, String> params) {
        try {
            Long vacationId = Long.valueOf(params.get("vacationId"));
            String status = params.get("status");

            Vacation vacation = vacationRepository.findById(vacationId).orElse(null);
            if (vacation != null) {
            	Status_code statusCode = status_codeRepository.findByCode(status) // 상태코드 설정
            	        .orElseThrow(() -> new RuntimeException("상태코드 없음"));
            	vacation.setStatus_code(statusCode);
                vacationRepository.save(vacation); //설정한대로 저장
                
                // 직원 user_id 가져오기
                String user_id = vacation.getUser_account().getUser_id();

                // 상태코드 name으로 메시지 만들기      보낼 사람  alert 제목     내용
                notificationService.notifyUser(user_id, "휴가 현황", statusCode.getName() + "되었습니다.");

                return Map.of("success", true); //성공 여부를 JSON으로 반환
            }
        } catch (Exception e) { 
            e.printStackTrace();
        }
        return Map.of("success", false);
    }
    
    // 의사 -> 휴가 신청 by 은서
    @GetMapping("/doctor/applyVacation")
    public String applyVacation(Model model) {
        String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Vacation> vacation = vacationRepository.findVacationByUserId(user_id);
        List<Vacation_type> vacationTypes = vacation_typeRepository.findAll();
        
        model.addAttribute("vacation", vacation);
        model.addAttribute("vacationTypes", vacationTypes);
        model.addAttribute("username", user_id);
        
        return "doctor/applyVacation";
    }
    
    // 의사 -> 휴가 신청 -> 폼 제출 by 은서
    @PostMapping("/doctor/applyVacation")
    @ResponseBody // 반환값을 JSON형태(키-값)로 전달
    public Map<String,Object> applyVacation(@RequestBody Map<String,String> body) {
        Map<String,Object> result = new HashMap<>(); // 키-값 
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String user_id = auth.getName(); //로그인 한 사용자의 user_id

            User_account user = new User_account(); 
            user.setUser_id(user_id); //새로 생성한 객체에 user_id 설정

            Vacation_type vt = vacation_typeRepository.findByTypeName(body.get("type_name")) //type_name으로 vacation_type 찾기
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 휴가분류"));

            Status_code status_code = status_codeRepository.findByCode("VAC_APPROVED_REQUESTED") //코드로 status_code찾기
                    .orElseThrow(() -> new RuntimeException("상태코드 없음"));

            Vacation v = new Vacation();
            v.setUser_account(user); 
            v.setVacation_type(vt);
            v.setStart_date(LocalDate.parse(body.get("start_date")));
            v.setEnd_date(LocalDate.parse(body.get("end_date")));
            v.setStatus_code(status_code);
            v.setReason(body.get("reason"));

            vacationRepository.save(v);
            
            result.put("success", true); // 저장 성공 시
            // 직원이 휴가 신청했을 때 HR에게 알림
            notificationService.notifyHR("휴가 신청", "휴가 신청이 있습니다.");
        } catch(Exception e){
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
