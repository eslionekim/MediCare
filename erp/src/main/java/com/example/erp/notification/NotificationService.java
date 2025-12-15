package com.example.erp.notification;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;



@Service 
@RequiredArgsConstructor 
public class NotificationService { 
	private final SimpMessagingTemplate messagingTemplate; //웹소켓을 통해 메시지전송 
	
	// 특정 유저에게 실시간 1:1 알림 전송
    public void notifyUser(String user_id, String title, String content) {
        // 메시지 객체 만들어서 보내기
        Map<String, String> msg = new HashMap<>();
        msg.put("title", title);
        msg.put("content", content);

        // STOMP /user/{user_id}/queue/notify 로 전송
        messagingTemplate.convertAndSendToUser(
        	    user_id,            // 로그인한 직원의 Principal 이름
        	    "/queue/notify",    // 목적지 (user 접두사는 자동)
        	    msg                 // 메시지 객체
        );
        //messagingTemplate : 특정 주소로 메시지 전송
    }
    
    // HR 전체 알림
    public void notifyHR(String title, String content) {
        Map<String, String> msg = new HashMap<>();
        msg.put("title", title);
        msg.put("content", content);
        messagingTemplate.convertAndSend("/topic/hr", msg); // hr전체로 /topic/hr 구독해야 수신
    }
}

