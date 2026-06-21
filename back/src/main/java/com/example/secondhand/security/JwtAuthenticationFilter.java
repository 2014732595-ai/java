package com.example.secondhand.security;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token) && !jwtUtil.isTokenExpired(token)) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                String username = claims.getSubject();
                Integer role = claims.get("role", Integer.class);
                Long userId = claims.get("userId", Long.class);  // 从 Token 中获取 userId
                String authority = role == 1 ? "ROLE_ADMIN" : "ROLE_USER";

                // 使用 UsernamePasswordAuthenticationToken，把 userId 放在 credentials 中
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,           // principal: 用户名
                        userId,             // credentials: 存储 userId（验证后这个字段可以存储其他信息）
                        Collections.singletonList(new SimpleGrantedAuthority(authority)));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Token 即将过期时自动刷新，通过响应头返回新 Token
                if (jwtUtil.isTokenExpiringSoon(token)) {
                    String newToken = jwtUtil.refreshToken(token);
                    response.setHeader("X-New-Token", newToken);
                }
            } catch (Exception ignored) {
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
