package com.example.secondhand.service;

/**
 * 短信服务接口
 */
public interface SmsService {

    /**
     * 发送验证码
     * @param phone 手机号
     */
    void sendCode(String phone);

    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    boolean validateCode(String phone, String code);
}
