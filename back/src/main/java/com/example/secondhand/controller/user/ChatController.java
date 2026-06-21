package com.example.secondhand.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.common.Result;
import com.example.secondhand.entity.ChatMessage;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.ChatService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/chat")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations() {
        Long userId = getCurrentUserId();
        List<Map<String, Object>> conversations = chatService.getConversations(userId);
        return Result.success(conversations);
    }

    @GetMapping("/messages/{userId}")
    public Result<IPage<ChatMessage>> getMessages(
            @PathVariable Long userId,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long currentUserId = getCurrentUserId();
        IPage<ChatMessage> page = chatService.getMessages(currentUserId, userId, productId, pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/read/{fromId}")
    public Result<Void> markAsRead(@PathVariable Long fromId) {
        Long userId = getCurrentUserId();
        chatService.markAsRead(userId, fromId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount() {
        Long userId = getCurrentUserId();
        int count = chatService.getUnreadCount(userId);
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
