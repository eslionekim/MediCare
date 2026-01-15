package com.example.erp.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Department.Department;
import com.example.erp.Department.DepartmentRepository;
import com.example.erp.Role_code.Role_codeRepository;
import com.example.erp.Staff_profile.MyPageDTO;
import com.example.erp.Staff_profile.Staff_profile;
import com.example.erp.Staff_profile.Staff_profileRepository;
import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;
import com.example.erp.User_role.User_roleRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class doctorController {
	private final Staff_profileRepository staff_profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final User_accountRepository user_accountRepository;
    
	//의사->마이페이지->비밀번호 확인
	@GetMapping("/doctor/verifyPassword")
	public String verifyPasswordForm() {
	    return "doctor/verifyPassword";
	}

	@PostMapping("/doctor/verifyPassword")
	public String verifyPassword(@RequestParam("password") String password,
	                             Model model) {

	    String userId = SecurityContextHolder.getContext().getAuthentication().getName();

	    // User_account repository 주입되어 있어야 함
	    User_account user = user_accountRepository.findByUser_id(userId)
	            .orElse(null);

	    if (user == null) {
	        model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
	        return "doctor/verifyPassword";
	    }

	    // 👉 평문이면 equals
	    // 👉 암호화 되어 있으면 matches 사용
	    if (!passwordEncoder.matches(password, user.getPassword())) {
	        model.addAttribute("error", "비밀번호가 올바르지 않습니다.");
	        return "doctor/verifyPassword";
	    }

	    // 성공 → 마이페이지 이동
	    return "redirect:/doctor/doctorMyPage";
	}

	
	@GetMapping("/doctor/doctorMyPage") //의사->마이페이지
    public String doctorMyPage(Model model) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();

	    User_account account = user_accountRepository.findById(userId).orElseThrow();

	    Staff_profile profile =
	            staff_profileRepository.findByUser_account_User_id(userId)
	            .orElse(new Staff_profile());   // 없으면 빈 객체

	    model.addAttribute("account", account);
	    model.addAttribute("profile", profile);
		return "doctor/doctorMyPage"; 
    }
	
	@PutMapping("/doctor/doctorMyPage")
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
