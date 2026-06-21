package com.example.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand.common.BusinessException;
import com.example.secondhand.entity.User;
import com.example.secondhand.mapper.UserMapper;
import com.example.secondhand.security.JwtUtil;
import com.example.secondhand.service.SmsService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 15;
    private static final String LOGIN_FAIL_KEY_PREFIX = "login:fail:";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SmsService smsService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 检查登录失败次数
        String failKey = LOGIN_FAIL_KEY_PREFIX + username;
        String failCountStr = stringRedisTemplate.opsForValue().get(failKey);
        int failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
        if (failCount >= MAX_LOGIN_FAIL_COUNT) {
            throw new BusinessException("登录失败次数过多，账号已锁定" + LOCK_MINUTES + "分钟");
        }

        // BCrypt 密码验证
        if (user.getPassword() == null || !PASSWORD_ENCODER.matches(password, user.getPassword())) {
            // 登录失败，计数 +1
            stringRedisTemplate.opsForValue().increment(failKey);
            stringRedisTemplate.expire(failKey, LOCK_MINUTES, TimeUnit.MINUTES);
            int remaining = MAX_LOGIN_FAIL_COUNT - failCount - 1;
            if (remaining > 0) {
                throw new BusinessException("密码错误，还剩" + remaining + "次机会");
            } else {
                throw new BusinessException("密码错误次数过多，账号已锁定" + LOCK_MINUTES + "分钟");
            }
        }

        // 登录成功，清除失败计数
        stringRedisTemplate.delete(failKey);
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public String loginBySms(String phone, String code) {
        // 校验手机号格式
        if (!phone.matches("^\\d{11}$")) {
            throw new BusinessException("手机号格式错误");
        }

        // 校验验证码格式
        if (!code.matches("^\\d{6}$")) {
            throw new BusinessException("验证码格式错误");
        }

        // 验证验证码
        if (!smsService.validateCode(phone, code)) {
            throw new BusinessException("验证码错误");
        }

        // 查询用户
        User user = getByPhone(phone);

        // 自动注册
        if (user == null) {
            user = new User();
            String suffix = phone.substring(phone.length() - 4);
            user.setUsername("user_" + suffix);
            user.setNickname("用户" + suffix);
            user.setPhone(phone);
            user.setPassword(PASSWORD_ENCODER.encode("123456")); // BCrypt加密初始密码
            user.setRole(0);
            user.setStatus(1);
            user.setPhoneVerified(1);
            userMapper.insert(user);
        }

        // 生成 token
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public void register(String username, String password, String nickname, String phone) {
        User existing = userMapper.findByUsername(username);
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(PASSWORD_ENCODER.encode(password));  // BCrypt加密存储
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public User getUserInfo(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateProfile(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        user.setUsername(null);
        userMapper.updateById(user);
    }

    @Override
    public User getByPhone(String phone) {
        if (phone == null) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return getOne(wrapper);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        // 获取当前用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验新密码格式
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException("密码格式不正确，长度为 6-20 位数字或字母");
        }

        // 判断是首次设置密码还是修改密码
        boolean isFirstSet = (user.getPassword() == null || user.getPassword().isEmpty());
        
        if (isFirstSet) {
            // 首次设置密码：不需要验证旧密码
            if (oldPassword != null && !oldPassword.isEmpty()) {
                // 用户输入了旧密码，但实际是首次设置，忽略即可
            }
        } else {
            // 修改密码：需要验证旧密码（BCrypt验证）
            if (oldPassword == null || oldPassword.isEmpty()) {
                throw new BusinessException("请输入原密码");
            }
            if (!PASSWORD_ENCODER.matches(oldPassword, user.getPassword())) {
                throw new BusinessException("原密码错误");
            }
            // 检查新旧密码是否相同
            if (PASSWORD_ENCODER.matches(newPassword, user.getPassword())) {
                throw new BusinessException("新密码不能与原密码相同");
            }
        }

        // BCrypt加密存储新密码
        user.setPassword(PASSWORD_ENCODER.encode(newPassword));
        updateById(user);
    }

    @Override
    public void updateUsername(Long userId, String newUsername) {
        // 获取当前用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证用户名格式：3-20 位，只能包含字母、数字、下划线
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        
        if (newUsername.length() < 3 || newUsername.length() > 20) {
            throw new BusinessException("用户名长度必须在 3-20 位之间");
        }
        
        if (!newUsername.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException("用户名只能包含字母、数字和下划线");
        }

        // 检查用户名是否已被占用
        User existingUser = userMapper.findByUsername(newUsername);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BusinessException("用户名已被占用");
        }

        // 更新用户名
        user.setUsername(newUsername);
        updateById(user);
    }

    @Override
    public String generateNewToken(Long userId, String username, Integer role) {
        return jwtUtil.generateToken(userId, username, role);
    }
}
