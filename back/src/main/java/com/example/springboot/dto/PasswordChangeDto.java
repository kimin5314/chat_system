package com.example.springboot.dto;

import lombok.Data;

@Data
public class PasswordChangeDto {
    private String username;
    private String userPassword;
    private String newPassword;

}
