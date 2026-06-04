package com.example.secondhand.service.impl;

import com.example.secondhand.common.BusinessException;
import com.example.secondhand.entity.SmsCode;
import com.example.secondhand.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 短信服务实现类（开发环境 - 控制台打印）
 */
@Slf4j
@Service
@Profile("dev")  // 仅开发环境使用
public class SmsServiceImpl implements SmsService {

    // 内存存储验证码
    private static final Map<String, SmsCode> CODE_STORE = new ConcurrentHashMap<>();

    // 验证码有效期（分钟）
    private static final int EXPIRE_MINUTES = 5;

    // 发送间隔（秒）
    private static final int SEND_INTERVAL = 60;

    @Override
    public void sendCode(String phone) {
        // 校验手机号格式
        if (!phone.matches("^\\d{11}$")) {
            throw new BusinessException("手机号格式错误");
        }

        // 检查发送频率
        checkSendFrequency(phone);

        // 生成验证码
        String code = generateCode();

        // 存储验证码
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(EXPIRE_MINUTES);
        CODE_STORE.put(phone, new SmsCode(phone, code, expireTime, now));

        // 打印到控制台
        log.info("[SMS] 手机号：{}, 验证码：{}, 过期时间：{}", phone, code, expireTime);
    }

    @Override
    public boolean validateCode(String phone, String code) {
        // 从内存获取验证码
        SmsCode smsCode = CODE_STORE.get(phone);

        // 验证
        if (smsCode == null) {
            log.warn("[SMS] 验证码不存在：{}", phone);
            return false;
        }

        if (smsCode.getExpireTime().isBefore(LocalDateTime.now())) {
            log.warn("[SMS] 验证码已过期：{}", phone);
            CODE_STORE.remove(phone);
            return false;
        }

        if (!smsCode.getCode().equals(code)) {
            log.warn("[SMS] 验证码错误：{}", phone);
            return false;
        }

        // 验证成功后删除
        CODE_STORE.remove(phone);
        log.info("[SMS] 验证码验证成功：{}", phone);
        return true;
    }

    /**
     * 检查发送频率
     */
    private void checkSendFrequency(String phone) {
        SmsCode lastCode = CODE_STORE.get(phone);
        if (lastCode != null && !lastCode.getExpireTime().isBefore(LocalDateTime.now())) {
            long seconds = ChronoUnit.SECONDS.between(lastCode.getSendTime(), LocalDateTime.now());
            if (seconds < SEND_INTERVAL) {
                throw new BusinessException("验证码已发送，请 " + (SEND_INTERVAL - seconds) + " 秒后再试");
            }
        }
    }

    /**
     * 生成 6 位随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
