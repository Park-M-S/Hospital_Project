package com.hospital.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.service.EmergencyApiService;
import com.hospital.service.EmergencyMockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class EmergencyApiWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    
    private EmergencyMockService emergencyMockApiService;
    
    @Autowired
    public void setEmergencyApiService(EmergencyMockService emergencyMockApiService) {
        this.emergencyMockApiService = emergencyMockApiService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket 연결됨: " + session.getId() + ", 총 연결수: " + sessions.size());
        
        // 첫 접속자면 스케줄러 시작
        emergencyMockApiService.onMockWebSocketConnected();
        
        // 초기 데이터 전송 (캐시된 데이터가 있으면)
        try {
            JsonNode initialData = emergencyMockApiService.getMockEmergencyRoomData();
            if (initialData != null && initialData.size() > 0) {
                session.sendMessage(new TextMessage(initialData.toString()));
                System.out.println("초기 데이터 전송 완료: " + session.getId());
            } else {
                System.out.println("전송할 초기 데이터 없음: " + session.getId());
            }
        } catch (Exception e) {
            System.err.println("초기 데이터 전송 실패: " + session.getId() + ", 오류: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket 연결 해제: " + session.getId() + ", 총 연결수: " + sessions.size());
        
        // 마지막 접속자가 나가면 스케줄러 중지
        emergencyMockApiService.stopMockScheduler();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket 에러: " + session.getId());
        exception.printStackTrace();
        
        sessions.remove(session);
        
        // 에러로 인한 연결 해제도 스케줄러 중지 확인
        emergencyMockApiService.stopMockScheduler();
    }

    /**
     * 모든 연결된 클라이언트에게 데이터 브로드캐스트
     */
    public void broadcastEmergencyRoomData(String data) {
        if (data == null || sessions.isEmpty()) {
            return;
        }

        synchronized (sessions) {
            // 닫힌 세션 제거
            sessions.removeIf(session -> !session.isOpen());

            int successCount = 0;
            for (WebSocketSession session : new HashSet<>(sessions)) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(data));
                        successCount++;
                    }
                } catch (IOException e) {
                    System.err.println("메시지 전송 실패: " + session.getId());
                    sessions.remove(session);
                }
            }
            
            System.out.println("브로드캐스트 완료. 성공한 세션 수: " + successCount + "/" + sessions.size());
        }
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
                    System.err.println("WebSocket 세션 종료 실패: " + session.getId());
                }
            }
            sessions.clear();
        }
        System.out.println("✅ 모든 WebSocket 연결 종료 완료");
    }

    /**
     * 현재 연결된 세션 수 조회 (유효하지 않은 세션 정리 포함)
     */
    public int getConnectedSessionCount() {
        synchronized (sessions) {
            // 유효하지 않은 세션 정리
            sessions.removeIf(session -> !session.isOpen());
            return sessions.size();
        }
    }
    
    /**
     * 현재 연결 상태 정보 반환
     */
    public String getConnectionStatus() {
        int validSessions = getConnectedSessionCount();
        return String.format("총 세션: %d, 유효 세션: %d", sessions.size(), validSessions);
    }
}