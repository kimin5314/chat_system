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
    private AppProperties appProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Use configured frontend URL for WebSocket CORS
        String frontendUrl = appProperties.getFrontendUrl();
        
        registry.addHandler(new FriendRequestHandler(), "/ws/friend")
                .setAllowedOrigins(frontendUrl);
        
        registry.addHandler(chatWebSocketHandler, "/chat")
                .setAllowedOrigins(frontendUrl);
    }
}
