package com.example.secondhand.controller.admin;

import com.example.secondhand.common.BusinessException;
import com.example.secondhand.common.Result;
import com.example.secondhand.dto.LoginDTO;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginDTO loginDTO) {
        // 先查询用户信息（不清除密码）
        User user = userService.getUserInfo(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证是否为管理员
        if (!user.getRole().equals(1)) {
            throw new BusinessException("非管理员账号，无法登录管理后台");
        }
        
        // 验证密码并生成 token
        String token = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }
}
