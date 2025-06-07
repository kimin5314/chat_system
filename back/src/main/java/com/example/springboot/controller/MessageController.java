package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.ConversationDto;
import com.example.springboot.dto.MessageDto;
import com.example.springboot.dto.SendMessageDto;
import com.example.springboot.service.MessageService;
import com.example.springboot.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public Result sendMessage(@RequestBody SendMessageDto sendMessageDto, 
                             HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            Integer senderId = JwtUtil.getUserIdFromToken(token);
            
            MessageDto message = messageService.sendMessage(senderId, sendMessageDto);
            return Result.success(message);
        } catch (Exception e) {
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }

    @GetMapping("/conversation/{friendId}")
    public Result getConversation(@PathVariable Integer friendId, 
                                 HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            Integer userId = JwtUtil.getUserIdFromToken(token);
            
            List<MessageDto> messages = messageService.getConversation(userId, friendId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取聊天记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/conversations")
    public Result getConversations(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            Integer userId = JwtUtil.getUserIdFromToken(token);
            
            List<ConversationDto> conversations = messageService.getConversations(userId);
            return Result.success(conversations);
        } catch (Exception e) {
            return Result.error("获取对话列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/mark-read/{senderId}")
    public Result markMessagesAsRead(@PathVariable Integer senderId, 
                                   HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            Integer receiverId = JwtUtil.getUserIdFromToken(token);
            
            messageService.markMessagesAsRead(receiverId, senderId);
            return Result.success("消息已标记为已读");
        } catch (Exception e) {
            return Result.error("标记已读失败: " + e.getMessage());
        }
    }

    @GetMapping("/unread-count/{senderId}")
    public Result getUnreadCount(@PathVariable Integer senderId, 
                               HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            Integer receiverId = JwtUtil.getUserIdFromToken(token);
            
            Long count = messageService.getUnreadCount(receiverId, senderId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取未读消息数失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public Result getMessageStats(@RequestParam(value = "date", required = false) String date,
                                 HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            Integer userId = JwtUtil.getUserIdFromToken(token);
            
            // If no date provided, use today
            if (date == null || date.isEmpty()) {
                date = java.time.LocalDate.now().toString();
            }
            
            Long messageCount = messageService.getMessageCountByDate(userId, date);
            
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("count", messageCount);
            stats.put("date", date);
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取消息统计失败: " + e.getMessage());
        }
    }
}
