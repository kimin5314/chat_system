package com.example.springboot.interceptor;

import com.example.springboot.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("请求头Authorization: " + authHeader); // 调试日志

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Token缺失或格式错误");
            response.setStatus(401);
            return false;
        }

        String token = authHeader.substring(7);
        System.out.println("提取的Token: " + token); // 调试日志

        try {
            if (!JwtUtil.validateToken(token)) {
                System.out.println("Token验证失败");
                response.setStatus(401);
                return false;
            }
            String username = JwtUtil.getUsernameFromToken(token);
            request.setAttribute("username", username);
            return true;
        } catch (Exception e) {
            System.out.println("Token解析异常: " + e.getMessage()); // 捕获具体异常
            response.setStatus(401);
            return false;
        }
    }
}