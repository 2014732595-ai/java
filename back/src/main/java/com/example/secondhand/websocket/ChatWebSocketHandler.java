package com.example.secondhand.websocket;

import com.example.secondhand.entity.ChatMessage;
import com.example.secondhand.security.JwtUtil;
import com.example.secondhand.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 存储在线用户的 WebSocket 会话: userId -> session
    private static final ConcurrentHashMap<Long, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();
    // 存储 session 对应的 userId
    private static final ConcurrentHashMap<String, Long> SESSION_USER_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 URL 参数获取 token 并解析 userId
        String query = session.getUri().getQuery();
        String token = null;
        if (query != null && query.startsWith("token=")) {
            token = query.substring(6);
        }
        if (token != null && !token.isEmpty()) {
            try {
                Long userId = jwtUtil.getUserId(token);
                ONLINE_USERS.put(userId, session);
                SESSION_USER_MAP.put(session.getId(), userId);
            } catch (Exception e) {
                session.close(CloseStatus.NOT_ACCEPTABLE);
            }
        } else {
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long fromId = SESSION_USER_MAP.get(session.getId());
        if (fromId == null) return;

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Long toId = Long.valueOf(payload.get("toId").toString());
        Long productId = payload.get("productId") != null ? Long.valueOf(payload.get("productId").toString()) : null;
        String content = payload.get("content").toString();

        // 保存消息到数据库
        ChatMessage chatMessage = chatService.sendMessage(fromId, toId, productId, content);

        // 构建返回消息
        Map<String, Object> response = new HashMap<>();
        response.put("id", chatMessage.getId());
        response.put("fromId", fromId);
        response.put("toId", toId);
        response.put("productId", productId);
        response.put("content", content);
        response.put("createTime", chatMessage.getCreateTime());
        String json = objectMapper.writeValueAsString(response);

        // 发送给接收者
        WebSocketSession targetSession = ONLINE_USERS.get(toId);
        if (targetSession != null && targetSession.isOpen()) {
            targetSession.sendMessage(new TextMessage(json));
        }

        // 回发给发送者（确认）
        session.sendMessage(new TextMessage(json));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = SESSION_USER_MAP.remove(session.getId());
        if (userId != null) {
            ONLINE_USERS.remove(userId);
        }
    }
}
