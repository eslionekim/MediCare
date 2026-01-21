package com.example.erp.User_account;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.erp.Department.Department;
import com.example.erp.Department.DepartmentRepository;
import com.example.erp.Role_code.Role_code;
import com.example.erp.Role_code.Role_codeRepository;
import com.example.erp.Staff_profile.Staff_profile;
import com.example.erp.Staff_profile.Staff_profileRepository;
import com.example.erp.User_role.User_role;
import com.example.erp.User_role.User_roleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {
	private final Staff_profileRepository staff_profileRepository;
	private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final User_roleRepository user_roleRepository;
	private final User_accountRepository user_accountRepository;
	private final Role_codeRepository role_codeRepository;

	public void saveEmployee(EmployeeCreateDTO dto) {

	    // 1) User_account 생성
	    User_account user = new User_account();
	    user.setUser_id(dto.getUserId());
	    user.setName(dto.getName());
	    user.setPassword(passwordEncoder.encode(dto.getPassword()));
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

	}
	
	// 진료과 → Role 매핑
    private String convertDepartmentToRole(String deptCode) {

        if (deptCode == null) {
            return "STAFF";
        }

        // 의사 진료과
        if (deptCode.equals("ENDO") ||
            deptCode.equals("GS") ||
            deptCode.equals("ORTHO") ||
            deptCode.equals("PULMO")) {
            return "DOCTOR";
        }

        switch (deptCode) {
            case "STAFF": return "STAFF";
            case "HR":    return "HR";
            case "LOGIS": return "LOGIS";
            case "PHARM": return "PHARM";
            default:      return "STAFF";
        }
    }
}
