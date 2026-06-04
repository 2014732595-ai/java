package com.example.secondhand.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 发送验证码请求 DTO
 */
@Data
public class SmsCodeRequest {

    @NotBlank(message = "手机号不能为空")
    private String phone;
}
