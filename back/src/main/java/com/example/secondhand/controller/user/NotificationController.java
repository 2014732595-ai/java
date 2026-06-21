package com.example.secondhand.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Notification;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.NotificationService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/notifications")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<IPage<Notification>> getNotifications(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId();
        IPage<Notification> page = notificationService.getNotifications(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount() {
        Long userId = getCurrentUserId();
        int count = notificationService.getUnreadCount(userId);
        HashMap<String, Integer> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    private Long getCurrentUserId() {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (credentials instanceof Long) {
            return (Long) credentials;
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        return user.getId();
    }
}
