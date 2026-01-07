package com.example.erp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 스프링 설정 클래스로 인식하게 함
@EnableWebSecurity // 웹 보안 활성화, 스프링 시큐리티 기능 사용 , 필터 체인과 로그인/권한 기능을 사용 가능
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    
    
    @Bean //보안 정책
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomLoginSuccessHandler successHandler) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) //CSRF 보호 비활성화 (개발/테스트용) role_code대로
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/logout").permitAll()
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/pharm/**").hasRole("PHARM")
                .requestMatchers("/logis/**").hasRole("logis")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/hr/**").hasRole("HR")
                .requestMatchers("/patients/**").hasRole("STAFF")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") //로그인 페이지 설정
                .successHandler(successHandler) //역할별 리다이렉트
                .permitAll() // 모두 접근 허용
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    @Bean //인증 관리자 Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService) //로그인 시 UserDetailsService와 PasswordEncoder를 연결
                   .passwordEncoder(passwordEncoder());

        return authBuilder.build(); //설정 후 build()호출
    }


    @Bean
    public PasswordEncoder passwordEncoder() { //User_account 테이블에 저장할 때 반드시 BCrypt로 암호화
        // return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();
    }
    
}

