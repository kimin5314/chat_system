package com.example.springboot.dto;

import com.example.springboot.entity.Message;
import com.example.springboot.Enum.MessageType;

public class SendMessageDto {
    private Integer receiverId;
    private String content;
    private MessageType messageType = MessageType.TEXT;

    public SendMessageDto() {}

    public SendMessageDto(Integer receiverId, String content, MessageType messageType) {
        this.receiverId = receiverId;
        this.content = content;
        this.messageType = messageType;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
