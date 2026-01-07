package com.example.erp.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        // role_code 대로
        // 역할에 따라 페이지 이동
        String redirectURL = request.getContextPath();
        // 의사일 경우
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
            redirectURL += "/doctor/todayVisits";
        }
        // 인사일 경우
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_HR"))) {
            redirectURL += "/hr/vacationList";
        }
        // 원무일 경우
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
            redirectURL += "/patients";
        }
        // 물류일 경우
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_logis"))) {
            redirectURL += "/logis/itemRequest";
        }
        // 관리자일 경우
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_logis"))) {
            redirectURL += "/admin/dashboard";
        } else {
            redirectURL += "/home"; // 기본
        }

        response.sendRedirect(redirectURL);
    }
}