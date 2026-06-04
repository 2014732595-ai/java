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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        return Result.success(user);
    }

    @PutMapping
    public Result<Void> updateProfile(@RequestBody User user) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserInfo(username);
        user.setId(currentUser.getId());
        user.setUsername(username);
        userService.updateProfile(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/update-password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserInfo(username);
        
        userService.updatePassword(
            currentUser.getId(),
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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserInfo(username);
        
        String newUsername = request.get("username");
        userService.updateUsername(currentUser.getId(), newUsername);
        
        // 生成新的 Token
        String newToken = ((com.example.secondhand.service.impl.UserServiceImpl) userService)
            .generateNewToken(currentUser.getId(), newUsername, currentUser.getRole());
        
        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        
        return Result.success(result);
    }
}
