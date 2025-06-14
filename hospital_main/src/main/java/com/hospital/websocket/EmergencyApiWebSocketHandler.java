package com.hospital.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.emergency.service.EmergencyApiService;

@Component
public class EmergencyApiWebSocketHandler extends TextWebSocketHandler {
    
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    
    @Autowired
    private EmergencyApiService emergencyApiService;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        
        // 연결 시 초기 데이터 전송
        try {
            JsonNode initialData = emergencyApiService.getEmergencyRoomData();
            if (initialData != null && !initialData.isEmpty()) {
                session.sendMessage(new TextMessage(initialData.toString()));
            }
        } catch (Exception e) {
            System.err.println("초기 데이터 전송 실패: " + e.getMessage());
        }
        
        System.out.println("WebSocket 연결됨: " + session.getId() + ", 총 연결수: " + sessions.size());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket 연결 해제: " + session.getId() + ", 총 연결수: " + sessions.size());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket 에러: " + session.getId());
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
            
            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(data));
                    }
                } catch (IOException e) {
                    System.err.println("메시지 전송 실패: " + session.getId());
                    e.printStackTrace();
                    sessions.remove(session);
                }
            }
        }
        
        System.out.println("브로드캐스트 완료. 전송된 세션 수: " + sessions.size());
    }
    
    public int getConnectedSessionCount() {
        return sessions.size();
    }
}