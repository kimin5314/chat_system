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
    private ChatWebSocketHandler chatWebSocketHandler;    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new FriendRequestHandler(), "/ws/friend")
                .setAllowedOriginPatterns("*"); // 允许所有域名进行请求
        
        registry.addHandler(chatWebSocketHandler, "/chat")
                .setAllowedOriginPatterns("*"); // 允许所有域名进行请求
    }
}
