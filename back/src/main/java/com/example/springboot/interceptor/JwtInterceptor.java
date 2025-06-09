package com.example.springboot.interceptor;

import com.example.springboot.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        String token = authHeader.substring(7);

        try {
            if (!JwtUtil.validateToken(token)) {
                response.setStatus(401);
                return false;
            }
            String username = JwtUtil.getUsernameFromToken(token);
            request.setAttribute("username", username);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }
}