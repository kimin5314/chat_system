package com.example.springboot.dto;

import com.example.springboot.entity.Message;

public class SendMessageDto {
    private Integer receiverId;
    private String content;
    private Message.MessageType messageType = Message.MessageType.TEXT;

    public SendMessageDto() {}

    public SendMessageDto(Integer receiverId, String content, Message.MessageType messageType) {
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

    public Message.MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(Message.MessageType messageType) {
        this.messageType = messageType;
    }
}
