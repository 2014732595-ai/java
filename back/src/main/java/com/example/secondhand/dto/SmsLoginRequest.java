package com.example.secondhand.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 手机号登录请求 DTO
 */
@Data
public class SmsLoginRequest {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
