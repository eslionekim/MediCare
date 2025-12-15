package com.example.erp.security;

import org.springframework.messaging.simp.config.MessageBrokerRegistry;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration 
@EnableWebSocketMessageBroker //웹소켓 메시지 브로커 활성화 
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer { 
	@Override 
	public void registerStompEndpoints(StompEndpointRegistry registry) { //by 은서
		registry.addEndpoint("/ws") // 웹소컷 연결을 열기 위해 접속할 엔드포인트
				.withSockJS();  //브라우저가 웹소켓 지원안할때
		} 
	@Override 
	public void configureMessageBroker(MessageBrokerRegistry registry) { //by 은서
		registry.enableSimpleBroker("/topic", "/queue"); //구독 채널
		registry.setApplicationDestinationPrefixes("/app"); // MessageMapping 시 /app을 앞에 강제로 붙임
		registry.setUserDestinationPrefix("/user"); // 자동으로 /user/아이디.. 형식으로 변환
	} 
}