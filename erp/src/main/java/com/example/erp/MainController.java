package com.example.erp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {
	@GetMapping("/main")
	public String getMain() {
	    return "main"; // templates/kcd-list.html
	}

}
