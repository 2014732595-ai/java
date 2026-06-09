package com.example.secondhand.controller.user;

import com.example.secondhand.common.Result;
import com.example.secondhand.dto.UpdatePasswordRequest;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<User> getProfile() {
        // 从认证信息中获取 userId（从 credentials 字段）
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        Long userId = null;
        
        // 如果 credentials 是 Long 类型，说明存储的是 userId
        if (credentials instanceof Long) {
            userId = (Long) credentials;
        }
        // 兼容其他情况，从用户名查询
        else {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User tempUser = userService.getUserInfo(username);
            userId = tempUser.getId();
        }
        
        // 直接用 userId 查询，避免用户名修改后查不到
        User dbUser = userService.getById(userId);
        
        // 判断是否是首次设置密码
        boolean isFirstSetPassword = (dbUser.getPassword() == null || dbUser.getPassword().isEmpty());
        
        if (!isFirstSetPassword) {
            dbUser.setPassword("[HAS_PASSWORD]");
        }
        
        return Result.success(dbUser);
    }

    @PutMapping
    public Result<Void> updateProfile(@RequestBody User user) {
        // 从认证信息中获取 userId（从 credentials 字段）
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        Long userId = null;
        
        if (credentials instanceof Long) {
            userId = (Long) credentials;
        } else {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User tempUser = userService.getUserInfo(username);
            userId = tempUser.getId();
        }
        
        user.setId(userId);
        userService.updateProfile(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/update-password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        // 从认证信息中获取 userId（从 credentials 字段）
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        Long userId = null;
        
        if (credentials instanceof Long) {
            userId = (Long) credentials;
        } else {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User tempUser = userService.getUserInfo(username);
            userId = tempUser.getId();
        }
        
        userService.updatePassword(
            userId,
            request.getOldPassword(),
            request.getNewPassword()
        );
        
        return Result.success();
    }

    /**
     * 修改用户名
     */
    @PutMapping("/username")
    public Result<Map<String, String>> updateUsername(@RequestBody Map<String, String> request) {
        // 从认证信息中获取 userId（从 credentials 字段）
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        Long userId = null;
        
        if (credentials instanceof Long) {
            userId = (Long) credentials;
        } else {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User tempUser = userService.getUserInfo(username);
            userId = tempUser.getId();
        }
        
        String newUsername = request.get("username");
        userService.updateUsername(userId, newUsername);
        
        // 生成新的 Token
        User user = userService.getById(userId);
        String newToken = ((com.example.secondhand.service.impl.UserServiceImpl) userService)
            .generateNewToken(userId, newUsername, user.getRole());
        
        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        
        return Result.success(result);
    }
}
