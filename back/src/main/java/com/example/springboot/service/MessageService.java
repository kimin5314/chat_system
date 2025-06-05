package com.example.springboot.service;

import com.example.springboot.dto.ConversationDto;
import com.example.springboot.dto.MessageDto;
import com.example.springboot.dto.SendMessageDto;
import com.example.springboot.entity.Message;
import com.example.springboot.entity.User;
import com.example.springboot.repository.MessageRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Transactional
    public MessageDto sendMessage(Integer senderId, SendMessageDto sendMessageDto) {
        // Validate that receiver exists
        User receiver = userRepository.findById(sendMessageDto.getReceiverId().intValue())
                .orElseThrow(() -> new RuntimeException("接收者不存在"));

        User sender = userRepository.findById(senderId.intValue())
                .orElseThrow(() -> new RuntimeException("发送者不存在"));

        // Create message
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(sendMessageDto.getReceiverId());
        message.setContent(sendMessageDto.getContent());
        message.setMessageType(sendMessageDto.getMessageType());
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        // Create response DTO
        MessageDto messageDto = new MessageDto(savedMessage);
        messageDto.setSenderUsername(sender.getUsername());
        messageDto.setSenderAvatar(sender.getAvatarUrl());
        messageDto.setReceiverUsername(receiver.getUsername());
        messageDto.setReceiverAvatar(receiver.getAvatarUrl());

        // Send real-time notification via WebSocket
        try {
            chatWebSocketHandler.sendMessageToUser(sendMessageDto.getReceiverId(), messageDto);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to send WebSocket message: " + e.getMessage());
        }

        return messageDto;
    }

    public List<MessageDto> getConversation(Integer userId1, Integer userId2) {
        List<Message> messages = messageRepository.findConversation(userId1, userId2);
        
        // Get user info for message DTOs
        Map<Integer, User> userMap = new HashMap<>();
        userMap.put(userId1, userRepository.findById(userId1).orElse(null));
        userMap.put(userId2, userRepository.findById(userId2).orElse(null));

        return messages.stream().map(message -> {
            MessageDto dto = new MessageDto(message);
            
            User sender = userMap.get(message.getSenderId());
            User receiver = userMap.get(message.getReceiverId());
            
            if (sender != null) {
                dto.setSenderUsername(sender.getUsername());
                dto.setSenderAvatar(sender.getAvatarUrl());
            }
            if (receiver != null) {
                dto.setReceiverUsername(receiver.getUsername());
                dto.setReceiverAvatar(receiver.getAvatarUrl());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ConversationDto> getConversations(Integer userId) {
        List<Message> lastMessages = messageRepository.findLastMessagesForUser(userId);
        List<ConversationDto> conversations = new ArrayList<>();

        for (Message message : lastMessages) {
            Integer friendId = message.getSenderId().equals(userId) ? 
                           message.getReceiverId() : message.getSenderId();
            
            User friend = userRepository.findById(friendId).orElse(null);
            if (friend == null) continue;

            Long unreadCount = messageRepository.countUnreadMessages(userId, friendId);
            
            ConversationDto conversation = new ConversationDto();
            conversation.setFriendId(friendId);
            conversation.setFriendUsername(friend.getUsername());
            conversation.setFriendAvatar(friend.getAvatarUrl());
            conversation.setLastMessage(message.getContent());
            conversation.setUnreadCount(unreadCount);
            conversation.setLastMessageTime(message.getCreatedAt());
            conversation.setIsOnline(chatWebSocketHandler.isUserOnline(friendId));

            conversations.add(conversation);
        }

        return conversations;
    }

    @Transactional
    public void markMessagesAsRead(Integer receiverId, Integer senderId) {
        messageRepository.markMessagesAsRead(receiverId, senderId);
    }

    public Long getUnreadCount(Integer receiverId, Integer senderId) {
        return messageRepository.countUnreadMessages(receiverId, senderId);
    }
}
