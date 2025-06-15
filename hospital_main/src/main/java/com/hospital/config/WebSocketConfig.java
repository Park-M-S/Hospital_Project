package com.hospital.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.emergency.service.EmergencyApiService;

// @Component 제거 - @Bean으로 관리되므로
public class EmergencyApiWebSocketHandler extends TextWebSocketHandler {
    
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    
    @Autowired
    @Lazy  // 순환 참조 방지를 위한 지연 로딩
    private EmergencyApiService emergencyApiService;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        
        // 연결 시 초기 데이터 전송
        try {
            JsonNode initialData = emergencyApiService.getEmergencyRoomData();
            if (initialData != null && !initialData.isEmpty()) {
                session.sendMessage(new TextMessage(initialData.toString()));
                System.out.println("✅ 초기 데이터 전송 완료: " + session.getId());
            }
        } catch (Exception e) {
            System.err.println("❌ 초기 데이터 전송 실패: " + e.getMessage());
        }
        
        System.out.println("✅ WebSocket 연결됨: " + session.getId() + ", 총 연결수: " + sessions.size());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("📡 WebSocket 연결 해제: " + session.getId() + ", 총 연결수: " + sessions.size());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("❌ WebSocket 에러: " + session.getId());
        exception.printStackTrace();
        sessions.remove(session);
    }
    
    // 모든 연결된 클라이언트에게 데이터 브로드캐스트
    public void broadcastEmergencyRoomData(String data) {
        if (data == null || sessions.isEmpty()) {
            return;
        }
        
        synchronized (sessions) {
            // 닫힌 세션 제거
            sessions.removeIf(session -> !session.isOpen());
            
            for (WebSocketSession session : new HashSet<>(sessions)) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(data));
                    }
                } catch (IOException e) {
                    System.err.println("❌ 메시지 전송 실패: " + session.getId());
                    sessions.remove(session);
                }
            }
        }
        
        System.out.println("📡 브로드캐스트 완료. 전송된 세션 수: " + sessions.size());
    }
    
    /**
     * 모든 WebSocket 연결 강제 종료
     */
    public void closeAllSessions() {
        synchronized (sessions) {
            for (WebSocketSession session : new HashSet<>(sessions)) {
                try {
                    if (session.isOpen()) {
                        session.close(CloseStatus.NORMAL);
                    }
                } catch (IOException e) {
                    System.err.println("❌ WebSocket 세션 종료 실패: " + session.getId());
                }
            }
            sessions.clear();
        }
        System.out.println("✅ 모든 WebSocket 연결 종료 완료");
    }
    
    public int getConnectedSessionCount() {
        return sessions.size();
    }
}
