package com.example.secondhand.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 短信验证码实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsCode {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 验证码
     */
    private String code;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
}
