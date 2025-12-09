package com.example.erp.User_account;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class User_accountController {
	@GetMapping("/login")
    public String loginPage() {
        return "login"; // templates/login.html
    }
}
