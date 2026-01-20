package com.example.erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class passwordController {

	@Autowired
    private BCryptPasswordEncoder encoder;


    @GetMapping("/test/bcrypt")
	@ResponseBody
	public String testBcrypt() {
	    System.out.println(encoder.encode("ssss"));
	    return "check console";
	}
}
