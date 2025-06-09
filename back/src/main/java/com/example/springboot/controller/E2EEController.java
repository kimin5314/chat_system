package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.E2EEKeyDto;
import com.example.springboot.dto.EncryptedMessageDto;
import com.example.springboot.dto.MessageDto;
import com.example.springboot.service.E2EEService;
import com.example.springboot.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/e2ee")
public class E2EEController {

    @Autowired
    private E2EEService e2eeService;

    /**
     * Update user's E2EE settings (public key and enable/disable E2EE)
     */
    @PostMapping("/settings")
    public Result updateE2EESettings(@RequestBody E2EEKeyDto e2eeKeyDto, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            String username = JwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return Result.error("无效的token");
            }

            return e2eeService.updateE2EESettings(username, e2eeKeyDto);
        } catch (Exception e) {
            return Result.error("更新E2EE设置失败: " + e.getMessage());
        }
    }

    /**
     * Get user's public key for encryption
     */
    @GetMapping("/public-key/{userId}")
    public Result getUserPublicKey(@PathVariable Integer userId, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            String username = JwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return Result.error("无效的token");
            }

            return e2eeService.getUserPublicKey(userId);
        } catch (Exception e) {
            return Result.error("获取公钥失败: " + e.getMessage());
        }
    }

    /**
     * Send encrypted message
     */
    @PostMapping("/send-encrypted")
    public Result sendEncryptedMessage(@RequestBody EncryptedMessageDto encryptedMessageDto, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            String username = JwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return Result.error("无效的token");
            }

            return e2eeService.sendEncryptedMessage(username, encryptedMessageDto);
        } catch (Exception e) {
            return Result.error("发送加密消息失败: " + e.getMessage());
        }
    }

    /**
     * Check E2EE status between two users
     */
    @GetMapping("/status/{user1Id}/{user2Id}")
    public Result checkE2EEStatus(@PathVariable Integer user1Id, @PathVariable Integer user2Id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            String username = JwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return Result.error("无效的token");
            }

            return e2eeService.checkE2EEStatus(user1Id, user2Id);
        } catch (Exception e) {
            return Result.error("检查E2EE状态失败: " + e.getMessage());
        }
    }
}
