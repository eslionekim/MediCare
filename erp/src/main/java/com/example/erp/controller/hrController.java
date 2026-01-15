package com.example.erp.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.example.erp.Staff_profile.Staff_profile;
import com.example.erp.Staff_profile.Staff_profileRepository;
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
import com.example.erp.Warehouse.WarehouseRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class hrController {
	private final Staff_profileRepository staff_profileRepository;
	private final DepartmentRepository departmentRepository;
	private final User_roleRepository user_roleRepository;
	private final User_accountRepository user_accountRepository;
	private final Role_codeRepository role_codeRepository;

	@GetMapping("/hr/employee")
	public String employeeList(
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
}
