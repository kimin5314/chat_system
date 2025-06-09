package com.example.springboot.service;

import com.example.springboot.common.Result;
import com.example.springboot.dto.E2EEKeyDto;
import com.example.springboot.dto.EncryptedMessageDto;
import com.example.springboot.dto.MessageDto;
import com.example.springboot.entity.Message;
import com.example.springboot.entity.User;
import com.example.springboot.Enum.MessageType;
import com.example.springboot.repository.MessageRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class E2EEService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    /**
     * Update user's E2EE settings
     */
    public Result updateE2EESettings(String username, E2EEKeyDto e2eeKeyDto) {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }

            user.setPublicKey(e2eeKeyDto.getPublicKey());
            user.setE2eeEnabled(e2eeKeyDto.getE2eeEnabled());
            userRepository.save(user);

            return Result.success("E2EE设置更新成功");
        } catch (Exception e) {
            return Result.error("更新E2EE设置失败: " + e.getMessage());
        }
    }

    /**
     * Get user's public key for encryption
     */
    public Result getUserPublicKey(Integer userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return Result.error("用户不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("publicKey", user.getPublicKey());
            result.put("e2eeEnabled", user.getE2eeEnabled());
            result.put("username", user.getUsername());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取公钥失败: " + e.getMessage());
        }
    }

    /**
     * Send encrypted message
     */
    public Result sendEncryptedMessage(String senderUsername, EncryptedMessageDto encryptedMessageDto) {
        try {
            User sender = userRepository.findByUsername(senderUsername);
            if (sender == null) {
                return Result.error("发送者不存在");
            }

            User receiver = userRepository.findById(encryptedMessageDto.getReceiverId()).orElse(null);
            if (receiver == null) {
                return Result.error("接收者不存在");
            }

            // Validate E2EE parameters
            if (encryptedMessageDto.getEncryptedContent() == null || encryptedMessageDto.getEncryptedContent().trim().isEmpty()) {
                return Result.error("缺少加密内容");
            }
            if (encryptedMessageDto.getEncryptedAESKey() == null || encryptedMessageDto.getEncryptedAESKey().trim().isEmpty()) {
                return Result.error("缺少加密AES密钥");
            }
            if (encryptedMessageDto.getIv() == null || encryptedMessageDto.getIv().trim().isEmpty()) {
                return Result.error("缺少初始化向量");
            }

            // Create encrypted message
            Message message = new Message();
            message.setSenderId(sender.getId());
            message.setReceiverId(receiver.getId());
            message.setContent(encryptedMessageDto.getEncryptedContent()); // Store encrypted content in content field
            message.setMessageType(MessageType.ENCRYPTED); // Use message type to distinguish
            message.setEncryptedAESKey(encryptedMessageDto.getEncryptedAESKey()); // Store encrypted AES key
            message.setIv(encryptedMessageDto.getIv()); // Store initialization vector
            message.setCreatedAt(LocalDateTime.now());
            message.setUpdatedAt(LocalDateTime.now());

            Message savedMessage = messageRepository.save(message);

            // Create MessageDto for response
            MessageDto messageDto = new MessageDto();
            messageDto.setId(savedMessage.getId());
            messageDto.setSenderId(savedMessage.getSenderId());
            messageDto.setReceiverId(savedMessage.getReceiverId());
            messageDto.setContent(savedMessage.getContent()); // Return encrypted content in content field
            messageDto.setMessageType(savedMessage.getMessageType());
            messageDto.setIsRead(savedMessage.getIsRead());
            messageDto.setCreatedAt(savedMessage.getCreatedAt());
            messageDto.setUpdatedAt(savedMessage.getUpdatedAt());
            
            // Include E2EE parameters for decryption
            messageDto.setEncryptedAESKey(encryptedMessageDto.getEncryptedAESKey());
            messageDto.setIv(encryptedMessageDto.getIv());
            messageDto.setIsEncrypted(true); // Mark as encrypted for frontend processing

            // Send via WebSocket
            try {
                chatWebSocketHandler.sendMessageToUser(receiver.getId(), messageDto);
            } catch (Exception e) {
                System.err.println("Failed to send WebSocket message: " + e.getMessage());
            }

            return Result.success(messageDto);
        } catch (Exception e) {
            System.err.println("E2EE Error: " + e.getMessage());
            return Result.error("发送加密消息失败: " + e.getMessage());
        }
    }

    /**
     * Check if both users have E2EE enabled
     */
    public Result checkE2EEStatus(Integer userId1, Integer userId2) {
        try {
            User user1 = userRepository.findById(userId1).orElse(null);
            User user2 = userRepository.findById(userId2).orElse(null);

            if (user1 == null || user2 == null) {
                return Result.error("用户不存在");
            }

            Map<String, Boolean> status = new HashMap<>();
            status.put("user1E2EEEnabled", user1.getE2eeEnabled() != null ? user1.getE2eeEnabled() : false);
            status.put("user2E2EEEnabled", user2.getE2eeEnabled() != null ? user2.getE2eeEnabled() : false);
            status.put("bothEnabled", (user1.getE2eeEnabled() != null ? user1.getE2eeEnabled() : false) && 
                                    (user2.getE2eeEnabled() != null ? user2.getE2eeEnabled() : false));

            return Result.success(status);
        } catch (Exception e) {
            return Result.error("检查E2EE状态失败: " + e.getMessage());
        }
    }
}
