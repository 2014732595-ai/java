package com.example.secondhand.controller.user;

import com.example.secondhand.common.Result;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
}
