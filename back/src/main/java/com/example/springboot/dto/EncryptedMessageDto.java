package com.example.springboot.dto;

public class EncryptedMessageDto {
    private Integer receiverId;
    private String encryptedContent;
    private String encryptedAESKey;
    private String iv;
    private String messageType = "ENCRYPTED";

    public EncryptedMessageDto() {}

    public EncryptedMessageDto(Integer receiverId, String encryptedContent) {
        this.receiverId = receiverId;
        this.encryptedContent = encryptedContent;
    }

    public EncryptedMessageDto(Integer receiverId, String encryptedContent, String encryptedAESKey, String iv) {
        this.receiverId = receiverId;
        this.encryptedContent = encryptedContent;
        this.encryptedAESKey = encryptedAESKey;
        this.iv = iv;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public String getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(String encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    public String getEncryptedAESKey() {
        return encryptedAESKey;
    }

    public void setEncryptedAESKey(String encryptedAESKey) {
        this.encryptedAESKey = encryptedAESKey;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
