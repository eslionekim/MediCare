package com.example.erp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;
import com.example.erp.User_role.User_role;


@Service
@RequiredArgsConstructor  // final 필드 자동 생성자 주입
public class UserDetailsServiceImpl implements UserDetailsService { //로그인 시 사용자 정보를 조회할 서비스

    private final User_accountRepository user_accountRepository;

    @Override
    public UserDetails loadUserByUsername(String user_id) throws UsernameNotFoundException {
    	System.out.println("loadUserByUsername 호출: " + user_id); 
    	
    	User_account user = user_accountRepository.findByUser_id(user_id) // 아이디로 사용자조회
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + user_id));

        // UserDetails 구현체 반환
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUser_id()) //UserDetail에 로그인 id 설정
                .password(user.getPassword()) //UserDetail에 로그인 pw 설정
                .roles(
                	    user.getUser_role().stream()
                	        .filter(r -> r.getRole_code() != null)
                	        .map((User_role r) -> r.getRole_code().getRole_code())
                	        .toArray(String[]::new)
                	)


                .build(); //UserDetails 객체 생성 및 반환
    }
    
}
