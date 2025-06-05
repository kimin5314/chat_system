package com.example.springboot.config;

import com.example.springboot.websocket.FriendRequestHandler;
import com.example.springboot.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;
    
    @Autowired
    private FriendRequestHandler friendRequestHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Allow all origins for WebSocket handshakes
        registry.addHandler(friendRequestHandler, "/ws/friend")
                .setAllowedOriginPatterns("*");
        registry.addHandler(chatWebSocketHandler, "/chat")
                .setAllowedOriginPatterns("*");
    }
}
