package com.example.springboot.websocket;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import com.example.springboot.utils.JwtUtil;

public class FriendRequestHandler extends TextWebSocketHandler {
    // Changed: Use Map<userId, Set<WebSocketSession>> to support multiple sessions per user
    private static final Map<Integer, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    // Keep track of session to userId mapping for quick lookup
    private static final Map<WebSocketSession, Integer> sessionToUser = new ConcurrentHashMap<>();    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Integer userId = getUserIdFromToken(session);
        if (userId != null) {
            // Add session to user's session set
            userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
            sessionToUser.put(session, userId);
            
            System.out.println("Friend WebSocket connected for user: " + userId + " (Session: " + session.getId() + ")");
            System.out.println("User " + userId + " now has " + userSessions.get(userId).size() + " active friend sessions");
        } else {
            System.out.println("Friend WebSocket connected but couldn't extract userId from token");
        }
    }
    
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        // Handle heartbeats
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }
        
        // Process other messages
        System.out.println("Received Friend WebSocket message: " + payload);
    }    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer userId = sessionToUser.remove(session);
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                
                System.out.println("Friend WebSocket disconnected for user: " + userId + " (Session: " + session.getId() + ")");
                System.out.println("User " + userId + " now has " + sessions.size() + " active friend sessions");
                
                // Remove user entry if no more sessions
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                    System.out.println("User " + userId + " has no more friend WebSocket sessions");
                }
            }
        }
    }    public void notifyUser(Integer targetUserId, String message) throws Exception {
        Set<WebSocketSession> sessions = userSessions.get(targetUserId);
        if (sessions != null && !sessions.isEmpty()) {
            // Send to all active sessions for this user
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (Exception e) {
                        System.err.println("Failed to send friend notification to session " + session.getId() + ": " + e.getMessage());
                        // Remove broken session
                        sessions.remove(session);
                        sessionToUser.remove(session);
                    }
                }
            }
        }
    }

    private Integer getUserIdFromToken(WebSocketSession session) {
        try {
            String token = session.getUri().getQuery().split("=")[1];
            return JwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
