package com.example.secondhand.controller.user;

import com.example.secondhand.common.Result;
import com.example.secondhand.dto.*;
import com.example.secondhand.entity.User;
import com.example.secondhand.security.JwtUtil;
import com.example.secondhand.service.SmsService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/auth")
public class UserAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }

    /**
     * 手机号验证码登录
     */
    @PostMapping("/login-by-sms")
    public Result<LoginResponse> loginBySms(@Valid @RequestBody SmsLoginRequest request) {
        String token = userService.loginBySms(request.getPhone(), request.getCode());
        
        // 查询用户信息
        User user = userService.getByPhone(request.getPhone());
        user.setPassword(null);
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(user);
        
        return Result.success(response);
    }

    /**
     * 发送验证码
     */
    @PostMapping("/sms-code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        smsService.sendCode(request.getPhone());
        return Result.success();
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO.getUsername(), registerDTO.getPassword(),
                registerDTO.getNickname(), registerDTO.getPhone());
        return Result.success();
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh-token")
    public Result<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String oldToken = request.get("token");
        if (oldToken == null || oldToken.isEmpty()) {
            return Result.error("Token不能为空");
        }
        try {
            String newToken = jwtUtil.refreshToken(oldToken);
            Map<String, String> result = new HashMap<>();
            result.put("token", newToken);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Token无效或已过期");
        }
    }
}
