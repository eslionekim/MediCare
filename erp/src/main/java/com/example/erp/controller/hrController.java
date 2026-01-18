package com.example.erp.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Department.Department;
import com.example.erp.Department.DepartmentRepository;
import com.example.erp.Fee_item.Fee_itemRepository;
import com.example.erp.Issue_request.Issue_requestDTO;
import com.example.erp.Issue_request.Issue_requestRepository;
import com.example.erp.Issue_request.Issue_requestService;
import com.example.erp.Issue_request_item.Issue_request_itemRepository;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Role_code.Role_code;
import com.example.erp.Role_code.Role_codeRepository;
import com.example.erp.Staff_profile.MyPageDTO;
import com.example.erp.Staff_profile.Staff_profile;
import com.example.erp.Staff_profile.Staff_profileRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeDTO;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.Stock.StockRepository;
import com.example.erp.Stock.StockService;
import com.example.erp.Stock_move.Stock_moveRepository;
import com.example.erp.Stock_move.Stock_moveService;
import com.example.erp.Stock_move_item.Stock_move_itemRepository;
import com.example.erp.User_account.EmployeeCreateDTO;
import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;
import com.example.erp.User_role.User_role;
import com.example.erp.User_role.User_roleRepository;
import com.example.erp.Vacation.Vacation;
import com.example.erp.Vacation.VacationDTO;
import com.example.erp.Vacation.VacationRepository;
import com.example.erp.Vacation_type.Vacation_type;
import com.example.erp.Vacation_type.Vacation_typeDTO;
import com.example.erp.Vacation_type.Vacation_typeRepository;
import com.example.erp.Warehouse.WarehouseRepository;
import com.example.erp.Work_schedule.ScheduleCalendarDTO;
import com.example.erp.Work_schedule.Work_scheduleService;
import com.example.erp.notification.NotificationService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class hrController {

    private final PasswordEncoder passwordEncoder;

    private final NotificationService notificationService;
	private final Staff_profileRepository staff_profileRepository;
	private final DepartmentRepository departmentRepository;
	private final User_roleRepository user_roleRepository;
	private final User_accountRepository user_accountRepository;
	private final Role_codeRepository role_codeRepository;
	
	private final VacationRepository vacationRepository;
    private final Vacation_typeRepository vacation_typeRepository;
    private final Work_scheduleService work_scheduleService;
    private final Status_codeRepository status_codeRepository;


	@GetMapping("/hr/employee")
	public String employeeList( //직원 리스트
	        @RequestParam(name="department", required = false) String department,
	        @RequestParam(name="keyword", required = false) String keyword,
	        @RequestParam(name = "hireDate",required = false)
	        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	        LocalDate hireDate,
	        Model model) {
	
	    // 전체 직원 (관리자 제외)
	    List<Staff_profile> list =
	            staff_profileRepository.findAllExceptAdmin();
	
	    // 필터 적용
	    List<Staff_profile> filteredList = list.stream()
	            // 근무과
	            .filter(sp ->
	                    department == null || department.isBlank()
	                    || sp.getDepartment().getName().equals(department)
	            )
	            // 키워드 (ID or 이름)
	            .filter(sp -> {
	                if (keyword == null || keyword.isBlank()) return true;
	                String k = keyword.toLowerCase();
	                return sp.getUser_account().getUser_id().toLowerCase().contains(k)
	                    || sp.getUser_account().getName().toLowerCase().contains(k);
	            })
	            // 입사일
	            .filter(sp ->
	                    hireDate == null
	                    || (sp.getHire_date() != null &&
	                        sp.getHire_date().equals(hireDate))
	            )
	            .toList();
	
	    // 근무과 select 옵션 (String 리스트!)
	    List<String> departmentList = list.stream()
	            .map(sp -> sp.getDepartment().getName())
	            .distinct()
	            .sorted()
	            .toList();
	
	    model.addAttribute("staffList", filteredList);
	    model.addAttribute("departments", departmentList);
	
	    // 검색값 유지
	    model.addAttribute("selectedDepartment", department);
	    model.addAttribute("keyword", keyword);
	    model.addAttribute("hireDate", hireDate);
	
	    return "hr/employee";
	}

	
	//인사->직원 등록
	@PostMapping("/hr/employee")
	@ResponseBody
	public ResponseEntity<?> saveEmployee(@RequestBody EmployeeCreateDTO dto) {

	    // 1) User_account 생성
	    User_account user = new User_account();
	    user.setUser_id(dto.getUserId());
	    user.setName(dto.getName());
	    user.setPassword(dto.getPassword());
	    user.set_active(true);
	    user.setCreated_at(LocalDateTime.now());

	    user_accountRepository.save(user);

	    // 2) Department 찾기 (name으로 조회)
	    Department department =
	            departmentRepository.findByName(dto.getDepartmentName())
	                    .orElseThrow(() -> new RuntimeException("부서 없음"));

	    // 3) Staff_profile 생성
	    Staff_profile sp = new Staff_profile();
	    sp.setUser_account(user);
	    sp.setDepartment(department);
	    sp.setHire_date(dto.getHireDate());

	    staff_profileRepository.save(sp);

	    // 4) Role_code 결정
	    String roleValue = convertDepartmentToRole(department.getDepartment_code());

	    Role_code roleCode =
	            role_codeRepository.findById(roleValue)
	                    .orElseThrow(() -> new RuntimeException("역할 없음"));

	    // 5) User_role 저장
	    User_role ur = new User_role();
	    ur.setUser_account(user);
	    ur.setRole_code(roleCode);

	    user_roleRepository.save(ur);

	    return ResponseEntity.ok().body("ok");
	}
	// 인사 -> 직원등록 -> 진료과별로 role_code부여
	public static String convertDepartmentToRole(String deptName) {

        if (deptName == null) {
            return "STAFF";
        }

        // 의사과목 ⇒ DOCTOR
        if (deptName.equals("ENDO") ||
            deptName.equals("GS") ||
            deptName.equals("ORTHO") ||
            deptName.equals("PULMO")) {
            return "DOCTOR";
        }

        switch (deptName) {
            case "STAFF": return "STAFF";
            case "HR": return "HR";
            case "LOGIS": return "LOGIS";
            case "PHARM": return "PHARM";
            default: return "STAFF";
        }
    }
	
	// 스케줄 조회
	
	@GetMapping("/hr/mySchedule") // 인사 -> 스케줄 조회 by 은서
    public String getMySchedulePage(Model model) {
    	String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Vacation> vacation = vacationRepository.findVacationByUserId(user_id);
        List<Vacation_type> vacationTypes = vacation_typeRepository.findAll();
        
        model.addAttribute("vacation", vacation);
        model.addAttribute("vacationTypes", vacationTypes);
        model.addAttribute("username", user_id);
        return "hr/mySchedule";
    }
	
	@GetMapping("/hr/mySchedule/events") //원무 -> 스케줄 조회 -> 근무 스케줄 달력 by 은서
    @ResponseBody
    public List<Map<String, Object>> getMyScheduleEvents(
            @RequestParam(value="year",required = false) int year,
            @RequestParam(value="month",required = false) int month
    ) {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        List<ScheduleCalendarDTO> list =
                work_scheduleService.getStaffMonthlySchedule(userId, year, month);

        List<Map<String, Object>> events = new ArrayList<>();

        for (ScheduleCalendarDTO item : list) {
            Map<String, Object> event = new HashMap<>();
            
            StringBuilder title = new StringBuilder();
            title.append(item.getWorkName());

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            if (item.getStartTime() != null) {
                title.append("\n출근 ")
                     .append(item.getStartTime().format(timeFormatter));
            }

            if (item.getEndTime() != null) {
                title.append("\n퇴근 ")
                     .append(item.getEndTime().format(timeFormatter));
            }

            if (item.getStatusName() != null) {
                title.append(item.getStatusName());
            }

            
            event.put("title", title.toString());                 // ⭐ work_name
            event.put("start", item.getWorkDate().toString());     // ⭐ yyyy-MM-dd
            event.put("allDay", true);

            events.add(event);
        }

        return events;
    }
	
	@GetMapping("/hr/mySchedule/vacations") //인사 -> 스케줄 조회 -> 휴가 리스트 by 은서
    @ResponseBody
    public List<VacationDTO> getMyVacations() {
    	String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return vacationRepository.findVacationByUserId(userId)
            .stream()
            .map(v -> new VacationDTO(
                v.getVacation_id(),
                v.getVacation_type().getType_name(),
                v.getStart_date().toString(),
                v.getEnd_date().toString(),
                v.getStatus_code().getName(),
                v.getStatus_code().getStatus_code()
            ))
            .collect(Collectors.toList());
    }
    
    @GetMapping("/hr/mySchedule/vacation-types") //인사-> 스케줄 조회->휴가리스트 -> 검색창-> 분류 by 은서
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
    
    @GetMapping("/hr/mySchedule/vacation-status") //인사-> 스케줄 조회-> 휴가리스트-> 검색창-> 승인여부 by 은서
    @ResponseBody
    public List<Status_codeDTO> getVacationStatus() {
        return status_codeRepository.findByCategoryAndIsActiveTrue("vacation")
            .stream()
            .map(s -> new Status_codeDTO(
                s.getStatus_code(),
                s.getName()
            ))
            .toList();
    }

    @GetMapping("/hr/mySchedule/vacations/search") //인사-> 스케줄 조회-> 휴가리스트-> 검색창 by 은서
    @ResponseBody
    public List<VacationDTO> searchVacations(
        @RequestParam(value="typeCode",required = false) String typeCode,
        @RequestParam(value="statusCode",required = false) String statusCode,
        @RequestParam(value="date",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return vacationRepository.searchVacations(userId,
                (typeCode == null || typeCode.isBlank()) ? null : typeCode,
                (statusCode == null || statusCode.isBlank()) ? null : statusCode,
                date)
            .stream()
            .map(v -> new VacationDTO(
        		v.getVacation_id(),
                v.getVacation_type().getType_name(),
                v.getStart_date().toString(),
                v.getEnd_date().toString(),
                v.getStatus_code().getName(),
                v.getStatus_code().getStatus_code()
            ))
            .collect(Collectors.toList());
    }

    @PostMapping("/hr/mySchedule/vacation/{vacationId}/cancel") //인사-> 스케줄 조회-> 휴가리스트 -> 휴가취소 by 은서
    @ResponseBody
    public void cancelVacation(@PathVariable("vacationId") Long vacationId) {
    	Vacation vacation = vacationRepository.findById(vacationId)
                .orElseThrow(() -> new RuntimeException("휴가 없음"));

        Status_code cancelStatus = status_codeRepository.findByCode("VAC_CANCEL_REQUESTED")
                .orElseThrow(() -> new RuntimeException("상태코드 없음"));

        vacation.setStatus_code(cancelStatus);
        vacationRepository.save(vacation);
    }
    
    // 인사 -> 휴가 신청 by 은서
    @GetMapping("/hr/applyVacation")
    public String applyVacation(Model model) {
        String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Vacation> vacation = vacationRepository.findVacationByUserId(user_id);
        List<Vacation_type> vacationTypes = vacation_typeRepository.findAll();
        
        model.addAttribute("vacation", vacation);
        model.addAttribute("vacationTypes", vacationTypes);
        model.addAttribute("username", user_id);
        
        return "hr/applyVacation";
    }
    
    // 인사 -> 휴가 신청 -> 폼 제출 by 은서
    @PostMapping("/hr/applyVacation")
    @ResponseBody // 반환값을 JSON형태(키-값)로 전달
    public Map<String,Object> applyVacation(@RequestBody Map<String,String> body) {
        Map<String,Object> result = new HashMap<>(); // 키-값 
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String user_id = auth.getName(); //로그인 한 사용자의 user_id

            User_account user = user_accountRepository.findByUser_id(user_id)
                    .orElseThrow(() -> new RuntimeException("사용자 없음"));

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
    
    //mypage
    //인사->마이페이지->비밀번호 확인
  	@GetMapping("/hr/verifyPassword")
  	public String verifyPasswordForm() {
  	    return "hr/verifyPassword";
  	}

  	@PostMapping("/hr/verifyPassword")
  	public String verifyPassword(@RequestParam("password") String password,
  	                             Model model) {

  	    String userId = SecurityContextHolder.getContext().getAuthentication().getName();

  	    // User_account repository 주입되어 있어야 함
  	    User_account user = user_accountRepository.findByUser_id(userId)
  	            .orElse(null);

  	    if (user == null) {
  	        model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
  	        return "hr/verifyPassword";
  	    }

  	    // 평문이면 equals
  	    // 암호화 되어 있으면 matches 사용
  	    if (!passwordEncoder.matches(password, user.getPassword())) {
  	        model.addAttribute("error", "비밀번호가 올바르지 않습니다.");
  	        return "hr/verifyPassword";
  	    }

  	    // 성공 → 마이페이지 이동
  	    return "redirect:/hr/hrMyPage";
  	}

  	
  	@GetMapping("/hr/hrMyPage") //의사->마이페이지
      public String doctorMyPage(Model model) {
  		String userId = SecurityContextHolder.getContext().getAuthentication().getName();

  	    User_account account = user_accountRepository.findById(userId).orElseThrow();

  	    Staff_profile profile =
  	            staff_profileRepository.findByUser_account_User_id(userId)
  	            .orElse(new Staff_profile());   // 없으면 빈 객체

  	    model.addAttribute("account", account);
  	    model.addAttribute("profile", profile);
  		return "hr/hrMyPage"; 
      }
  	
  	@PutMapping("/hr/hrMyPage")
  	@ResponseBody
  	public String updateDoctor(@RequestBody MyPageDTO dto) {

  	    String userId = SecurityContextHolder.getContext().getAuthentication().getName();

  	    User_account account = user_accountRepository.findById(userId).orElseThrow();

  	    // 비밀번호가 비어있지 않을 때만 변경
  	    if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {

  	        // 비밀번호 중복 검사
  	        if (user_accountRepository.existsByPassword(dto.getPassword())) {
  	            return "이미 사용 중인 비밀번호입니다.";
  	        }

  	        account.setPassword(dto.getPassword());
  	    }

  	    user_accountRepository.save(account);

  	    Staff_profile profile =
  	            staff_profileRepository.findByUser_account_User_id(userId)
  	            .orElse(new Staff_profile());

  	    profile.setLicense_number(dto.getLicense());
  	    profile.setBank_name(dto.getBank());
  	    profile.setBank_account(dto.getAccount());

  	    staff_profileRepository.save(profile);

  	    return "수정 완료";
  	}

}
