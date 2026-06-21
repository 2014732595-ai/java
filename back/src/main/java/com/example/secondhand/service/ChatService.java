package com.example.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.entity.ChatMessage;

import java.util.List;
import java.util.Map;

public interface ChatService {
    ChatMessage sendMessage(Long fromId, Long toId, Long productId, String content);
    List<Map<String, Object>> getConversations(Long userId);
    IPage<ChatMessage> getMessages(Long userId1, Long userId2, Long productId, int pageNum, int pageSize);
    void markAsRead(Long userId, Long fromId);
    int getUnreadCount(Long userId);
}
