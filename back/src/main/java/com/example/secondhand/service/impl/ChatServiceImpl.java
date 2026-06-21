package com.example.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.secondhand.entity.ChatMessage;
import com.example.secondhand.entity.User;
import com.example.secondhand.mapper.ChatMessageMapper;
import com.example.secondhand.mapper.UserMapper;
import com.example.secondhand.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public ChatMessage sendMessage(Long fromId, Long toId, Long productId, String content) {
        ChatMessage message = new ChatMessage();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setProductId(productId);
        message.setContent(content);
        message.setIsRead(0);
        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<Map<String, Object>> getConversations(Long userId) {
        // 查询所有与当前用户有消息往来的用户
        LambdaQueryWrapper<ChatMessage> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(ChatMessage::getFromId, userId);
        List<ChatMessage> sent = chatMessageMapper.selectList(wrapper1);

        LambdaQueryWrapper<ChatMessage> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(ChatMessage::getToId, userId);
        List<ChatMessage> received = chatMessageMapper.selectList(wrapper2);

        // 提取对方用户ID
        Set<Long> contactIds = new HashSet<>();
        for (ChatMessage msg : sent) {
            contactIds.add(msg.getToId());
        }
        for (ChatMessage msg : received) {
            contactIds.add(msg.getFromId());
        }
        contactIds.remove(userId);

        List<Map<String, Object>> conversations = new ArrayList<>();
        for (Long contactId : contactIds) {
            Map<String, Object> conv = new HashMap<>();
            conv.put("userId", contactId);

            // 获取联系人信息
            User contact = userMapper.selectById(contactId);
            conv.put("nickname", contact != null ? contact.getNickname() : "用户" + contactId);
            conv.put("avatar", contact != null ? contact.getAvatar() : null);

            // 获取最后一条消息
            LambdaQueryWrapper<ChatMessage> lastMsgWrapper = new LambdaQueryWrapper<>();
            lastMsgWrapper.and(w -> w
                    .and(w1 -> w1.eq(ChatMessage::getFromId, userId).eq(ChatMessage::getToId, contactId))
                    .or(w2 -> w2.eq(ChatMessage::getFromId, contactId).eq(ChatMessage::getToId, userId))
            ).orderByDesc(ChatMessage::getCreateTime).last("LIMIT 1");
            ChatMessage lastMsg = chatMessageMapper.selectOne(lastMsgWrapper);
            conv.put("lastMessage", lastMsg != null ? lastMsg.getContent() : "");
            conv.put("lastTime", lastMsg != null ? lastMsg.getCreateTime() : null);

            // 未读数
            LambdaQueryWrapper<ChatMessage> unreadWrapper = new LambdaQueryWrapper<>();
            unreadWrapper.eq(ChatMessage::getFromId, contactId)
                    .eq(ChatMessage::getToId, userId)
                    .eq(ChatMessage::getIsRead, 0);
            conv.put("unreadCount", chatMessageMapper.selectCount(unreadWrapper));

            conversations.add(conv);
        }

        // 按最后消息时间排序
        conversations.sort((a, b) -> {
            if (a.get("lastTime") == null && b.get("lastTime") == null) return 0;
            if (a.get("lastTime") == null) return 1;
            if (b.get("lastTime") == null) return -1;
            return ((Comparable) b.get("lastTime")).compareTo(a.get("lastTime"));
        });

        return conversations;
    }

    @Override
    public IPage<ChatMessage> getMessages(Long userId1, Long userId2, Long productId, int pageNum, int pageSize) {
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(w1 -> w1.eq(ChatMessage::getFromId, userId1).eq(ChatMessage::getToId, userId2))
                .or(w2 -> w2.eq(ChatMessage::getFromId, userId2).eq(ChatMessage::getToId, userId1))
        );
        if (productId != null) {
            wrapper.eq(ChatMessage::getProductId, productId);
        }
        wrapper.orderByDesc(ChatMessage::getCreateTime);
        return chatMessageMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long fromId) {
        LambdaUpdateWrapper<ChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatMessage::getFromId, fromId)
                .eq(ChatMessage::getToId, userId)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1);
        chatMessageMapper.update(null, wrapper);
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getToId, userId).eq(ChatMessage::getIsRead, 0);
        return Math.toIntExact(chatMessageMapper.selectCount(wrapper));
    }
}
