package com.example.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand.entity.Notification;

public interface NotificationService {
    void sendNotification(Long userId, String type, String title, String content, Long relatedId);
    IPage<Notification> getNotifications(Long userId, int pageNum, int pageSize);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    int getUnreadCount(Long userId);
}
