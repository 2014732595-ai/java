package com.example.secondhand.dto;

import lombok.Data;
import com.example.secondhand.entity.User;

/**
 * 登录响应 DTO
 */
@Data
public class LoginResponse {

    private String token;

    private User user;
}
