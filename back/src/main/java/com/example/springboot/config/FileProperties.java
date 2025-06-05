package com.example.springboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {
    
    private String uploadDir;
    private String avatarDir;
    
    public String getUploadDir() {
        return uploadDir;
    }
    
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
    
    public String getAvatarDir() {
        return avatarDir;
    }
    
    public void setAvatarDir(String avatarDir) {
        this.avatarDir = avatarDir;
    }
}
