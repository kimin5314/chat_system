package com.example.springboot.websocket;

import com.example.springboot.dto.MessageDto;
import com.example.springboot.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Changed: Use Map<userId, Set<WebSocketSession>> to support multiple sessions per user
    private static final Map<Integer, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    // Keep track of session to userId mapping for quick lookup
    private static final Map<WebSocketSession, Integer> sessionToUser = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Integer userId = getUserIdFromToken(session);
        if (userId != null) {
            // Add session to user's session set
            userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
            sessionToUser.put(session, userId);
            
            System.out.println("Chat WebSocket connected for user: " + userId + " (Session: " + session.getId() + ")");
            System.out.println("User " + userId + " now has " + userSessions.get(userId).size() + " active sessions");
            
            // Notify other users that this user is online (only if this is their first session)
            if (userSessions.get(userId).size() == 1) {
                broadcastUserStatus(userId, true);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer userId = sessionToUser.remove(session);
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                
                System.out.println("Chat WebSocket disconnected for user: " + userId + " (Session: " + session.getId() + ")");
                System.out.println("User " + userId + " now has " + sessions.size() + " active sessions");
                
                // Only mark user as offline if they have no more active sessions
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                    broadcastUserStatus(userId, false);
                    System.out.println("User " + userId + " is now completely offline");
                }
            }
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        // Handle heartbeats
        if ("ping".equals(payload)) {
            // Respond with pong immediately
            session.sendMessage(new TextMessage("pong"));
            return;
        }
        
        // Handle other messages
        System.out.println("Received WebSocket message: " + payload);
        
        try {
            // Try to parse as JSON and process accordingly
            // Your existing message handling logic
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public void sendMessageToUser(Integer userId, MessageDto messageDto) throws Exception {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "NEW_MESSAGE");
            wsMessage.put("data", messageDto);
            
            String jsonMessage = objectMapper.writeValueAsString(wsMessage);
            
            // Send to all active sessions for this user
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(jsonMessage));
                    } catch (Exception e) {
                        System.err.println("Failed to send message to session " + session.getId() + ": " + e.getMessage());
                        // Remove broken session
                        sessions.remove(session);
                        sessionToUser.remove(session);
                    }
                }
            }
        }
    }

    public void sendNotificationToUser(Integer userId, String type, Object data) throws Exception {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", type);
            wsMessage.put("data", data);
            
            String jsonMessage = objectMapper.writeValueAsString(wsMessage);
            
            // Send to all active sessions for this user
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(jsonMessage));
                    } catch (Exception e) {
                        System.err.println("Failed to send notification to session " + session.getId() + ": " + e.getMessage());
                        // Remove broken session
                        sessions.remove(session);
                        sessionToUser.remove(session);
                    }
                }
            }
        }
    }

    public boolean isUserOnline(Integer userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        
        // Check if user has at least one open session
        return sessions.stream().anyMatch(WebSocketSession::isOpen);
    }

    private void broadcastUserStatus(Integer userId, boolean isOnline) {
        Map<String, Object> statusMessage = new HashMap<>();
        statusMessage.put("type", isOnline ? "USER_ONLINE" : "USER_OFFLINE");
        statusMessage.put("data", Map.of("userId", userId, "isOnline", isOnline));

        try {
            String jsonMessage = objectMapper.writeValueAsString(statusMessage);
            
            // Broadcast to all active sessions of all users
            for (Set<WebSocketSession> userSessionSet : userSessions.values()) {
                for (WebSocketSession session : userSessionSet) {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(jsonMessage));
                        } catch (Exception e) {
                            System.err.println("Failed to broadcast to session " + session.getId() + ": " + e.getMessage());
                            // Remove broken session
                            Integer sessionUserId = sessionToUser.remove(session);
                            if (sessionUserId != null) {
                                Set<WebSocketSession> sessions = userSessions.get(sessionUserId);
                                if (sessions != null) {
                                    sessions.remove(session);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to broadcast user status: " + e.getMessage());
        }
    }

    private Integer getUserIdFromToken(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("token=")) {
                String token = query.split("token=")[1].split("&")[0];
                return JwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            System.err.println("Failed to extract user ID from token: " + e.getMessage());
        }
        return null;
    }
}
