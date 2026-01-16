package com.example.erp.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;

@ControllerAdvice(annotations = Controller.class)
public class UserInfoAdvice {
    private final User_accountRepository userAccountRepository;

    public UserInfoAdvice(User_accountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @ModelAttribute
    public void addUserInfo(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            model.addAttribute("userId", "");
            model.addAttribute("userName", "");
            return;
        }

        String userId = auth.getName();
        if ("anonymousUser".equalsIgnoreCase(userId)) {
            model.addAttribute("userId", "");
            model.addAttribute("userName", "");
            return;
        }

        Optional<User_account> user = userAccountRepository.findByUser_id(userId);
        String userName = user.map(User_account::getName).orElse("알 수 없음");

        model.addAttribute("userId", userId);
        model.addAttribute("userName", userName);
    }
}
