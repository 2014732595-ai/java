package com.example.secondhand.config;

import com.example.secondhand.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private com.example.secondhand.security.UserDetailsServiceImpl userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 允许访问的业务接口（原来的配置）
                .antMatchers(
                        "/api/user/auth/login",
                        "/api/user/auth/login-by-sms",
                        "/api/user/auth/sms-code",
                        "/api/user/auth/register",
                        "/api/user/auth/refresh-token",
                        "/api/user/products",
                        "/api/user/products/**",
                        "/api/user/categories",
                        "/api/user/comments/**",
                        "/api/user/reviews/product/**",
                        "/api/file/**"
                ).permitAll()
                // 管理端登录（原来的配置）
                .antMatchers("/api/admin/auth/login").permitAll()
                // WebSocket 端点
                .antMatchers("/ws/**").permitAll()
                // ---------------- 新增Swagger放行路径 ----------------
                .antMatchers(
                        "/swagger-ui.html",       // Swagger主页面
                        "/v2/api-docs",           // 导入ApiPost用的JSON接口文档
                        "/swagger-resources/**",  // Swagger配置资源
                        "/webjars/**",            // Swagger静态资源（JS/CSS）
                        "/doc.html"               // 兼容Knife4j增强版Swagger
                ).permitAll()
                // --------------------------------------------------
                // 其他接口需要认证
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}