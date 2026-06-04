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
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SmsService smsService;

    @Override
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        
        // 明文密码验证
        if (user.getPassword() == null || !user.getPassword().equals(password)) {
            throw new BusinessException("密码错误");
        }
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
            user.setPassword(""); // 设置空密码（手机号登录不需要密码）
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
        user.setPassword(password);  // 明文密码存储
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
            throw new BusinessException("密码格式不正确，长度为 6-20 位");
        }

        // 判断是首次设置密码还是修改密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // 首次设置密码：不需要验证旧密码
            if (oldPassword != null && !oldPassword.isEmpty()) {
                throw new BusinessException("首次设置密码，无需输入原密码");
            }
        } else {
            // 修改密码：需要验证旧密码（明文比较）
            if (oldPassword == null || oldPassword.isEmpty()) {
                throw new BusinessException("请输入原密码");
            }
            if (!user.getPassword().equals(oldPassword)) {
                throw new BusinessException("原密码错误");
            }
            // 检查新旧密码是否相同
            if (newPassword.equals(user.getPassword())) {
                throw new BusinessException("新密码不能与原密码相同");
            }
        }

        // 明文存储新密码
        user.setPassword(newPassword);
        updateById(user);
    }
}
