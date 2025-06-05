package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ConversationDto {
    private Integer friendId;
    private String friendUsername;
    private String friendAvatar;
    private String lastMessage;
    private Long unreadCount;
    private Boolean isOnline;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    public ConversationDto() {}

    public ConversationDto(Integer friendId, String friendUsername, String friendAvatar, 
                          String lastMessage, Long unreadCount, Boolean isOnline, 
                          LocalDateTime lastMessageTime) {
        this.friendId = friendId;
        this.friendUsername = friendUsername;
        this.friendAvatar = friendAvatar;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
        this.isOnline = isOnline;
        this.lastMessageTime = lastMessageTime;
    }

    // Getters and Setters
    public Integer getFriendId() {
        return friendId;
    }

    public void setFriendId(Integer friendId) {
        this.friendId = friendId;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    public String getFriendAvatar() {
        return friendAvatar;
    }

    public void setFriendAvatar(String friendAvatar) {
        this.friendAvatar = friendAvatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
